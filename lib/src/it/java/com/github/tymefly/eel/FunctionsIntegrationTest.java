package com.github.tymefly.eel;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import com.github.tymefly.eel.exception.EelFailException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Functional testing of each of the EEL functions
 */
public class FunctionsIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

    private static final double TEST_DELTA = 0.00000001;

    private EelContext context;
    
    
    @Before
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_strings() {
        Assert.assertEquals("upper", "HELLO", Eel.compile(context, "$( upper('hello') )").evaluate().asText());
        Assert.assertEquals("lower", "hello", Eel.compile(context, "$( lower('HELLO') )").evaluate().asText());
        Assert.assertEquals("title", "Hello World Again!! 01abcd~", Eel.compile(context, "$( title('hello world AGAIN!! 01abCD~') )").evaluate().asText());
        Assert.assertEquals("len", "7", Eel.compile(context, "$( len('  xxxx ') )").evaluate().asText());
        Assert.assertTrue("isEmpty - empty", Eel.compile(context, "$( isEmpty('') )").evaluate().asLogic());
        Assert.assertFalse("isEmpty - white", Eel.compile(context, "$( isEmpty(' ') )").evaluate().asLogic());
        Assert.assertFalse("isEmpty - text", Eel.compile(context, "$( isEmpty('abc') )").evaluate().asLogic());
        Assert.assertTrue("isBlank - empty", Eel.compile(context, "$( isBlank('') )").evaluate().asLogic());
        Assert.assertTrue("isBlank - white", Eel.compile(context, "$( isBlank(' ') )").evaluate().asLogic());
        Assert.assertFalse("isBlank - text", Eel.compile(context, "$( isBlank('abc') )").evaluate().asLogic());
        Assert.assertEquals("trim", "xxxx", Eel.compile(context, "$( trim('  xxxx ') )").evaluate().asText());
        Assert.assertEquals("left", "abc", Eel.compile(context, "$( left('abcdef', 3) )").evaluate().asText());
        Assert.assertEquals("right", "def", Eel.compile(context, "$( right('abcdef', 3) )").evaluate().asText());
        Assert.assertEquals("mid", "bc", Eel.compile(context, "$( mid('abcdef', 1, 2) )").evaluate().asText());
        Assert.assertEquals("beforeFirst", "ab", Eel.compile(context, "$( beforeFirst('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("afterFirst", "cd.ef", Eel.compile(context, "$( afterFirst('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("beforeLast", "ab.cd", Eel.compile(context, "$( beforeLast('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("afterLast", "ef", Eel.compile(context, "$( afterLast('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertTrue("match", Eel.compile(context, "$( matches(uuid(), '[0-9a-f-]{36}') )").evaluate().asLogic());
        Assert.assertEquals("extract",
            "HelloWorld",
            Eel.compile(context, "$( extract('?? ~Hello~  @World@ ??', '.*~([^~]*)~.*@([^@]*)@.*') )").evaluate().asText());
        Assert.assertEquals("replace", "he!!o", Eel.compile(context, "$( replace('hello', 'l', '!') )").evaluate().asText());
        Assert.assertEquals("replaceAll", "?ello", Eel.compile(context, "$( replaceEx('hello', '^.', '?') )").evaluate().asText());
        Assert.assertEquals("indexOf", 2, Eel.compile(context, "$( indexOf('hello', 'l') )").evaluate().asNumber().intValue());
        Assert.assertEquals("lastIndexOf", 3, Eel.compile(context, "$( lastIndexOf('hello', 'l') )").evaluate().asNumber().intValue());
        Assert.assertEquals("printf", "format:    Hello 12", Eel.compile(context, "$( printf('format: %8.8s %d', 'Hello', 12) )").evaluate().asText());
        Assert.assertEquals("padLeft", "      myText", Eel.compile(context, "$( padLeft('myText', 12) )").evaluate().asText());
        Assert.assertEquals("padRight", "myText@@@@@@", Eel.compile(context, "$( padRight('myText', 12, '@_') )").evaluate().asText());

        Assert.assertEquals("char", "a", Eel.compile(context, "$( char(97) )").evaluate().asText());
        Assert.assertEquals("codepoint", 126, Eel.compile(context, "$( codepoint('~') )").evaluate().asNumber().intValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_stats() {
        Assert.assertEquals("max", "5", Eel.compile(context, "$( max(-3, 1, 2, 5) )").evaluate().asText());
        Assert.assertEquals("min", "-3", Eel.compile(context, "$( min(-3, 1, 2, 5) )").evaluate().asText());
        Assert.assertEquals("avg", "1.25", Eel.compile(context, "$( avg(-3, 1, 2, 5) )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        // Sign tests and manipulation
        Assert.assertEquals("Abs", 2.1, Eel.compile(context, "$( abs(-2.1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Sgn", -1, Eel.compile(context, "$( sgn(-2.1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Logarithms and exponential functions
        Assert.assertEquals("Log", 2.0, Eel.compile(context, "$( log(100) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Ln", 4.605170185988091, Eel.compile(context, "$( ln(100) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Exp", 148.4131591025766, Eel.compile(context, "$( exp(5) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Root", 7.0, Eel.compile(context, "$( root(343, 3) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Trigonometry
        Assert.assertEquals("Sin", 0, Eel.compile(context, "$( sin(pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Cos", -1.0, Eel.compile(context, "$( cos(pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Tan", 0, Eel.compile(context, "$( tan(2 * pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile(context, "$( asin(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile(context, "$( asin(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Acos", 1.570796326794897, Eel.compile(context, "$( acos(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Atan", 0.78539816, Eel.compile(context, "$( atan(1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Rounding functions
        Assert.assertEquals("number.truncate", new BigDecimal("6"), Eel.compile(context, "$( number.truncate(6.5) )").evaluate().asNumber());
        Assert.assertEquals("number.round", new BigDecimal("7"), Eel.compile(context, "$( number.round(6.5) )").evaluate().asNumber());

        // Other maths functions
        Assert.assertEquals("Factorial", 720, Eel.compile(context, "$( factorial(6) )").evaluate().asNumber().intValue());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_newDate() {
        Assert.assertTrue("utc",
            Eel.compile(context, "$( date.utc('-200year') )")
                .evaluate()
                .asText()
                .matches("18\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
        Assert.assertTrue("local",
            Eel.compile(context, "$( date.local('+1h') )")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+"));
        Assert.assertTrue("at",
            Eel.compile(context, "$( date.at('-5') )")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-05"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_manipulations() {
        Assert.assertEquals("truncate",
            "2007-12-03T10:00:00Z",
            Eel.compile(context, "$( date.truncate( 1196676933, 'h') )")
                .evaluate()
                .asText());
        Assert.assertEquals("offset",
            "2007-12-03T07:15:42Z",
            Eel.compile(context, "$( date.offset( 1196676933, '-3h', '+9s') )")
                .evaluate()
                .asText());
        Assert.assertEquals("set",
            "2007-12-03T03:05:01Z",
            Eel.compile(context, "$( date.set( 1196676933, '3h', '5minute', '1s') )")
                .evaluate()
                .asText());
        Assert.assertEquals("setZone",
            "2007-12-03T10:15:33-06",
            Eel.compile(context, "$( date.setZone( 1196676933, '-6') )")
                .evaluate()
                .asText());
        Assert.assertEquals("moveZone",
            "2007-12-03T04:15:33-06",
            Eel.compile(context, "$( date.moveZone( 1196676933, '-6') )")
                .evaluate()
                .asText());
        Assert.assertEquals("duration",
            14,
            Eel.compile(context, "$( duration( 1196676933, 1235988933, 'months') )")
                .evaluate()
                .asNumber().longValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_date() {
        String date = Eel.compile(context, "$( format.date( 'd/M/yyyy HH:mm', 1196676933, '4d', '-7minutes' ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.date",
            "7/12/2007 10:08",
            date);

        String utc = Eel.compile(context, "$( format.utc( 'd/M/yyyy HH:mm', '+2w' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.utc: " + utc, utc.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String local = Eel.compile(context, "$( format.local( 'd/M/yyyy HH:mm', '-3h' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.local: " + local, local.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String at = Eel.compile(context, "$( format.at( 'Europe/Paris', 'd/M/yyyy HH:mm', '+12h', '3m' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.at: " + at, at.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_number() {
        String hex = Eel.compile(context, "$( format.hex( 1196676933 ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.hex", "4753d745", hex);

        String octal = Eel.compile(context, "$( format.octal( 1196676933 ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.octal", "10724753505", octal);

        String radixThree = Eel.compile(context, "$( format.number( 1196676933, 3 ) )")
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
        long utc = Eel.compile(context, "$( NUMBER(${date}) )")          // Convert Text -> Number -> Date
            .evaluate(symbols)
            .asNumber()
            .longValue();
        long setZone = Eel.compile(context, "$( date.setZone( NUMBER(${date}), '+5') )")
            .evaluate(symbols)
            .asNumber()
            .longValue();
        long moveZone = Eel.compile(context, "$( date.moveZone( NUMBER(${date}), '+5') )")
            .evaluate(symbols)
            .asNumber()
            .longValue();

        Assert.assertEquals("moveZone() should NOT change time from UTC", utc, moveZone);
        Assert.assertEquals("setZone() SHOULD change time from UTC", (utc - 18000), setZone);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logging() {
        Assert.assertEquals("trace", "3", Eel.compile(context, "$( log.trace( 1 + 2 ) )").evaluate().asText());
        Assert.assertEquals("debug", "true", Eel.compile(context, "$( log.trace( true | false ) )").evaluate().asText());
        Assert.assertEquals("info", "value", Eel.compile(context, "$( log.info( 'message {}', 'value' ) )").evaluate().asText());
        Assert.assertEquals("warn", "false", Eel.compile(context, "$( log.warn( 'message {}', ( 2=3 ) ) )").evaluate().asText());
        Assert.assertEquals("error", "ab", Eel.compile(context, "$( log.error( 'message {} {}', 'test', 'a'~>'b' ) )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_system() {
        String fileSeparator = Eel.compile(context, "$( system.fileSeparator() )").evaluate().asText();
        String pwd = Eel.compile(context, "$( system.pwd() )").evaluate().asText();
        String home = Eel.compile(context, "$( system.home() )").evaluate().asText();
        String temp = Eel.compile(context, "$( system.temp() )").evaluate().asText();

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
            Eel.compile(context, "$( realPath('" + path + "/d.txt') )").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("dirName",
            "a/b/../c",
            Eel.compile(context, "$( dirName('" + path + "d.txt') )").evaluate().asText()
                .replace('\\', '/').replaceFirst("^.:", ""));
        Assert.assertEquals("baseName",
            "d",
            Eel.compile(context, "$( baseName('" + path + "d.txt', '.txt') )").evaluate().asText());
        Assert.assertEquals("extension",
            "tar.gz",
            Eel.compile(context, "$( extension('" + path + "d.tar.gz') )").evaluate().asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileInfo() {
        Assert.assertTrue("POM exists", Eel.compile(context, "$( exists('pom.*') )").evaluate().asLogic());

        // Source Control will change the file attributes, so the best we can do is make sure these functions don't throw exceptions

        Eel.compile(context, "$( createAt('pom.xml') )").evaluate().asDate();
        Eel.compile(context, "$( accessedAt('pom.xml') )").evaluate().asDate();
        Eel.compile(context, "$( modifiedAt('pom.xml') )").evaluate().asDate();
        Eel.compile(context, "$( fileSize('pom.xml') )").evaluate().asNumber();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_findFiles() {
        Assert.assertEquals("fileCount",
            1,
            Eel.compile(context, "$( fileCount('.', 'pom.xml') )").evaluate().asNumber().longValue());

        // Source Control will set the attributes, so the best we can do is limit the search to a single file that must exist

        Assert.assertEquals("firstCreated",
            "./pom.xml",
            Eel.compile(context, "$( firstCreated('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("lastCreated",
            "./pom.xml",
            Eel.compile(context, "$( lastCreated('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));

        Assert.assertEquals("firstAccessed",
            "./pom.xml",
            Eel.compile(context, "$( firstAccessed('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("lastAccessed",
            "./pom.xml",
            Eel.compile(context, "$( lastAccessed('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));

        Assert.assertEquals("firstModified",
            "./pom.xml",
            Eel.compile(context, "$( firstModified('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("lastModified",
            "./pom.xml",
            Eel.compile(context, "$( lastModified('.', 'pom.xml') )").evaluate().asText().replace('\\', '/'));
    }


    /**
     * Integration test {@link Eel} with failures
     */
    @Test
    public void test_fail() {
        EelFailException actual;

        actual = Assert.assertThrows("No message", EelFailException.class, () -> Eel.compile(context, "$( fail() )").evaluate());
        Assert.assertEquals("No message - unexpected message", "", actual.getMessage());

        actual = Assert.assertThrows("With message", EelFailException.class, () -> Eel.compile(context, "$( fail('my Message!') )").evaluate());
        Assert.assertEquals("With message - unexpected message", "my Message!", actual.getMessage());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_misc() {
        Assert.assertTrue("random", Eel.compile(context, "$( random(10, 99) )").evaluate().asText().matches("\\d{2}"));
        Assert.assertTrue("uuid", Eel.compile(context, "$( uuid() )").evaluate().asText().matches("[0-9a-f-]{36}"));
    }
}
