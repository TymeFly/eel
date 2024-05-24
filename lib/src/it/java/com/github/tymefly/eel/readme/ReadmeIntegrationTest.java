package com.github.tymefly.eel.readme;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Result;
import com.github.tymefly.eel.SymbolsTable;
import com.github.tymefly.eel.Type;
import com.github.tymefly.eel.exception.EelFailException;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import static java.util.Map.entry;

/**
 * Code taken from the readme.md file
 */
public class ReadmeIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    private static final String someExpression = "$( 0 + 1 )";
    private static final Map<String, String> someData = Map.ofEntries(
        entry("key", "myValue"),
        entry("second", "2nd"),
        entry("root", "/my/path"),
        entry("myFilePrefix", "myFile"),
        entry("STR", "abc~0123")
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
            stdOut.getLog().contains("As a number 1"));
        Assert.assertFalse("Unexpected Logic message",
            stdOut.getLog().contains("As logic"));
        Assert.assertFalse("Unexpected Date message",
            stdOut.getLog().contains("As logic"));
        Assert.assertTrue("Missing String message",
            stdOut.getLog().contains("As a string 1"));
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
        Assert.assertEquals("unexpected result",
            "this is an expression",
            Eel.compile("this is an expression").evaluate().asText());
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
        Assert.assertTrue("Last week was $date.local( \"-7d\" )",
            Eel.compile("Last week was $date.local( \"-7d\" )").evaluate().asText()
                .matches("Last week was \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));

        Assert.assertTrue("Last week was $date.truncate( date.local( \"-7d\" ), \"d\" )",
            Eel.compile("Last week was $date.truncate( date.local( \"-7d\" ), \"d\" )").evaluate().asText()
                .matches("Last week was \\d{4}-\\d{2}-\\d{2}T00:00:00.*"));

        Assert.assertTrue("Last week was $format.local( \"yyyy-MM-dd\", \"-7d\" )",
            Eel.compile("Last week was $format.local( \"yyyy-MM-dd\", \"-7d\" )").evaluate().asText()
                .matches("Last week was \\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Counters() {
        EelContext context = EelContext.factory().build();

        Assert.assertEquals("$count()",
            0,
            Eel.compile(context, "$count()").evaluate().asNumber().intValue());

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
        SymbolsTable symbols = SymbolsTable.from(Map.of("myPath", tempFolder.getRoot().getAbsolutePath()));

        Assert.assertEquals("$firstModified( ${myPath} )",
            first.getAbsolutePath(),
            Eel.compile("$firstModified( ${myPath} )").evaluate(symbols).asText());

        File second = tempFolder.newFile("second.txt");

        Assert.assertEquals("$firstModified( ${myPath}, \"*.txt\" )",
            second.getAbsolutePath(),
            Eel.compile("$firstModified( ${myPath}, \"*.txt\" )").evaluate(symbols).asText());

        Assert.assertEquals("$( firstModified( ${myPath}, \"*.txt\", count() ) )",
            second.getAbsolutePath(),
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
            stdOut.getLogWithNormalizedLineSeparator().contains("Logged EEL Message: Not Set\n"));

        result = Eel.compile("$log.info( \"The value is {}\", ${myValue-not set} )").evaluate();
        Assert.assertEquals("Message#2",
            "not set",
            result.asText());
        Assert.assertEquals("Message#2 type",
            Type.TEXT,
            result.getType());
        Assert.assertTrue("Message#2 not logged",
            stdOut.getLogWithNormalizedLineSeparator().contains("Logged EEL Message: The value is not set\n"));

        result = Eel.compile("$log.info( \"Evaluating {} + {} = {}\", 1, 2, ( 1 + 2 ) )").evaluate();
        Assert.assertEquals("Message#3",
            "3",
            result.asText());
        Assert.assertEquals("Message#3 type",
            Type.NUMBER,
            result.getType());
        Assert.assertTrue("Message#3 not logged",
            stdOut.getLogWithNormalizedLineSeparator().contains("Logged EEL Message: Evaluating 1 + 2 = 3\n"));

        result = Eel.compile("${myValue-$log.warn( \"myValue is not set\" )}").evaluate();
        Assert.assertEquals("Message#4",
            "myValue is not set",
            result.asText());
        Assert.assertEquals("Message#4 type",
            Type.TEXT,
            result.getType());
        Assert.assertTrue("Message#4 not logged",
            stdOut.getLogWithNormalizedLineSeparator().contains("Logged EEL Message: myValue is not set\n"));

        result = Eel.compile("$log.info( \"{} {}\", \"Hello\", \"World\", 99 )").evaluate();
        Assert.assertEquals("Message#5",
            "99",
            result.asText());
        Assert.assertEquals("Message#5 type",
            Type.NUMBER,
            result.getType());
        Assert.assertTrue("Message#5 not logged",
            stdOut.getLogWithNormalizedLineSeparator().contains("Logged EEL Message: Hello World\n"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FailingExpressions() {
        Exception actual;

        actual = Assert.assertThrows("Test1",
            EelFailException.class,
            () -> Eel.compile("$( isEmpty( ${myValue-} ) ? fail() : ${myValue} )").evaluate());
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
            Eel.compile("$indexOf( 'abcdef', 'z', 0 )").evaluate().asNumber().intValue());

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
            stdOut.getLogWithNormalizedLineSeparator().contains("There is no z, returning 0\n"));

        Assert.assertEquals("$indexOf( 'abcdef', 'd', fail() )",
            3,
            Eel.compile("$indexOf( 'abcdef', 'd', fail() )").evaluate().asNumber().intValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_DatesOperations() throws Exception {
        long start = ZonedDateTime.now(ZoneId.of("UTC")).toEpochSecond();
        EelContext context = EelContext.factory().build();
        File file = tempFolder.newFile("file.tmp");
        SymbolsTable symbols = SymbolsTable.from(Map.of("myFile", file.getAbsolutePath()));
        Result result;

        result = Eel.compile(context, "$( date.utc() + 5 )").evaluate();
        Assert.assertTrue("$( date.utc() + 5 )",
            ((result.asNumber().longValue() == start + 5) || (result.asNumber().longValue() == start + 6)));

        result = Eel.compile(context, "$( date.utc() - date.start() )").evaluate();
        Assert.assertTrue("$( date.utc() - date.start() )",
            ((result.asNumber().longValue() == 0) || (result.asNumber().longValue() == 1)));

        result = Eel.compile(context, "$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )")
            .evaluate(symbols);
        Assert.assertFalse("$( duration( modifiedAt( ${myFile} ), date.local(), \"months\" ) > 6 )",
            result.asLogic());
    }
}
