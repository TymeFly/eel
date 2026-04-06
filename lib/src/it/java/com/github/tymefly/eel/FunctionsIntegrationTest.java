package com.github.tymefly.eel;

import java.io.File;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFailException;
import func.Delay;
import func.Echo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional testing each EEL function
 */
@ExtendWith(SystemStubsExtension.class)
public class FunctionsIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    private static final double TEST_DELTA = 0.00000001;

    private EelContext context;
    
    
    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withUdfPackage(Delay.class.getPackage())
            .withTimeout(Duration.ofSeconds(0))
            .withStartOfWeek(DayOfWeek.SUNDAY)
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_eelMetaData() {
        assertPattern("version", "$eel.version()", "\\d{1,2}\\.\\d+");
        assertPattern("buildDate", "$eel.buildDate()", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_text() {
        assertEquals("HELLO", Eel.compile(context, "$upper('hello')").evaluate().asText(), "upper");
        assertEquals("hello", Eel.compile(context, "$lower('HELLO')").evaluate().asText(), "lower");
        assertEquals("Hello World Again!! 01abcd~", Eel.compile(context, "$title('hello world AGAIN!! 01abCD~')").evaluate().asText(), "title");
        assertEquals("7", Eel.compile(context, "$len('  xxxx ')").evaluate().asText(), "len");
        assertTrue(Eel.compile(context, "$isEmpty('')").evaluate().asLogic(), "isEmpty - empty");
        assertFalse(Eel.compile(context, "$isEmpty(' ')").evaluate().asLogic(), "isEmpty - white");
        assertFalse(Eel.compile(context, "$isEmpty('abc')").evaluate().asLogic(), "isEmpty - text");
        assertTrue(Eel.compile(context, "$isBlank('')").evaluate().asLogic(), "isBlank - empty");
        assertTrue(Eel.compile(context, "$isBlank(' ')").evaluate().asLogic(), "isBlank - white");
        assertFalse(Eel.compile(context, "$isBlank('abc')").evaluate().asLogic(), "isBlank - text");
        assertEquals("xxxx", Eel.compile(context, "$trim('  xxxx ')").evaluate().asText(), "trim");
        assertEquals("abc", Eel.compile(context, "$left('abcdef', 3)").evaluate().asText(), "left");
        assertEquals("def", Eel.compile(context, "$right('abcdef', 3)").evaluate().asText(), "right");
        assertEquals("bc", Eel.compile(context, "$mid('abcdef', 1, 2)").evaluate().asText(), "mid");
        assertEquals("cdef", Eel.compile(context, "$mid('abcdef', 2 )").evaluate().asText(), "mid - default length");
        assertEquals("ab", Eel.compile(context, "$beforeFirst('ab.cd.ef', '.')").evaluate().asText(), "beforeFirst");
        assertEquals("cd.ef", Eel.compile(context, "$afterFirst('ab.cd.ef', '.')").evaluate().asText(), "afterFirst");
        assertEquals("ab.cd", Eel.compile(context, "$beforeLast('ab.cd.ef', '.')").evaluate().asText(), "beforeLast");
        assertEquals("ef", Eel.compile(context, "$afterLast('ab.cd.ef', '.')").evaluate().asText(), "afterLast");
        assertEquals("ab.cd", Eel.compile(context, "$before('ab.cd.ef.gh', '.', 2)").evaluate().asText(), "before");
        assertEquals("ef.gh", Eel.compile(context, "$after('ab.cd.ef.gh', '.', 2)").evaluate().asText(), "after");
        assertEquals(3, Eel.compile(context, "$contains('ab.cd.ef.gh', '.')").evaluate().asInt(), "contains");
        assertTrue(Eel.compile(context, "$matches(uuid(), '[0-9a-f-]{36}')").evaluate().asLogic(), "match");
        assertEquals("HelloWorld",
            Eel.compile(context, "$extract('?? ~Hello~  @World@ ??', '.*~([^~]*)~.*@([^@]*)@.*')").evaluate().asText(),
            "extract");
        assertEquals("he!!o", Eel.compile(context, "$replace('hello', 'l', '!')").evaluate().asText(), "replace");
        assertEquals("?ello", Eel.compile(context, "$replaceEx('hello', '^.', '?')").evaluate().asText(), "replaceAll");
        assertEquals(2, Eel.compile(context, "$indexOf('hello', 'l')").evaluate().asInt(), "indexOf");
        assertEquals(3, Eel.compile(context, "$lastIndexOf('hello', 'l')").evaluate().asInt(), "lastIndexOf");
        assertEquals("format:    Hello 12", Eel.compile(context, "$printf('format: %8.8s %d', 'Hello', 12)").evaluate().asText(), "printf");
        assertEquals("      myText", Eel.compile(context, "$padLeft('myText', 12)").evaluate().asText(), "padLeft");
        assertEquals("myText@@@@@@", Eel.compile(context, "$padRight('myText', 12, '@_')").evaluate().asText(), "padRight");

        assertEquals("a", Eel.compile(context, "$char(97)").evaluate().asText(), "char");
        assertEquals(126, Eel.compile(context, "$codepoint('~')").evaluate().asInt(), "codepoint");

        assertEquals(10, Eel.compile(context, "$text.random()").evaluate().asText().length(), "text.random");
        assertEquals(3, Eel.compile(context, "$text.index('x', 'a', 'b', 'x', 'y', 'z')").evaluate().asInt(), "text.index");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logic() {
        assertEquals(4, Eel.compile(context, "$logic.index( false, ( 1 < 0 ), -1, 1 )").evaluate().asInt(), "logic.index");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_stats() {
        assertEquals(new BigDecimal("5"), Eel.compile(context, "$max(-3, 1, 2, 5)").evaluate().asNumber(), "max");
        assertEquals(new BigDecimal("-3"), Eel.compile(context, "$min(-3, 1, 2, 5)").evaluate().asNumber(), "min");
        assertEquals(new BigDecimal("1.25"), Eel.compile(context, "$avg(-3, 1, 2, 5)").evaluate().asNumber(), "avg");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        // Sign tests and manipulation
        assertEquals(new BigDecimal("2.1"), Eel.compile(context, "$abs(-2.1)").evaluate().asNumber(), "Abs");
        assertEquals(new BigDecimal("-1"), Eel.compile(context, "$sgn(-2.1)").evaluate().asNumber(), "Sgn");

        // Conversion functions
        assertEquals(180, Eel.compile(context, "$toDegrees(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA, "toDegrees");
        assertEquals(3.14159265, Eel.compile(context, "$toRadians(180)").evaluate().asNumber().doubleValue(), TEST_DELTA, "toRadians");

        // Logarithms and exponential functions
        assertEquals(2.0, Eel.compile(context, "$log(100)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Log");
        assertEquals(4.605170185988091, Eel.compile(context, "$ln(100)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Ln");
        assertEquals(148.4131591025766, Eel.compile(context, "$exp(5)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Exp");
        assertEquals(7.0, Eel.compile(context, "$root(343, 3)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Root");

        // Trigonometry
        assertEquals(0, Eel.compile(context, "$sin(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA, "Sin");
        assertEquals(-1.0, Eel.compile(context, "$cos(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA, "Cos");
        assertEquals(0, Eel.compile(context, "$tan(2 * number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA, "Tan");
        assertEquals(0, Eel.compile(context, "$asin(0)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Asin");
        assertEquals(0, Eel.compile(context, "$asin(0)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Asin");
        assertEquals(1.570796326794897, Eel.compile(context, "$acos(0)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Acos");
        assertEquals(0.78539816, Eel.compile(context, "$atan(1)").evaluate().asNumber().doubleValue(), TEST_DELTA, "Atan");

        // Rounding functions
        assertEquals(new BigDecimal("6"), Eel.compile(context, "$number.truncate(6.5)").evaluate().asNumber(), "number.truncate");
        assertEquals(new BigDecimal("6.5"), Eel.compile(context, "$number.round(6.45, 1)").evaluate().asNumber(), "number.round");
        assertEquals(new BigDecimal("7"), Eel.compile(context, "$number.ceil(6.45)").evaluate().asNumber(), "number.ceil");
        assertEquals(new BigDecimal("6"), Eel.compile(context, "$number.floor(6.5)").evaluate().asNumber(), "number.floor");

        // Other maths functions
        assertEquals(720, Eel.compile(context, "$factorial(6)").evaluate().asInt(), "Factorial");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_newDate() {
        assertEquals("2001-02-03T04:05:06Z",
            Eel.compile(context, "$date.parse('yyyy-MM-dd HH:mm:ss z', '2001-02-03 04:05:06 GMT')")
                .evaluate()
                .asText(),
            "date.parse"
        );
        assertTrue(Eel.compile(context, "$date.utc()")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z"),
            "date.utc"
        );
        assertTrue(Eel.compile(context, "$date.utc('-200year')")
                .evaluate()
                .asText()
                .matches("18\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z"),
            "date.utc with offset"
        );
        assertTrue(Eel.compile(context, "$date.local('+1h')")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+"),
            "date.local"
        );
        assertTrue(Eel.compile(context, "$date.at('-5')")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?-05"),
            "date.at"
        );
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_manipulations() {
        assertEquals("2007-12-03T10:00:00Z",
            Eel.compile(context, "$date.plus( 1196676933, '@h')")
                .evaluate()
                .asText(),
            "offset - snap"
        );
        assertEquals("2007-12-03T07:15:42Z",
            Eel.compile(context, "$date.plus( 1196676933, '-3h', '+9s')")
                .evaluate()
                .asText(),
            "offset - plus"
        );
        assertEquals("2007-12-03T13:15:23.995Z",
            Eel.compile(context, "$date.minus( 1196676933, '-3h', '+9s', '5i')")
                .evaluate()
                .asText(),
            "offset - minus"
        );
        assertEquals("2007-12-03T03:05:01Z",
            Eel.compile(context, "$date.set( 1196676933, '3h', '5minute', '1s')")
                .evaluate()
                .asText(),
            "set"
        );
        assertEquals("2007-12-03T10:15:33-06",
            Eel.compile(context, "$date.setZone( 1196676933, '-6')")
                .evaluate()
                .asText(),
            "setZone"
        );
        assertEquals("2007-12-03T04:15:33-06",
            Eel.compile(context, "$date.moveZone( 1196676933, '-6')")
                .evaluate()
                .asText(),
            "moveZone"
        );
        assertEquals(14,
            Eel.compile(context, "$duration( 1196676933, 1235988933, 'months')")
                .evaluate()
                .asLong(),
            "duration"
        );
        assertEquals("2009-01-11T00:00:00Z",
            Eel.compile(context, "$date.set( '2009-03-05T10:11:12Z', '2w', '@w' )")
                .evaluate()
                .asText(),
            "set and snap weeks"
        );
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_date() {
        String date = Eel.compile(context, "$format.date( 'd/M/yyyy HH:mm', 1196676933, '4d', '-7minutes' )")
            .evaluate()
            .asText();
        assertEquals("7/12/2007 10:08", date, "format.date"
        );

        String start = Eel.compile(context, "$format.start( 'd/M/yyyy HH:mm', 'UTC', '+1h' )")
            .evaluate()
            .asText();
        assertTrue(start.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"), "format.start: " + start);

        String utc = Eel.compile(context, "$format.utc( 'd/M/yyyy HH:mm', '+2w' )")
            .evaluate()
            .asText();
        assertTrue(utc.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"), "format.utc: " + utc);

        String local = Eel.compile(context, "$format.local( 'd/M/yyyy HH:mm', '-3h' )")
            .evaluate()
            .asText();
        assertTrue(local.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"), "format.local: " + local);

        String at = Eel.compile(context, "$format.at( 'Europe/Paris', 'd/M/yyyy HH:mm', '+12h', '3m' )")
            .evaluate()
            .asText();
        assertTrue(at.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"), "format.at: " + at);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_number() {
        String binary = Eel.compile(context, "$format.binary( 0xa55a )")
            .evaluate()
            .asText();
        assertEquals("1010010101011010", binary, "format.binary");

        String octal = Eel.compile(context, "$format.octal( 1196676933 )")
            .evaluate()
            .asText();
        assertEquals("10724753505", octal, "format.octal");

        String hex = Eel.compile(context, "$format.hex( 1196676933 )")
            .evaluate()
            .asText();
        assertEquals("4753d745", hex, "format.hex");

        String radixThree = Eel.compile(context, "$format.number( 1196676933, 3 )")
            .evaluate()
            .asText();
        assertEquals("10002101202111010220", radixThree, "format.number");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_zoneCheck() {
        Map<String, String> symbols = Map.of("date", "1196676933");
        long utc = Eel.compile(context, "$( number(${date}) )")          // Convert Text -> Number -> Date
            .evaluate(symbols)
            .asLong();
        long setZone = Eel.compile(context, "$date.setZone( number(${date}), '+5')")
            .evaluate(symbols)
            .asLong();
        long moveZone = Eel.compile(context, "$date.moveZone( number(${date}), '+5')")
            .evaluate(symbols)
            .asLong();

        assertEquals(utc, moveZone, "moveZone() should NOT change time from UTC");
        assertEquals((utc - 18000), setZone, "setZone() SHOULD change time from UTC");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logging() {
        assertEquals("3", Eel.compile(context, "$log.trace( 1 + 2 )").evaluate().asText(), "trace");
        assertEquals("true", Eel.compile(context, "$log.trace( true or false )").evaluate().asText(), "debug");
        assertEquals("value", Eel.compile(context, "$log.info( 'message {}', 'value' )").evaluate().asText(), "info");
        assertEquals("false", Eel.compile(context, "$log.warn( 'message {}', ( 2=3 ) )").evaluate().asText(), "warn");
        assertEquals("ab", Eel.compile(context, "$log.error( 'message {} {}', 'test', 'a'~>'b' )").evaluate().asText(), "error");
        assertEquals("ab", Eel.compile(context, "$log.error( 'message {} {}', 'test', 'a'~>'b' )").evaluate().asText(), "error");
        assertEquals("Hello World!", Eel.compile(context, "$log.error( 'No data', 'Hello World!' )").evaluate().asText(), "pass through");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logging_filters() {
        assertEquals("Tab\tChar",
            Eel.compile(context, "$log.info( 'Tab\\tChar' )").evaluate().asText(),
            "Keep Tab - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: Tab\tChar"),
            "Keep Tab - Logged text: " + stdOut.getLinesNormalized());

        // Filter out the following escaped characters
        assertEquals("New\nLine",
            Eel.compile(context, "$log.info( 'New\\nLine' )").evaluate().asText(),
            "Skip NewLine - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: NewLine"),
            "Skip NewLine - Logged text: " + stdOut.getLinesNormalized());

        assertEquals("Carriage\rReturn",
            Eel.compile(context, "$log.info( 'Carriage\\rReturn' )").evaluate().asText(),
            "Skip CarriageReturn - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: CarriageReturn"),
            "Skip CarriageReturn - Logged text: " + stdOut.getLinesNormalized());

        assertEquals("Form\fFeed",
            Eel.compile(context, "$log.info( 'Form\\fFeed' )").evaluate().asText(),
            "Skip FormFeed - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: FormFeed"),
            "Skip FormFeed - Logged text: " + stdOut.getLinesNormalized());

        assertEquals("Back\bSpace",
            Eel.compile(context, "$log.info( 'Back\\bSpace' )").evaluate().asText(),
            "Skip Backspace - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Logged EEL Message: FormFeed"),
            "Skip Backspace - Logged text: " + stdOut.getLinesNormalized());

        assertEquals("Unicode: \u00A9 ~", Eel.compile(context, "$log.info( 'Unicode: \u00A9 ~' )").evaluate().asText(),
            "Unicode - pass through");
        assertTrue(stdOut.getLinesNormalized().contains("Unicode:  ~"),
            "Unicode - Logged text: " + stdOut.getLinesNormalized());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_system() {
        String fileSeparator = Eel.compile(context, "$system.fileSeparator()").evaluate().asText();
        String pwd = Eel.compile(context, "$system.pwd()").evaluate().asText();
        String home = Eel.compile(context, "$system.home()").evaluate().asText();
        String temp = Eel.compile(context, "$system.temp()").evaluate().asText();

        assertTrue("/".equals(fileSeparator) || "\\".equals(fileSeparator),
            "fileSeparator(): " + fileSeparator);
        assertTrue(pwd.matches("^([A-za-z]:)?[/\\\\]([^/\\\\]+[/\\\\])*"), "pwd");
        assertFalse(home.isEmpty(), "system.home() : " + home);      // Other tests have checked the value
        assertFalse(temp.isEmpty(), "system.temp() : " + temp);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileNames() {
        String path = (File.separatorChar =='/') ? "a/b/../c/" : "a\\\\b\\\\..\\\\c\\\\";

        assertEquals(System.getProperty("user.dir").replace('\\', '/') + "/a/c/d.txt",
            Eel.compile(context, "$realPath('" + path + "/d.txt')").evaluate().asText().replace('\\', '/'),
            "realPath");
        assertEquals("a/b/../c",
            Eel.compile(context, "$dirName('" + path + "d.txt')").evaluate().asText()
                .replace('\\', '/').replaceFirst("^.:", ""),
            "dirName");
        assertEquals("d",
            Eel.compile(context, "$baseName('" + path + "d.txt', '.txt')").evaluate().asText(),
            "baseName");
        assertEquals("tar.gz",
            Eel.compile(context, "$extension('" + path + "d.tar.gz')").evaluate().asText(),
            "extension");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileInfo() {
        assertTrue(Eel.compile(context, "$exists('pom.*')").evaluate().asLogic(), "POM exists");

        // Source Control will change the file attributes, so the best we can do is to
        // make sure these functions don't throw exceptions

        Eel.compile(context, "$createAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$accessedAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$modifiedAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$fileSize('pom.xml')").evaluate().asNumber();

        EelFailException actual = assertThrows(EelFailException.class,
            () -> Eel.compile(context, "$fileSize('missingFile', fail('Expected fileSize exception'))").evaluate());

        assertEquals("Expected fileSize exception", actual.getMessage(), "Unexpected Error Message"
        );
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_findFiles() {
        assertEquals(1, Eel.compile(context, "$fileCount('.', 'pom.xml')").evaluate().asLong(), "fileCount");

        // Source Control will set the attributes, so limit the search to a single file that must exist
        test_findFiles_helper("firstCreated", "$firstCreated('.', 'p?m.*')");
        test_findFiles_helper("lastCreated", "$lastCreated('.', 'p?m.*')");

        test_findFiles_helper("firstAccessed", "$firstAccessed('.', 'p?m.*')");
        test_findFiles_helper("lastAccessed", "$lastAccessed('.', 'p?m.*')");

        test_findFiles_helper("firstModified", "$firstModified('.', 'p?m.*')");
        test_findFiles_helper("lastModified", "$lastModified('.', 'p?m.*')");
    }

    private void test_findFiles_helper(@Nonnull String name, @Nonnull String expression) {
        String actual = Eel.compile(context, expression).evaluate().asText();

        assertTrue(actual.replace('\\', '/').endsWith("/pom.xml"), name + "returned: " + actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_io() {
        String textFile = getClass()
            .getClassLoader()
            .getResource("io-data.txt")
            .getFile();

        assertEquals("line 1", Eel.compile(context, "$io.head( '" + textFile + "', 1 )").evaluate().asText(), "head");
        assertEquals("line 3", Eel.compile(context, "$io.tail( '" + textFile + "', 1 )").evaluate().asText(), "tail");
    }

    /**
     * Integration test {@link Eel} with failures
     */
    @Test
    public void test_fail() {
        EelFailException actual;

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile(context, "$fail()").evaluate(),
            "No message");
        assertEquals("", actual.getMessage(), "No message - unexpected message");

        actual = assertThrows(EelFailException.class,
            () -> Eel.compile(context, "$fail('my Message!')").evaluate(),
            "With message");
        assertEquals("my Message!", actual.getMessage(), "With message - unexpected message");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_misc() {
        assertTrue(Eel.compile(context, "$random(10, 99)").evaluate().asText().matches("\\d{2}"), "random");
        assertTrue(Eel.compile(context, "$uuid()").evaluate().asText().matches("[0-9a-f-]{36}"), "uuid");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_failUdf() {
        context = EelContext.factory()
            .withUdfClass(Echo.class)
            .build();
        Eel eel = Eel.compile(context, "$test.echo(fail('test me'))");

        assertThrows(EelFailException.class, eel::evaluate);
    }



    private void assertPattern(@Nonnull String name, @Nonnull String expression, @Nonnull String expectedPattern) {
        String value = Eel.compile(context, expression)
            .evaluate()
            .asText();

        assertTrue(value.matches(expectedPattern),
            name + " was unexpected value. Was " + value + ", expected pattern " + expectedPattern
        );
    }
}
