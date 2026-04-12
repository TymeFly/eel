package com.github.tymefly.eel;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFailException;
import com.github.tymefly.eel.exception.EelSemanticException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.readme.Half;
import com.github.tymefly.eel.readme.MyClass1;
import com.github.tymefly.eel.readme.MyClass2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Code taken from the readme.md file
 */
@ExtendWith(SystemStubsExtension.class)
public class ReadmeIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    @TempDir
    private File tempFolder;


    private static final String someExpression = "$( 0 + 1 )";
    private static final Map<String, String> someData = Map.ofEntries(
        entry("key", "myValue"),
        entry("second", "2nd"),
        entry("root", "/my/path"),
        entry("myFilePrefix", "myFile"),
        entry("STR", "abc~0123"),
        entry("myValue", "99")
    );

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_BasicUsage() {
        String result = Eel.compile(someExpression)
            .evaluate()
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_InlineEelContext() {
        String result = Eel.factory()
            .withPrecision(12)
            .compile(someExpression)
            .evaluate()
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ExplicitEelContext() {
        EelContext context = EelContext.factory()
            .withPrecision(12)
            .build();
        String result = Eel.compile(context, someExpression)
            .evaluate()
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_InlineEelSymbolsTable() {
        Map<String, String> symTable = someData;
        String result = Eel.compile(someExpression)
            .evaluate(symTable)
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ExplicitSymbolsTable() {
        Map<String, String> values = someData;
        SymbolsTable symbols = SymbolsTable.factory(".")
            .withValues("val", values)
            .withProperties("prop")
            .build();
        String result = Eel.compile(someExpression)
            .evaluate(symbols)
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ExplicitEelContextAndSymbolsTable() {
        EelContext context = EelContext.factory()
            .withPrecision(12)
            .build();
        SymbolsTable symbols = SymbolsTable.factory(".")
            .withProperties("props")
            .withEnvironment("env")
            .build();
        String result = Eel.compile(context, someExpression)
            .evaluate(symbols)
            .asText();

        assertEquals("1", result, "Unexpected result");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UsingTheResult() {
        Result result = Eel.compile(someExpression)
            .evaluate();

        if (result.getType() == Type.NUMBER) {
            System.out.println("As a number " + result.asNumber());
        } else if (result.getType() == Type.LOGIC) {
            System.out.println("As logic " + result.asLogic());
        } else if (result.getType() == Type.DATE) {
            System.out.println("As a date " + result.asDate());
        }

        System.out.println("As a string " + result.asText());

        assertTrue(stdOut.getLinesNormalized().contains("As a number 1"),
            "Missing Number message");
        assertFalse(stdOut.getLinesNormalized().contains("As logic"),
            "Unexpected Logic message");
        assertFalse(stdOut.getLinesNormalized().contains("As logic"),
            "Unexpected Date message");
        assertTrue(stdOut.getLinesNormalized().contains("As a string 1"),
            "Missing String message");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SourceCode() {
        Result result = Eel.compile("\\$CA 1.23")
            .evaluate();

        assertResult("Escaped $", Type.TEXT, "$CA 1.23", result);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Comments_closed() {
        Result result = Eel.compile("$random( 10, ## upper-limit ## 99 )")
            .evaluate();

        assertEquals(Type.NUMBER, result.getType(), "Unexpected Type");
        assertTrue(result.asText().matches("[1-9][0-9]"), "Unexpected Value: " + result.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Comments_trailing() {
        InputStream source = getClass().getResourceAsStream("/comment.eel");
        Result result = Eel.factory()
            .compile(source)
            .evaluate();

        assertEquals(Type.NUMBER, result.getType(), "Unexpected Type");
        assertTrue(result.asText().matches("[1-9][0-9](\\.5)?"), "Unexpected Value: " + result.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Operator_notes() {
        // in operator
        testExpression("$( (${myValue} = 1) or (${myValue} = 2) or (${myValue} = 3) )", Type.LOGIC, "false");
        testExpression("$( ${myValue} in { 1, 2, 3 } )", Type.LOGIC, "false");

        // bitwise rounding
        testExpression("$( 3 << ( 28 / 10) )", Type.NUMBER, "12");

        // divide operators
        testExpression("$( 12.0 / 1 )", Type.NUMBER, "12.0");
        testExpression("$( 12.3 / 1 )", Type.NUMBER, "12.3");
        testExpression("$( 12.5 / 1 )", Type.NUMBER, "12.5");
        testExpression("$( 12.7 / 1 )", Type.NUMBER, "12.7");
        testExpression("$( -12.0 / 1 )", Type.NUMBER, "-12.0");
        testExpression("$( -12.3 / 1 )", Type.NUMBER, "-12.3");
        testExpression("$( -12.5 / 1 )", Type.NUMBER, "-12.5");
        testExpression("$( -12.7 / 1 )", Type.NUMBER, "-12.7");

        testExpression("$( 12.0 // 1 )", Type.NUMBER, "12");
        testExpression("$( 12.3 // 1 )", Type.NUMBER, "12");
        testExpression("$( 12.5 // 1 )", Type.NUMBER, "12");
        testExpression("$( 12.7 // 1 )", Type.NUMBER, "12");
        testExpression("$( -12.0 // 1 )", Type.NUMBER, "-12");
        testExpression("$( -12.3 // 1 )", Type.NUMBER, "-13");
        testExpression("$( -12.5 // 1 )", Type.NUMBER, "-13");
        testExpression("$( -12.5 // 1 )", Type.NUMBER, "-13");
        testExpression("$( -12.7 // 1 )", Type.NUMBER, "-13");

        testExpression("$( 12.0 -/ 1 )", Type.NUMBER, "12");
        testExpression("$( 12.3 -/ 1 )", Type.NUMBER, "12");
        testExpression("$( 12.5 -/ 1 )", Type.NUMBER, "12");
        testExpression("$( 12.7 -/ 1 )", Type.NUMBER, "12");
        testExpression("$( -12.0 -/ 1 )", Type.NUMBER, "-12");
        testExpression("$( -12.3 -/ 1 )", Type.NUMBER, "-12");
        testExpression("$( -12.5 -/ 1 )", Type.NUMBER, "-12");
        testExpression("$( -12.7 -/ 1 )", Type.NUMBER, "-12");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ValueInterpolation_ScopedSymbols() {
        SymbolsTable symbols = SymbolsTable.factory(".")
            .withValues("m1", Map.ofEntries(entry("a", "Map1 value a"), entry("b", "Map1 value b")))
            .withValues("m2", Map.ofEntries(entry("a", "Map2 value a"), entry("b", "Map2 value b")))
            .build();

        assertThrows(EelUnknownSymbolException.class, () -> Eel.compile("${a}").evaluate(symbols), "${a}");
        assertEquals("Map1 value a", Eel.compile("${m1.a}").evaluate(symbols).asText(), "${m1.a}");
        assertEquals("Map2 value b", Eel.compile("${m2.b}").evaluate(symbols).asText(), "${m2.b}");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ValueInterpolation_modifiers() {
        assertEquals("myValue", Eel.compile("${key}").evaluate(someData).asText(), "${key}");
        assertEquals("7", Eel.compile("${#key}").evaluate(someData).asText(), "${#key}");
        assertEquals("MyValue", Eel.compile("${key^}").evaluate(someData).asText(), "${key^}");
        assertEquals("MYVALUE", Eel.compile("${key^^}").evaluate(someData).asText(), "${key^^}");
        assertEquals("myValue", Eel.compile("${key,}").evaluate(someData).asText(), "${key,}");
        assertEquals("myvalue", Eel.compile("${key,,}").evaluate(someData).asText(), "${key,,}");
        assertEquals("MyValue", Eel.compile("${key~}").evaluate(someData).asText(), "${key~}");
        assertEquals("MYvALUE", Eel.compile("${key~~}").evaluate(someData).asText(), "${key~~}");
        assertEquals("Val", Eel.compile("${key:2:3}").evaluate(someData).asText(), "${key:offset:count}");
        assertEquals("myValue", Eel.compile("${key-default}").evaluate(someData).asText(), "${key-default}");

        assertEquals("Myvalue", Eel.compile("${key,,^}").evaluate(someData).asText(), "${key,,^}");
        assertEquals("MYVALUE", Eel.compile("${key^^-default}").evaluate(someData).asText(), "${key^^-default}");
        assertEquals("myv", Eel.compile("${key:0:3,,}").evaluate(someData).asText(), "${key:0:3,,}");
        assertEquals("Val", Eel.compile("${key:2:3^}").evaluate(someData).asText(), "${key:2:3^}");
        assertEquals("7", Eel.compile("${#key-default}").evaluate(someData).asText(), "${#key-default}");
        assertEquals("myV", Eel.compile("${key:0:3:-default}").evaluate(someData).asText(), "${key:0:3:-default}");

        assertEquals("defaultText", Eel.compile("${undefined-defaultText}").evaluate(someData).asText(), "${undefined-defaultText}");
        assertEquals("", Eel.compile("${undefined-}").evaluate(someData).asText(), "${undefined-}");
        assertEquals("2nd", Eel.compile("${first-${second}}").evaluate(someData).asText(), "${first-${second}}");
        assertEquals("2nd", Eel.compile("${first-${second-defaultText}}").evaluate(someData).asText(), "${first-${second-defaultText}}");
        assertEquals("299792458", Eel.compile("${undefined-$number.c()}").evaluate(someData).asText(), "${undefined-$myFunction()}");
        assertEquals("hello", Eel.compile("${undefined-$( 'hello' )}").evaluate(someData).asText(), "${undefined-$( expression )}");
        assertEquals("0",
            Eel.compile("${STR:$(indexOf(${STR}, '~', fail()) + 1):1}").evaluate(someData).asText(),
            "${STR:$(indexOf(${STR}, '~', fail()) + 1):1}");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Lazy() {
        assertEquals("Undefined",
            Eel.compile("${myVariable-$log.error('Undefined')}").evaluate(someData).asText(),
            "Value interpolation might not evaluate the default");

        assertEquals("Undefined",
            Eel.compile("$( myVariable? ? ${myVariable} : log.error('Undefined') )").evaluate(someData).asText(),
            "The conditional operator will only evaluate one path");

        assertEquals("true",
            Eel.compile("$( true or (count() = 10) )").evaluate(someData).asText(),
            "The logic operators are short-circuited - or");
        assertEquals("false",
            Eel.compile("$( false and (count() = 10) )").evaluate(someData).asText(),
            "The logic operators are short-circuited - and");

        assertEquals("-1",
            Eel.compile("$indexOf(${myVariable-}, 'x')").evaluate(someData).asText(),
            "Function arguments might not be evaluated - no default");
        assertEquals("99",
            Eel.compile("$indexOf( ${myVariable-}, 'x', log.error('Undefined', 99) )").evaluate(someData).asText(),
            "Function arguments might not be evaluated - with default");

        assertEquals("1",
            Eel.compile("$( 1 ; fail(\"Not evaluated\") ; $[1] )").evaluate(someData).asText(),
            "Fail should not be evaluated");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UDF() {
        EelContext context = EelContext.factory()
            .withUdfClass(Half.class)
            .build();

        assertEquals("617", Eel.compile(context, "$divide.by2( 1234 )").evaluate().asText(), "divide.by2( 1234 )");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UDF_LoadClasses() {
        EelContext context = EelContext.factory()
            .withUdfClass(MyClass1.class)
            .withUdfClass(MyClass2.class)
            .build();

        assertEquals("99 - myName1",
            Eel.compile(context, "$my.random() - $my.stateful()").evaluate().asText(),
            "Unexpected");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UDF_LoadPackage() {
        EelContext context = EelContext.factory()
            .withUdfPackage(MyClass1.class.getPackage())    // Any of the classes in the package could have been used
            .build();

        assertEquals("99",
            Eel.compile(context, "$my.random()").evaluate().asText(),
            "Unexpected");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_TextPassThrough() {
        assertEquals("this is an expression",
            Eel.compile("this is an expression").evaluate().asText(),
            "Text pass through");

        assertEquals("Hello\nWorld",
            Eel.compile("Hello\\nWorld").evaluate().asText(),
            "with new line");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ForcingTheResultType() {
        assertEquals(Type.NUMBER,
            Eel.compile("$number( ${#myValue--1} )").evaluate().getType(),
            "unexpected type");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_PathsWithCommonRoot() {
        assertEquals("/my/path/config",
            Eel.compile("${root}/config").evaluate(someData).asText(),
            "${root}/config");
        assertEquals("/my/path/template",
            Eel.compile("${root}/template").evaluate(someData).asText(),
            "${root}/template");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CallingFunctions() {
        String actual;

        actual = Eel.compile("Next week is $date.local( \"7d\" )").evaluate().asText();
        assertTrue(actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"),
            "Next week is $date.local( \"7d\" ) returned: " + actual);

        actual = Eel.compile("Next week is $date.set( date.local(\"7d\"), \"@d\" )").evaluate().asText();
        assertTrue(actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"),
            "Next week is $date.set( date.local(\"7d\"), \"@d\" ) returned: " + actual);

        actual = Eel.compile("Next week is $date.local( \"7d\", \"@d\" )").evaluate().asText();
        assertTrue(actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T00:00:00.*"),
            "Next week is $date.local( \"7d\", \"@d\" ) returned: " + actual);

        actual = Eel.compile("Next week is $format.local( \"yyyy-MM-dd\", \"7d\" )").evaluate().asText();
        assertTrue(actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}"),
            "Next week is $format.local( \"yyyy-MM-dd\", \"7d\" ) returned: " + actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Counters() {
        EelContext context = EelContext.factory().build();

        assertEquals(0,
            Eel.compile(context, "$count()").evaluate().asInt(),
            "$count()");

        assertEquals("First: 0, Second: 0",
            Eel.compile(context, "First: $count( \"first\" ), Second: $count( \"second\" )").evaluate().asText(),
            "First: $count( \"first\" ), Second: $count( \"second\" )");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CreatingSequenceOfFiles() {
        EelContext context = EelContext.factory().build();

        assertTrue(Eel.compile(context, "$system.temp()/${myFilePrefix-}$count().txt").evaluate(someData).asText()
                .matches(".+/myFile0.txt$"),
            "$system.temp()/${myFilePrefix-}$count().txt");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DateTimeBasedDirectories() {
        EelContext context = EelContext.factory().build();

        assertTrue(Eel.compile(context, "${root-}/$format.local(\"yyyy/MM/dd/HH/\")").evaluate(someData).asText()
                .matches("/my/path/\\d{4}/\\d{2}/\\d{2}/\\d{2}/"),
            "${root-}/$format.local(\"yyyy/MM/dd/HH/\")");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SeparatingRandomlyNamedFiles() {
        EelContext context = EelContext.factory().build();
        String actual = Eel.compile(context,
                "$( text.random(10, '0-9') ; $left( $[1], 4 ); '${root-}/$[2]/$[1].txt' )")
            .evaluate(someData)
            .asText();

        assertTrue(actual.matches("/my/path/(\\d{4})/\\1\\d{6}\\.txt"),
            "SeparatingRandomlyNamedFiles: " + actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ConvertingPaths() {
        SymbolsTable symbols = SymbolsTable.from(Map.of("root", "/my/path"));
        String path;

        path = Eel.compile("$replace( ${root-}, '\\\\', '/')").evaluate(symbols).asText();
        assertEquals("/my/path", path, "$replace( ${root-}, '\\\\', '/')");

        path = Eel.compile("$realPath( ${root-} )").evaluate(symbols).asText();
        assertTrue(path.matches("(.:)?[/\\\\]my[/\\\\]path"), "$realPath( ${root-} ) = " + path);

        path = Eel.compile("$realPath( ${root-} ~> \"/\" ~> format.local(\"yyyy/MM/dd/HH/\") )").evaluate(symbols).asText();
        assertTrue(path.matches("(.:)?[/\\\\]my[/\\\\]path[/\\\\]\\d{4}[/\\\\]\\d{2}[/\\\\]\\d{2}[/\\\\]\\d{2}"),
            "$realPath( ${root-} ~> \"/\" ~> format.local(\"yyyy/MM/dd/HH/\") ) = " + path);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DirectoriesListings() throws Exception {
        File first = new File(tempFolder, "first.tmp");
        SymbolsTable symbols = SymbolsTable.from(Map.of("myPath", tempFolder.getCanonicalPath()));

        first.createNewFile();

        assertEquals(first.getCanonicalPath(),
            Eel.compile("$firstModified( ${myPath} )").evaluate(symbols).asText(),
            "$firstModified( ${myPath} )");

        File second = new File(tempFolder, "second.txt");
        second.createNewFile();

        assertEquals(second.getCanonicalPath(),
            Eel.compile("$firstModified( ${myPath}, \"*.txt\" )").evaluate(symbols).asText(),
            "$firstModified( ${myPath}, \"*.txt\" )");

        assertEquals(second.getCanonicalPath(),
            Eel.compile("$( firstModified( ${myPath}, \"*.txt\", count() ) )").evaluate(symbols).asText(),
            "$( firstModified( ${myPath}, \"*.txt\", count() ) )");

        String result;
        int count = -1;

        do {
            result = Eel.compile("$( firstModified( ${myPath}, \"*\", count(), \"\" ) )").evaluate(symbols).asText();
            count++;
        } while (!result.isEmpty() && (count < 999));

        assertEquals(1, count, "Unexpected number of files found");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logging() {
        Result result;

        result = Eel.compile("$log.info( ${myValue-Not Set} )").evaluate();
        assertEquals("Not Set",
            result.asText(),
            "message#1");
        assertEquals(Type.TEXT,
            result.getType(),
            "Message#1 type");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: Not Set\n"),
            "Message#1 not logged");

        result = Eel.compile("$log.info( \"The value is {}\", ${myValue-not set} )").evaluate();
        assertEquals("not set",
            result.asText(),
            "Message#2");
        assertEquals(Type.TEXT,
            result.getType(),
            "Message#2 type");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: The value is not set\n"),
            "Message#2 not logged");

        result = Eel.compile("$log.info( \"Evaluating {} + {} = {}\", 1, 2, ( 1 + 2 ) )").evaluate();
        assertEquals("3",
            result.asText(),
            "Message#3");
        assertEquals(Type.NUMBER,
            result.getType(),
            "Message#3 type");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: Evaluating 1 + 2 = 3\n"),
            "Message#3 not logged");

        result = Eel.compile("${myValue-$log.warn( \"myValue is not set\" )}").evaluate();
        assertEquals("myValue is not set",
            result.asText(),
            "Message#4");
        assertEquals(Type.TEXT,
            result.getType(),
            "Message#4 type");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: myValue is not set\n"),
            "Message#4 not logged");

        result = Eel.compile("$log.info( \"{} {}\", \"Hello\", \"World\", 99 )").evaluate();
        assertEquals("99",
            result.asText(),
            "Message#5");
        assertEquals(Type.NUMBER,
            result.getType(),
            "Message#5 type");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: Hello World\n"),
            "Message#5 not logged");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FailingExpressions() {
        Exception actual;

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile("$( ${myValue-} ; isEmpty( $[1] ) ? fail() : $[1] )").evaluate(),
            "Test1");
        assertEquals("",
            actual.getMessage(),
            "Unexpected message#1");

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile("${myValue-$fail(\"Custom Message\")}").evaluate(),
            "Test2");
        assertEquals("Custom Message",
            actual.getMessage(),
            "Unexpected message#2");

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile("$( not myValue1? or not myValue2? ? fail() : ${myValue1} ~> ${myValue2} )").evaluate(),
            "Test3");
        assertEquals("",
            actual.getMessage(),
            "Unexpected message#3");

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile("$( eel.version() >= 99.9 ? 0 : fail(\"Invalid EEL Version\") )").evaluate(),
            "Test4");
        assertEquals("Invalid EEL Version",
            actual.getMessage(),
            "Unexpected message#4");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FunctionsThatReturnDefaultValues() {
        Result result;
        EelFailException exception;

        assertFalse(Eel.compile("$indexOf( 'abcdef', 'z' )").evaluate().asLogic(),
            "$indexOf( 'abcdef', 'z' )");

        assertEquals(0,
            Eel.compile("$indexOf( 'abcdef', 'z', 0 )").evaluate().asInt(),
            "$indexOf( 'abcdef', 'z', 0 )");

        exception = assertThrows(EelFailException.class,
            () -> Eel.compile("$indexOf( 'abcdef', 'z', fail() )").evaluate(),
            "$indexOf( 'abcdef', 'z', fail() )");
        assertEquals("",
            exception.getMessage(),
            "Unexpected message");

        exception = assertThrows(EelFailException.class,
            () -> Eel.compile("$indexOf( 'abcdef', 'z', fail('There is no z') )").evaluate(),
            "$indexOf( 'abcdef', 'z', fail('There is no z') )");
        assertEquals("There is no z",
            exception.getMessage(),
            "Unexpected message");

        result = Eel.compile("$indexOf('abcdef', 'z', log.warn('There is no z, returning {}', 0) )").evaluate();
        assertEquals("0",
            result.asText(),
            "value");
        assertEquals(Type.NUMBER,
            result.getType(),
            "type");
        assertTrue(stdOut.getLinesNormalized().contains("There is no z, returning 0\n"),
            "Message not logged");

        assertEquals(3,
            Eel.compile("$indexOf( 'abcdef', 'd', fail() )").evaluate().asInt(),
            "$indexOf( 'abcdef', 'd', fail() )");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DatesOperations() throws Exception {
        long start = ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond();
        EelContext context = EelContext.factory().build();
        File file = new File(tempFolder, "file.tmp");
        SymbolsTable symbols = SymbolsTable.from(Map.of("myFile", file.getCanonicalPath()));
        Result result;

        file.createNewFile();

        result = Eel.compile(context, "$( date.utc() + 5 )").evaluate();
        assertTrue(((result.asLong() == start + 5) || (result.asLong() == start + 6)),
            "$( date.utc() + 5 )");

        result = Eel.compile(context, "$( date.utc() - date.start() )").evaluate();
        assertTrue(((result.asLong() == 0) || (result.asLong() == 1)),
            "$( date.utc() - date.start() )");

        result = Eel.compile(context, "$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )")
            .evaluate(symbols);
        assertFalse(result.asLogic(),
            "$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CompoundExpressions() {
        SymbolsTable symbols = SymbolsTable.from(Map.of("Key", "ABCdefGHI"));
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();

        assertEquals("Abcdefghi",
            Eel.compile(context, "$( ${Key,,^-} ; isEmpty( $[1] ) ? fail('no text') : $[1] )").evaluate(symbols).asText(),
            "Not DRY");

        assertEquals("abc",
            Eel.compile(context, "$( 'a' ; $[1] ~> 'b' ; $[2] ~> 'c' )")
                .evaluate(symbols)
                .asText(),
            "Backward references");

        assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( $[2] + $[3] ; 2 ; 3 ; $[1] )").evaluate(symbols),
            "Forward references");

        assertEquals("First = <a~b> and Second = <c~d>",
            Eel.compile(context, "First = $( 'a' ; 'b' ; '<$[1]~$[2]>' ) and Second = $( 'c' ; 'd' ; '<$[1]~$[2]>' )")
                .evaluate(symbols)
                .asText(),
            "Scope");

        assertThrows(EelSyntaxException.class,
            () -> Eel.compile(context, "$( count() ; $[1] + $( 2 ; $[1] + 3 ) )").evaluate(symbols),
            "Nested chains");

        // Evaluates, but the value is undefined
        Eel.compile(context, "$( count() ; count() ; count(); ($[2] ** $[3]) + $[1] )").evaluate(symbols);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CompoundExpressions_variableIndex() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();

        Eel expression = Eel.compile(context, "$( 'First'; 'Second'; 'Third' ; $[($count() + 1)] )");

        assertEquals("First", expression.evaluate().asText(), "First Call. No default);");
        assertEquals("Second", expression.evaluate().asText(), "Second Call. No default);");
        assertEquals("Third", expression.evaluate().asText(), "Third Call. No default);");
        assertThrows(EelSemanticException.class, () -> expression.evaluate().asText(), "Fourth Call. No default);");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CompoundExpressions_variableIndexWithDefault() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();

        Eel expression = Eel.compile(context, "$( 'First'; 'Second'; 'Third' ; $[($count() + 1)-$log.info('Not found', '3+')] )");

        assertEquals("First", expression.evaluate().asText(), "First Call. With default);");
        assertEquals("Second", expression.evaluate().asText(), "Second Call. With default);");
        assertEquals("Third", expression.evaluate().asText(), "Third Call. With default);");
        assertEquals("3+", expression.evaluate().asText(), "Fourth Call. With default);");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ChoosingValues() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Map<String, String> data = Map.ofEntries(
            entry("logicExpression", "true"),
            entry("numericExpression", "2"),
            entry("search", "test3"),
            entry("test1", "false"),
            entry("test2", "0"),
            entry("test3", "true")
        );
        SymbolsTable symbols = SymbolsTable.from(data);


        assertEquals("trueValue",
            Eel.compile(context, "$( ${logicExpression} ? 'trueValue' : 'falseValue' )").evaluate(symbols).asText(),
            "simple selection");
        assertEquals("value2",
            Eel.compile(context, "$( 'value1'; 'value2'; 'value3'; $[ ${numericExpression} ] )").evaluate(symbols).asText(),
            "numeric selection");
        assertEquals("value2",
            Eel.compile(context, "$( 'value1'; 'value2'; 'value3'; $[ ${numericExpression}-defaultValue] )").evaluate(symbols).asText(),
            "numeric selection with default");
        assertEquals("value3",
            Eel.compile(context, "$( 'value1'; 'value2'; 'value3'; $[text.index( ${search}, 'test1', 'test2', 'test3' )-defaultValue] )").evaluate(symbols).asText(),
            "text.index");
        assertEquals("value3",
            Eel.compile(context, "$( 'value1'; 'value2'; 'value3'; $[logic.index( ${test1}, ${test2}, ${test3} )-defaultValue] )").evaluate(symbols).asText(),
            "text.index");
    }


    private void testExpression(@Nonnull String expression,
                                @Nonnull Type expectedType,
                                @Nonnull String expectedText) {
        Result result = Eel.compile(expression)
            .evaluate(someData);

        assertResult("Expression '" + expression + "'", expectedType, expectedText, result);
    }

    private void assertResult(@Nonnull String message,
                              @Nonnull Type expectedType,
                              @Nonnull String expectedText,
                              @Nonnull Result actual) {
        assertEquals(expectedType, actual.getType(), message + ": Unexpected Type");
        assertEquals(expectedText, actual.asText(), message + ": Unexpected Value");
    }
}
