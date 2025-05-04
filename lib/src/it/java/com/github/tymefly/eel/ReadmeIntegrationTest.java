package com.github.tymefly.eel;

import java.io.File;
import java.io.InputStream;
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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

import static java.util.Map.entry;

/**
 * Code taken from the readme.md file
 */
public class ReadmeIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertEquals("Unexpected result", "1", result);
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

        Assert.assertTrue("Missing Number message",
            stdOut.getLinesNormalized().contains("As a number 1"));
        Assert.assertFalse("Unexpected Logic message",
            stdOut.getLinesNormalized().contains("As logic"));
        Assert.assertFalse("Unexpected Date message",
            stdOut.getLinesNormalized().contains("As logic"));
        Assert.assertTrue("Missing String message",
            stdOut.getLinesNormalized().contains("As a string 1"));
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
    public void test_comments_closed() {
        Result result = Eel.compile("$random( 10, ## upper-limit ## 99 )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.NUMBER, result.getType());
        Assert.assertTrue("Unexpected Value: " + result.asText(), result.asText().matches("[1-9][0-9]"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_comments_trailing() {
        InputStream source = getClass().getResourceAsStream("/comment.eel");
        Result result = Eel.factory()
            .compile(source)
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.NUMBER, result.getType());
        Assert.assertTrue("Unexpected Value: " + result.asText(), result.asText().matches("[1-9][0-9](\\.5)?"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_operator_notes() {
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

        Assert.assertThrows("${a}", EelUnknownSymbolException.class, () -> Eel.compile("${a}").evaluate(symbols));
        Assert.assertEquals("${m1.a}", "Map1 value a", Eel.compile("${m1.a}").evaluate(symbols).asText());
        Assert.assertEquals("${m2.b}", "Map2 value b", Eel.compile("${m2.b}").evaluate(symbols).asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ValueInterpolation_modifiers() {
        Assert.assertEquals("${key}", "myValue", Eel.compile("${key}").evaluate(someData).asText());
        Assert.assertEquals("${#key}", "7", Eel.compile("${#key}").evaluate(someData).asText());
        Assert.assertEquals("${key^}", "MyValue", Eel.compile("${key^}").evaluate(someData).asText());
        Assert.assertEquals("${key^^}", "MYVALUE", Eel.compile("${key^^}").evaluate(someData).asText());
        Assert.assertEquals("${key,}", "myValue", Eel.compile("${key,}").evaluate(someData).asText());
        Assert.assertEquals("${key,,}", "myvalue", Eel.compile("${key,,}").evaluate(someData).asText());
        Assert.assertEquals("${key~}", "MyValue", Eel.compile("${key~}").evaluate(someData).asText());
        Assert.assertEquals("${key~~}", "MYvALUE", Eel.compile("${key~~}").evaluate(someData).asText());
        Assert.assertEquals("${key:offset:count}", "Val", Eel.compile("${key:2:3}").evaluate(someData).asText());
        Assert.assertEquals("${key-default}", "myValue", Eel.compile("${key-default}").evaluate(someData).asText());

        Assert.assertEquals("${key,,^}", "Myvalue", Eel.compile("${key,,^}").evaluate(someData).asText());
        Assert.assertEquals("${key^^-default}", "MYVALUE", Eel.compile("${key^^-default}").evaluate(someData).asText());
        Assert.assertEquals("${key:0:3,,}", "myv", Eel.compile("${key:0:3,,}").evaluate(someData).asText());
        Assert.assertEquals("${key:2:3^}", "Val", Eel.compile("${key:2:3^}").evaluate(someData).asText());
        Assert.assertEquals("${#key-default}", "7", Eel.compile("${#key-default}").evaluate(someData).asText());
        Assert.assertEquals("${key:0:3:-default}", "myV", Eel.compile("${key:0:3:-default}").evaluate(someData).asText());

        Assert.assertEquals("${undefined-defaultText}", "defaultText", Eel.compile("${undefined-defaultText}").evaluate(someData).asText());
        Assert.assertEquals("${undefined-}", "", Eel.compile("${undefined-}").evaluate(someData).asText());
        Assert.assertEquals("${first-${second}}", "2nd", Eel.compile("${first-${second}}").evaluate(someData).asText());
        Assert.assertEquals("${first-${second-defaultText}}", "2nd", Eel.compile("${first-${second-defaultText}}").evaluate(someData).asText());
        Assert.assertEquals("${undefined-$myFunction()}", "299792458", Eel.compile("${undefined-$number.c()}").evaluate(someData).asText());
        Assert.assertEquals("${undefined-$( expression )}", "hello", Eel.compile("${undefined-$( 'hello' )}").evaluate(someData).asText());
        Assert.assertEquals("${STR:$(indexOf(${STR}, '~', fail()) + 1):1}",
            "0",
            Eel.compile("${STR:$(indexOf(${STR}, '~', fail()) + 1):1}").evaluate(someData).asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Lazy() {
        Assert.assertEquals("Value interpolation might not evaluate the default",
            "Undefined",
            Eel.compile("${myVariable-$log.error('Undefined')}").evaluate(someData).asText());

        Assert.assertEquals("The conditional operator will only evaluate one path",
            "Undefined",
            Eel.compile("$( myVariable? ? ${myVariable} : log.error('Undefined') )").evaluate(someData).asText());

        Assert.assertEquals("The logic operators are short-circuited - or",
            "true",
            Eel.compile("$( true or (count() = 10) )").evaluate(someData).asText());
        Assert.assertEquals("The logic operators are short-circuited - and",
            "false",
            Eel.compile("$( false and (count() = 10) )").evaluate(someData).asText());

        Assert.assertEquals("Function arguments might not be evaluated - no default",
            "-1",
            Eel.compile("$indexOf(${myVariable-}, 'x')").evaluate(someData).asText());
        Assert.assertEquals("Function arguments might not be evaluated - with default",
            "99",
            Eel.compile("$indexOf( ${myVariable-}, 'x', log.error('Undefined', 99) )").evaluate(someData).asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UDF() {
        EelContext context = EelContext.factory()
            .withUdfClass(Half.class)
            .build();

        Assert.assertEquals("divide.by2( 1234 )", "617", Eel.compile(context, "$divide.by2( 1234 )").evaluate().asText());
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

        Assert.assertEquals("Unexpected",
            "99 - myName1",
            Eel.compile(context, "$my.random() - $my.stateful()").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UDF_LoadPackage() {
        EelContext context = EelContext.factory()
            .withUdfPackage(MyClass1.class.getPackage())    // Any of the classes in the package could have been used
            .build();

        Assert.assertEquals("Unexpected",
            "99",
            Eel.compile(context, "$my.random()").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_TextPassThrough() {
        Assert.assertEquals("Text pass through",
            "this is an expression",
            Eel.compile("this is an expression").evaluate().asText());

        Assert.assertEquals("with new line",
            "Hello\nWorld",
            Eel.compile("Hello\\nWorld").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ForcingTheResultType() {
        Assert.assertEquals("unexpected type",
            Type.NUMBER,
            Eel.compile("$number( ${#myValue--1} )").evaluate().getType());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_PathsWithCommonRoot() {
        Assert.assertEquals("${root}/config",
            "/my/path/config",
            Eel.compile("${root}/config").evaluate(someData).asText());
        Assert.assertEquals("${root}/template",
            "/my/path/template",
            Eel.compile("${root}/template").evaluate(someData).asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CallingFunctions() {
        String actual;

        actual = Eel.compile("Next week is $date.local( \"7d\" )").evaluate().asText();
        Assert.assertTrue("Next week is $date.local( \"7d\" ) returned: " + actual,
            actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));

        actual = Eel.compile("Next week is $date.set( date.local(\"7d\"), \"@d\" )").evaluate().asText();
        Assert.assertTrue("Next week is $date.set( date.local(\"7d\"), \"@d\" ) returned: " + actual,
            actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));

        actual = Eel.compile("Next week is $date.local( \"7d\", \"@d\" )").evaluate().asText();
        Assert.assertTrue("Next week is $date.local( \"7d\", \"@d\" ) returned: " + actual,
            actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}T00:00:00.*"));

        actual = Eel.compile("Next week is $format.local( \"yyyy-MM-dd\", \"7d\" )").evaluate().asText();
        Assert.assertTrue("Next week is $format.local( \"yyyy-MM-dd\", \"7d\" ) returned: " + actual,
            actual.matches("Next week is \\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Counters() {
        EelContext context = EelContext.factory().build();

        Assert.assertEquals("$count()",
            0,
            Eel.compile(context, "$count()").evaluate().asInt());

        Assert.assertEquals("First: $count( \"first\" ), Second: $count( \"second\" )",
            "First: 0, Second: 0",
            Eel.compile(context, "First: $count( \"first\" ), Second: $count( \"second\" )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_CreatingSequenceOfFiles() {
        EelContext context = EelContext.factory().build();

        Assert.assertTrue("$system.temp()/${myFilePrefix-}$count().txt",
            Eel.compile(context, "$system.temp()/${myFilePrefix-}$count().txt").evaluate(someData).asText()
                .matches(".+/myFile0.txt$"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DateTimeBasedDirectories() {
        EelContext context = EelContext.factory().build();

        Assert.assertTrue("${root-}/$format.local(\"yyyy/MM/dd/HH/\")",
            Eel.compile(context, "${root-}/$format.local(\"yyyy/MM/dd/HH/\")").evaluate(someData).asText()
                .matches("/my/path/\\d{4}/\\d{2}/\\d{2}/\\d{2}/"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SeparatingRandomlyNamedFiles() {
        EelContext context = EelContext.factory().build();
        String actual = Eel.compile(context,
                "$( text.random(10, '0-9') ; ${root-} ~> '/' ~> right( $[1], 4 ) ~> '/' ~> $[1] ~> '.txt' )")
            .evaluate(someData)
            .asText();

        Assert.assertTrue("SeparatingRandomlyNamedFiles: " + actual,
            actual.matches("/my/path/(\\d{4})/\\d{6}\\1\\.txt"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ConvertingPaths() {
        SymbolsTable symbols = SymbolsTable.from(Map.of("root", "\\my\\path"));

        Assert.assertEquals("$replace( ${root-}, '\\\\', '/')",
            "/my/path",
            Eel.compile("$replace( ${root-}, '\\\\', '/')").evaluate(symbols).asText());

        Assert.assertTrue("$realPath( ${root-} )",
            Eel.compile("$realPath( ${root-} )").evaluate(symbols).asText()
                .matches("(.:)?[/\\\\]my[/\\\\]path"));

        Assert.assertTrue("$realPath( ${root-} ~> \"/\" ~> format.local(\"yyyy/MM/dd/HH/\") )",
            Eel.compile("$realPath( ${root-} ~> \"/\" ~> format.local(\"yyyy/MM/dd/HH/\") )").evaluate(symbols).asText()
                .matches("(.:)?[/\\\\]my[/\\\\]path[/\\\\]\\d{4}[/\\\\]\\d{2}[/\\\\]\\d{2}[/\\\\]\\d{2}"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DirectoriesListings() throws Exception {
        File first = tempFolder.newFile("first.tmp");
        SymbolsTable symbols = SymbolsTable.from(Map.of("myPath", tempFolder.getRoot().getCanonicalPath()));

        Assert.assertEquals("$firstModified( ${myPath} )",
            first.getCanonicalPath(),
            Eel.compile("$firstModified( ${myPath} )").evaluate(symbols).asText());

        File second = tempFolder.newFile("second.txt");

        Assert.assertEquals("$firstModified( ${myPath}, \"*.txt\" )",
            second.getCanonicalPath(),
            Eel.compile("$firstModified( ${myPath}, \"*.txt\" )").evaluate(symbols).asText());

        Assert.assertEquals("$( firstModified( ${myPath}, \"*.txt\", count() ) )",
            second.getCanonicalPath(),
            Eel.compile("$( firstModified( ${myPath}, \"*.txt\", count() ) )").evaluate(symbols).asText());

        String result;
        int count = -1;

        do {
            result = Eel.compile("$( firstModified( ${myPath}, \"*\", count(), \"\" ) )").evaluate(symbols).asText();
            count++;
        } while (!result.isEmpty() && (count < 999));

        Assert.assertEquals("Unexpected number of files found", 1, count);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logging() {
        Result result;

        result = Eel.compile("$log.info( ${myValue-Not Set} )").evaluate();
        Assert.assertEquals("message#1",
            "Not Set",
            result.asText());
        Assert.assertEquals("Message#1 type",
            Type.TEXT,
            result.getType());
        Assert.assertTrue("Message#1 not logged",
            stdOut.getLinesNormalized().contains("Logged EEL Message: Not Set\n"));

        result = Eel.compile("$log.info( \"The value is {}\", ${myValue-not set} )").evaluate();
        Assert.assertEquals("Message#2",
            "not set",
            result.asText());
        Assert.assertEquals("Message#2 type",
            Type.TEXT,
            result.getType());
        Assert.assertTrue("Message#2 not logged",
            stdOut.getLinesNormalized().contains("Logged EEL Message: The value is not set\n"));

        result = Eel.compile("$log.info( \"Evaluating {} + {} = {}\", 1, 2, ( 1 + 2 ) )").evaluate();
        Assert.assertEquals("Message#3",
            "3",
            result.asText());
        Assert.assertEquals("Message#3 type",
            Type.NUMBER,
            result.getType());
        Assert.assertTrue("Message#3 not logged",
            stdOut.getLinesNormalized().contains("Logged EEL Message: Evaluating 1 + 2 = 3\n"));

        result = Eel.compile("${myValue-$log.warn( \"myValue is not set\" )}").evaluate();
        Assert.assertEquals("Message#4",
            "myValue is not set",
            result.asText());
        Assert.assertEquals("Message#4 type",
            Type.TEXT,
            result.getType());
        Assert.assertTrue("Message#4 not logged",
            stdOut.getLinesNormalized().contains("Logged EEL Message: myValue is not set\n"));

        result = Eel.compile("$log.info( \"{} {}\", \"Hello\", \"World\", 99 )").evaluate();
        Assert.assertEquals("Message#5",
            "99",
            result.asText());
        Assert.assertEquals("Message#5 type",
            Type.NUMBER,
            result.getType());
        Assert.assertTrue("Message#5 not logged",
            stdOut.getLinesNormalized().contains("Logged EEL Message: Hello World\n"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FailingExpressions() {
        Exception actual;

        actual = Assert.assertThrows("Test1",
            EelFailException.class,
            () -> Eel.compile("$( ${myValue-} ; isEmpty( $[1] ) ? fail() : $[1] )").evaluate());
        Assert.assertEquals("Unexpected message#1",
            "",
            actual.getMessage());

        actual = Assert.assertThrows("Test2",
            EelFailException.class,
            () -> Eel.compile("${myValue-$fail(\"Custom Message\")}").evaluate());
        Assert.assertEquals("Unexpected message#2",
            "Custom Message",
            actual.getMessage());

        actual = Assert.assertThrows("Test3",
            EelFailException.class,
            () -> Eel.compile("$( not myValue1? or not myValue2? ? fail() : ${myValue1} ~> ${myValue2} )").evaluate());
        Assert.assertEquals("Unexpected message#3",
            "",
            actual.getMessage());

        actual = Assert.assertThrows("Test4",
            EelFailException.class,
            () -> Eel.compile("$( eel.version() >= 99.9 ? 0 : fail(\"Invalid EEL Version\") )").evaluate());
        Assert.assertEquals("Unexpected message#4",
            "Invalid EEL Version",
            actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FunctionsThatReturnDefaultValues() {
        Result result;
        EelFailException exception;

        Assert.assertFalse("$indexOf( 'abcdef', 'z' )",
            Eel.compile("$indexOf( 'abcdef', 'z' )").evaluate().asLogic());

        Assert.assertEquals("$indexOf( 'abcdef', 'z', 0 )",
            0,
            Eel.compile("$indexOf( 'abcdef', 'z', 0 )").evaluate().asInt());

        exception = Assert.assertThrows("$indexOf( 'abcdef', 'z', fail() )",
            EelFailException.class,
            () -> Eel.compile("$indexOf( 'abcdef', 'z', fail() )").evaluate());
        Assert.assertEquals("Unexpected message",
            "",
            exception.getMessage());

        exception = Assert.assertThrows("$indexOf( 'abcdef', 'z', fail('There is no z') )",
            EelFailException.class,
            () -> Eel.compile("$indexOf( 'abcdef', 'z', fail('There is no z') )").evaluate());
        Assert.assertEquals("Unexpected message",
            "There is no z",
            exception.getMessage());

        result = Eel.compile("$indexOf('abcdef', 'z', log.warn('There is no z, returning {}', 0) )").evaluate();
        Assert.assertEquals("value",
            "0",
            result.asText());
        Assert.assertEquals("type",
            Type.NUMBER,
            result.getType());
        Assert.assertTrue("Message not logged",
            stdOut.getLinesNormalized().contains("There is no z, returning 0\n"));

        Assert.assertEquals("$indexOf( 'abcdef', 'd', fail() )",
            3,
            Eel.compile("$indexOf( 'abcdef', 'd', fail() )").evaluate().asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DatesOperations() throws Exception {
        long start = ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond();
        EelContext context = EelContext.factory().build();
        File file = tempFolder.newFile("file.tmp");
        SymbolsTable symbols = SymbolsTable.from(Map.of("myFile", file.getCanonicalPath()));
        Result result;

        result = Eel.compile(context, "$( date.utc() + 5 )").evaluate();
        Assert.assertTrue("$( date.utc() + 5 )",
            ((result.asLong() == start + 5) || (result.asLong() == start + 6)));

        result = Eel.compile(context, "$( date.utc() - date.start() )").evaluate();
        Assert.assertTrue("$( date.utc() - date.start() )",
            ((result.asLong() == 0) || (result.asLong() == 1)));

        result = Eel.compile(context, "$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )")
            .evaluate(symbols);
        Assert.assertFalse("$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )",
            result.asLogic());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ChainedExpressions() {
        SymbolsTable symbols = SymbolsTable.from(Map.of("Key", "ABCdefGHI"));
        EelContext context = EelContext.factory().build();

        Assert.assertEquals("Not DRY",
            "Abcdefghi",
            Eel.compile(context, "$( ${Key,,^-} ; isEmpty( $[1] ) ? fail('no text') : $[1] )").evaluate(symbols).asText());

        Assert.assertEquals("Backward references",
            "abc",
            Eel.compile(context, "$( 'a' ; $[1] ~> 'b' ; $[2] ~> 'c' )")
                .evaluate(symbols)
                .asText());

        Assert.assertThrows("Forward references",
            EelSemanticException.class,
            () -> Eel.compile(context, "$( $[2] + $[3] ; 2 ; 3 ; $[1] * 10 )").evaluate(symbols));

        Assert.assertEquals("Lazy",
            0,
            Eel.compile(context, "$( count() ; count() ; $[2] + $[2] )").evaluate(symbols).asInt());

        Assert.assertEquals("Scope",
            "First = <a~b> and Second = <c~d>",
            Eel.compile(context, "First = $( 'a' ; 'b' ; '<$[1]~$[2]>' ) and Second = $( 'c' ; 'd' ; '<$[1]~$[2]>' )")
                .evaluate(symbols)
                .asText());

        Assert.assertThrows("Nested chains",
            EelSyntaxException.class,
            () -> Eel.compile(context, "$( count() ; $[1] + $( 2 ; $[1] + 3 ) )").evaluate(symbols));
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
        Assert.assertEquals(message + ": Unexpected Type", expectedType, actual.getType());
        Assert.assertEquals(message + ": Unexpected Value", expectedText, actual.asText());
    }
}
