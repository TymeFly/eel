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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Functional testing of each of the EEL functions
 */
public class FunctionsIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    private static final double TEST_DELTA = 0.00000001;

    private EelContext context;
    
    
    @Before
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
        Assert.assertEquals("upper", "HELLO", Eel.compile(context, "$upper('hello')").evaluate().asText());
        Assert.assertEquals("lower", "hello", Eel.compile(context, "$lower('HELLO')").evaluate().asText());
        Assert.assertEquals("title", "Hello World Again!! 01abcd~", Eel.compile(context, "$title('hello world AGAIN!! 01abCD~')").evaluate().asText());
        Assert.assertEquals("len", "7", Eel.compile(context, "$len('  xxxx ')").evaluate().asText());
        Assert.assertTrue("isEmpty - empty", Eel.compile(context, "$isEmpty('')").evaluate().asLogic());
        Assert.assertFalse("isEmpty - white", Eel.compile(context, "$isEmpty(' ')").evaluate().asLogic());
        Assert.assertFalse("isEmpty - text", Eel.compile(context, "$isEmpty('abc')").evaluate().asLogic());
        Assert.assertTrue("isBlank - empty", Eel.compile(context, "$isBlank('')").evaluate().asLogic());
        Assert.assertTrue("isBlank - white", Eel.compile(context, "$isBlank(' ')").evaluate().asLogic());
        Assert.assertFalse("isBlank - text", Eel.compile(context, "$isBlank('abc')").evaluate().asLogic());
        Assert.assertEquals("trim", "xxxx", Eel.compile(context, "$trim('  xxxx ')").evaluate().asText());
        Assert.assertEquals("left", "abc", Eel.compile(context, "$left('abcdef', 3)").evaluate().asText());
        Assert.assertEquals("right", "def", Eel.compile(context, "$right('abcdef', 3)").evaluate().asText());
        Assert.assertEquals("mid", "bc", Eel.compile(context, "$mid('abcdef', 1, 2)").evaluate().asText());
        Assert.assertEquals("mid - default length", "cdef", Eel.compile(context, "$mid('abcdef', 2 )").evaluate().asText());
        Assert.assertEquals("beforeFirst", "ab", Eel.compile(context, "$beforeFirst('ab.cd.ef', '.')").evaluate().asText());
        Assert.assertEquals("afterFirst", "cd.ef", Eel.compile(context, "$afterFirst('ab.cd.ef', '.')").evaluate().asText());
        Assert.assertEquals("beforeLast", "ab.cd", Eel.compile(context, "$beforeLast('ab.cd.ef', '.')").evaluate().asText());
        Assert.assertEquals("afterLast", "ef", Eel.compile(context, "$afterLast('ab.cd.ef', '.')").evaluate().asText());
        Assert.assertEquals("before", "ab.cd", Eel.compile(context, "$before('ab.cd.ef.gh', '.', 2)").evaluate().asText());
        Assert.assertEquals("after", "ef.gh", Eel.compile(context, "$after('ab.cd.ef.gh', '.', 2)").evaluate().asText());
        Assert.assertEquals("contains", 3, Eel.compile(context, "$contains('ab.cd.ef.gh', '.')").evaluate().asInt());
        Assert.assertTrue("match", Eel.compile(context, "$matches(uuid(), '[0-9a-f-]{36}')").evaluate().asLogic());
        Assert.assertEquals("extract",
            "HelloWorld",
            Eel.compile(context, "$extract('?? ~Hello~  @World@ ??', '.*~([^~]*)~.*@([^@]*)@.*')").evaluate().asText());
        Assert.assertEquals("replace", "he!!o", Eel.compile(context, "$replace('hello', 'l', '!')").evaluate().asText());
        Assert.assertEquals("replaceAll", "?ello", Eel.compile(context, "$replaceEx('hello', '^.', '?')").evaluate().asText());
        Assert.assertEquals("indexOf", 2, Eel.compile(context, "$indexOf('hello', 'l')").evaluate().asInt());
        Assert.assertEquals("lastIndexOf", 3, Eel.compile(context, "$lastIndexOf('hello', 'l')").evaluate().asInt());
        Assert.assertEquals("printf", "format:    Hello 12", Eel.compile(context, "$printf('format: %8.8s %d', 'Hello', 12)").evaluate().asText());
        Assert.assertEquals("padLeft", "      myText", Eel.compile(context, "$padLeft('myText', 12)").evaluate().asText());
        Assert.assertEquals("padRight", "myText@@@@@@", Eel.compile(context, "$padRight('myText', 12, '@_')").evaluate().asText());

        Assert.assertEquals("char", "a", Eel.compile(context, "$char(97)").evaluate().asText());
        Assert.assertEquals("codepoint", 126, Eel.compile(context, "$codepoint('~')").evaluate().asInt());

        Assert.assertEquals("text.random", 10, Eel.compile(context, "$text.random()").evaluate().asText().length());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_stats() {
        Assert.assertEquals("max", new BigDecimal("5"), Eel.compile(context, "$max(-3, 1, 2, 5)").evaluate().asNumber());
        Assert.assertEquals("min", new BigDecimal("-3"), Eel.compile(context, "$min(-3, 1, 2, 5)").evaluate().asNumber());
        Assert.assertEquals("avg", new BigDecimal("1.25"), Eel.compile(context, "$avg(-3, 1, 2, 5)").evaluate().asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        // Sign tests and manipulation
        Assert.assertEquals("Abs", new BigDecimal("2.1"), Eel.compile(context, "$abs(-2.1)").evaluate().asNumber());
        Assert.assertEquals("Sgn", new BigDecimal("-1"), Eel.compile(context, "$sgn(-2.1)").evaluate().asNumber());

        // Conversion functions
        Assert.assertEquals("toDegrees", 180, Eel.compile(context, "$toDegrees(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("toRadians", 3.14159265, Eel.compile(context, "$toRadians(180)").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Logarithms and exponential functions
        Assert.assertEquals("Log", 2.0, Eel.compile(context, "$log(100)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Ln", 4.605170185988091, Eel.compile(context, "$ln(100)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Exp", 148.4131591025766, Eel.compile(context, "$exp(5)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Root", 7.0, Eel.compile(context, "$root(343, 3)").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Trigonometry
        Assert.assertEquals("Sin", 0, Eel.compile(context, "$sin(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Cos", -1.0, Eel.compile(context, "$cos(number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Tan", 0, Eel.compile(context, "$tan(2 * number.pi())").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile(context, "$asin(0)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile(context, "$asin(0)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Acos", 1.570796326794897, Eel.compile(context, "$acos(0)").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Atan", 0.78539816, Eel.compile(context, "$atan(1)").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Rounding functions
        Assert.assertEquals("number.truncate", new BigDecimal("6"), Eel.compile(context, "$number.truncate(6.5)").evaluate().asNumber());
        Assert.assertEquals("number.round", new BigDecimal("6.5"), Eel.compile(context, "$number.round(6.45, 1)").evaluate().asNumber());
        Assert.assertEquals("number.ceil", new BigDecimal("7"), Eel.compile(context, "$number.ceil(6.45)").evaluate().asNumber());
        Assert.assertEquals("number.floor", new BigDecimal("6"), Eel.compile(context, "$number.floor(6.5)").evaluate().asNumber());

        // Other maths functions
        Assert.assertEquals("Factorial", 720, Eel.compile(context, "$factorial(6)").evaluate().asInt());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_newDate() {
        Assert.assertTrue("utc",
            Eel.compile(context, "$date.utc('-200year')")
                .evaluate()
                .asText()
                .matches("18\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z"));
        Assert.assertTrue("local",
            Eel.compile(context, "$date.local('+1h')")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+"));
        Assert.assertTrue("at",
            Eel.compile(context, "$date.at('-5')")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?-05"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_manipulations() {
        Assert.assertEquals("offset - snap",
            "2007-12-03T10:00:00Z",
            Eel.compile(context, "$date.plus( 1196676933, '@h')")
                .evaluate()
                .asText());
        Assert.assertEquals("offset - plus",
            "2007-12-03T07:15:42Z",
            Eel.compile(context, "$date.plus( 1196676933, '-3h', '+9s')")
                .evaluate()
                .asText());
        Assert.assertEquals("offset - minus",
            "2007-12-03T13:15:23.995Z",
            Eel.compile(context, "$date.minus( 1196676933, '-3h', '+9s', '5i')")
                .evaluate()
                .asText());
        Assert.assertEquals("set",
            "2007-12-03T03:05:01Z",
            Eel.compile(context, "$date.set( 1196676933, '3h', '5minute', '1s')")
                .evaluate()
                .asText());
        Assert.assertEquals("setZone",
            "2007-12-03T10:15:33-06",
            Eel.compile(context, "$date.setZone( 1196676933, '-6')")
                .evaluate()
                .asText());
        Assert.assertEquals("moveZone",
            "2007-12-03T04:15:33-06",
            Eel.compile(context, "$date.moveZone( 1196676933, '-6')")
                .evaluate()
                .asText());
        Assert.assertEquals("duration",
            14,
            Eel.compile(context, "$duration( 1196676933, 1235988933, 'months')")
                .evaluate()
                .asLong());
        Assert.assertEquals("set and snap weeks",
            "2009-01-11T00:00:00Z",
            Eel.compile(context, "$date.set( '2009-03-05T10:11:12Z', '2w', '@w' )")
                .evaluate()
                .asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_date() {
        String date = Eel.compile(context, "$format.date( 'd/M/yyyy HH:mm', 1196676933, '4d', '-7minutes' )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.date",
            "7/12/2007 10:08",
            date);

        String start = Eel.compile(context, "$format.start( 'd/M/yyyy HH:mm', 'UTC', '+1h' )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.start: " + start, start.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String utc = Eel.compile(context, "$format.utc( 'd/M/yyyy HH:mm', '+2w' )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.utc: " + utc, utc.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String local = Eel.compile(context, "$format.local( 'd/M/yyyy HH:mm', '-3h' )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.local: " + local, local.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String at = Eel.compile(context, "$format.at( 'Europe/Paris', 'd/M/yyyy HH:mm', '+12h', '3m' )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.at: " + at, at.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_number() {
        String binary = Eel.compile(context, "$format.binary( 0xa55a )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.binary", "1010010101011010", binary);

        String octal = Eel.compile(context, "$format.octal( 1196676933 )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.octal", "10724753505", octal);

        String hex = Eel.compile(context, "$format.hex( 1196676933 )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.hex", "4753d745", hex);

        String radixThree = Eel.compile(context, "$format.number( 1196676933, 3 )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.number", "10002101202111010220", radixThree);
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

        Assert.assertEquals("moveZone() should NOT change time from UTC", utc, moveZone);
        Assert.assertEquals("setZone() SHOULD change time from UTC", (utc - 18000), setZone);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logging() {
        Assert.assertEquals("trace", "3", Eel.compile(context, "$log.trace( 1 + 2 )").evaluate().asText());
        Assert.assertEquals("debug", "true", Eel.compile(context, "$log.trace( true or false )").evaluate().asText());
        Assert.assertEquals("info", "value", Eel.compile(context, "$log.info( 'message {}', 'value' )").evaluate().asText());
        Assert.assertEquals("warn", "false", Eel.compile(context, "$log.warn( 'message {}', ( 2=3 ) )").evaluate().asText());
        Assert.assertEquals("error", "ab", Eel.compile(context, "$log.error( 'message {} {}', 'test', 'a'~>'b' )").evaluate().asText());
        Assert.assertEquals("error", "ab", Eel.compile(context, "$log.error( 'message {} {}', 'test', 'a'~>'b' )").evaluate().asText());
        Assert.assertEquals("pass through", "Hello World!", Eel.compile(context, "$log.error( 'No data', 'Hello World!' )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logging_filters() {
        Assert.assertEquals("Keep Tab - pass through",
            "Tab\tChar",
            Eel.compile(context, "$log.info( 'Tab\\tChar' )").evaluate().asText());
        Assert.assertTrue("Keep Tab - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Logged EEL Message: Tab\tChar"));

        // Filter out the following escaped characters
        Assert.assertEquals("Skip NewLine - pass through",
            "New\nLine",
            Eel.compile(context, "$log.info( 'New\\nLine' )").evaluate().asText());
        Assert.assertTrue("Skip NewLine - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Logged EEL Message: NewLine"));

        Assert.assertEquals("Skip CarriageReturn - pass through",
            "Carriage\rReturn",
            Eel.compile(context, "$log.info( 'Carriage\\rReturn' )").evaluate().asText());
        Assert.assertTrue("Skip CarriageReturn - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Logged EEL Message: CarriageReturn"));

        Assert.assertEquals("Skip FormFeed - pass through",
            "Form\fFeed",
            Eel.compile(context, "$log.info( 'Form\\fFeed' )").evaluate().asText());
        Assert.assertTrue("Skip FormFeed - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Logged EEL Message: FormFeed"));

        Assert.assertEquals("Skip Backspace - pass through",
            "Back\bSpace",
            Eel.compile(context, "$log.info( 'Back\\bSpace' )").evaluate().asText());
        Assert.assertTrue("Skip Backspace - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Logged EEL Message: FormFeed"));

        Assert.assertEquals("Unicode - pass through",
            "Unicode: \u00A9 ~",
            Eel.compile(context, "$log.info( 'Unicode: \u00A9 ~' )").evaluate().asText());
        Assert.assertTrue("Unicode - Logged text: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("Unicode:  ~"));
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

        Assert.assertTrue("fileSeparator(): " + fileSeparator,
            "/".equals(fileSeparator) || "\\".equals(fileSeparator));
        Assert.assertTrue("pwd", pwd.matches("^([A-za-z]:)?[/\\\\]([^/\\\\]+[/\\\\])*"));
        Assert.assertFalse("system.home() : " + home, home.isEmpty());      // Other tests have checked the value
        Assert.assertFalse("system.temp() : " + temp, temp.isEmpty());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileNames() {
        String path = (File.separatorChar =='/') ? "a/b/../c/" : "a\\\\b\\\\..\\\\c\\\\";

        Assert.assertEquals("realPath",
            System.getProperty("user.dir").replace('\\', '/') + "/a/c/d.txt",
            Eel.compile(context, "$realPath('" + path + "/d.txt')").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("dirName",
            "a/b/../c",
            Eel.compile(context, "$dirName('" + path + "d.txt')").evaluate().asText()
                .replace('\\', '/').replaceFirst("^.:", ""));
        Assert.assertEquals("baseName",
            "d",
            Eel.compile(context, "$baseName('" + path + "d.txt', '.txt')").evaluate().asText());
        Assert.assertEquals("extension",
            "tar.gz",
            Eel.compile(context, "$extension('" + path + "d.tar.gz')").evaluate().asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileInfo() {
        Assert.assertTrue("POM exists", Eel.compile(context, "$exists('pom.*')").evaluate().asLogic());

        // Source Control will change the file attributes, so the best we can do is make sure these functions don't throw exceptions

        Eel.compile(context, "$createAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$accessedAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$modifiedAt('pom.xml')").evaluate().asDate();
        Eel.compile(context, "$fileSize('pom.xml')").evaluate().asNumber();

        EelFailException actual = Assert.assertThrows(EelFailException.class,
            () -> Eel.compile(context, "$fileSize('missingFile', fail('Expected fileSize exception'))").evaluate());

        Assert.assertEquals("Unexpected Error Message",
            "Expected fileSize exception",
            actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_findFiles() {
        Assert.assertEquals("fileCount",
            1,
            Eel.compile(context, "$fileCount('.', 'pom.xml')").evaluate().asLong());

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

        Assert.assertTrue(name + "returned: " + actual, actual.replace('\\', '/').endsWith("/pom.xml"));
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

        Assert.assertEquals("head", "line 1", Eel.compile(context, "$io.head( '" + textFile + "', 1 )").evaluate().asText());
        Assert.assertEquals("tail", "line 3", Eel.compile(context, "$io.tail( '" + textFile + "', 1 )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel} with failures
     */
    @Test
    public void test_fail() {
        EelFailException actual;

        actual = Assert.assertThrows("No message", EelFailException.class, () -> Eel.compile(context, "$fail()").evaluate());
        Assert.assertEquals("No message - unexpected message", "", actual.getMessage());

        actual = Assert.assertThrows("With message", EelFailException.class, () -> Eel.compile(context, "$fail('my Message!')").evaluate());
        Assert.assertEquals("With message - unexpected message", "my Message!", actual.getMessage());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_misc() {
        Assert.assertTrue("random", Eel.compile(context, "$random(10, 99)").evaluate().asText().matches("\\d{2}"));
        Assert.assertTrue("uuid", Eel.compile(context, "$uuid()").evaluate().asText().matches("[0-9a-f-]{36}"));
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

        Assert.assertThrows(EelFailException.class, eel::evaluate);
    }



    private void assertPattern(@Nonnull String name, @Nonnull String expression, @Nonnull String expectedPattern) {
        String value = Eel.compile(context, expression)
            .evaluate()
            .asText();

        Assert.assertTrue(name + " was unexpected value. Was " + value + ", expected pattern " + expectedPattern,
            value.matches(expectedPattern));
    }
}
