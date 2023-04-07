package com.github.tymefly.eel;

import java.util.Map;

import org.junit.Assert;
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

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_strings() {
        Assert.assertEquals("upper", "HELLO", Eel.compile("$( upper('hello') )").evaluate().asText());
        Assert.assertEquals("lower", "hello", Eel.compile("$( lower('HELLO') )").evaluate().asText());
        Assert.assertEquals("len", "7", Eel.compile("$( len('  xxxx ') )").evaluate().asText());
        Assert.assertTrue("isEmpty", Eel.compile("$( isEmpty('') )").evaluate().asLogic());
        Assert.assertEquals("trim", "xxxx", Eel.compile("$( trim('  xxxx ') )").evaluate().asText());
        Assert.assertEquals("left", "abc", Eel.compile("$( left('abcdef', 3) )").evaluate().asText());
        Assert.assertEquals("right", "def", Eel.compile("$( right('abcdef', 3) )").evaluate().asText());
        Assert.assertEquals("mid", "bc", Eel.compile("$( mid('abcdef', 1, 2) )").evaluate().asText());
        Assert.assertEquals("beforeFirst", "ab", Eel.compile("$( beforeFirst('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("afterFirst", "cd.ef", Eel.compile("$( afterFirst('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("beforeLast", "ab.cd", Eel.compile("$( beforeLast('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertEquals("afterLast", "ef", Eel.compile("$( afterLast('ab.cd.ef', '.') )").evaluate().asText());
        Assert.assertTrue("match", Eel.compile("$( matches(uuid(), '[0-9a-f-]{36}') )").evaluate().asLogic());
        Assert.assertEquals("extract",
            "HelloWorld",
            Eel.compile("$( extract('?? ~Hello~  @World@ ??', '.*~([^~]*)~.*@([^@]*)@.*') )").evaluate().asText());
        Assert.assertEquals("replace", "he!!o", Eel.compile("$( replace('hello', 'l', '!') )").evaluate().asText());
        Assert.assertEquals("replaceAll", "?ello", Eel.compile("$( replaceEx('hello', '^.', '?') )").evaluate().asText());
        Assert.assertEquals("indexOf", 2, Eel.compile("$( indexOf('hello', 'l') )").evaluate().asNumber().intValue());
        Assert.assertEquals("lastIndexOf", 3, Eel.compile("$( lastIndexOf('hello', 'l') )").evaluate().asNumber().intValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_stats() {
        Assert.assertEquals("max", "5", Eel.compile("$( max(-3, 1, 2, 5) )").evaluate().asText());
        Assert.assertEquals("min", "-3", Eel.compile("$( min(-3, 1, 2, 5) )").evaluate().asText());
        Assert.assertEquals("avg", "1.25", Eel.compile("$( avg(-3, 1, 2, 5) )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        // Sign tests and manipulation
        Assert.assertEquals("Abs", 2.1, Eel.compile("$( abs(-2.1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Sgn", -1, Eel.compile("$( sgn(-2.1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Logarithms and exponential functions
        Assert.assertEquals("Log", 2.0, Eel.compile("$( log(100) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Ln", 4.605170185988091, Eel.compile("$( ln(100) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Exp", 148.4131591025766, Eel.compile("$( exp(5) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Root", 7.0, Eel.compile("$( root(343, 3) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Trigonometry
        Assert.assertEquals("Sin", 0, Eel.compile("$( sin(pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Cos", -1.0, Eel.compile("$( cos(pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Tan", 0, Eel.compile("$( tan(2 * pi) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile("$( asin(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Asin", 0, Eel.compile("$( asin(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Acos", 1.570796326794897, Eel.compile("$( acos(0) )").evaluate().asNumber().doubleValue(), TEST_DELTA);
        Assert.assertEquals("Atan", 0.78539816, Eel.compile("$( atan(1) )").evaluate().asNumber().doubleValue(), TEST_DELTA);

        // Other maths functions
        Assert.assertEquals("Factorial", 720, Eel.compile("$( factorial(6) )").evaluate().asNumber().intValue());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_date_newDate() {
        Assert.assertTrue("utc",
            Eel.compile("$( date.utc('-200year') )")
                .evaluate()
                .asText()
                .matches("18\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
        Assert.assertTrue("local",
            Eel.compile("$( date.local('+1h') )")
                .evaluate()
                .asText()
                .matches("20\\d{2}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.+"));
        Assert.assertTrue("at",
            Eel.compile("$( date.at('-5') )")
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
            Eel.compile("$( date.truncate( 1196676933, 'h') )")
                .evaluate()
                .asText());
        Assert.assertEquals("offset",
            "2007-12-03T07:15:42Z",
            Eel.compile("$( date.offset( 1196676933, '-3h', '+9s') )")
                .evaluate()
                .asText());
        Assert.assertEquals("set",
            "2007-12-03T03:05:01Z",
            Eel.compile("$( date.set( 1196676933, '3h', '5minute', '1s') )")
                .evaluate()
                .asText());
        Assert.assertEquals("setZone",
            "2007-12-03T10:15:33-06",
            Eel.compile("$( date.setZone( 1196676933, '-6') )")
                .evaluate()
                .asText());
        Assert.assertEquals("moveZone",
            "2007-12-03T04:15:33-06",
            Eel.compile("$( date.moveZone( 1196676933, '-6') )")
                .evaluate()
                .asText());
        Assert.assertEquals("duration",
            14,
            Eel.compile("$( duration( 1196676933, 1235988933, 'months') )")
                .evaluate()
                .asNumber().longValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_date() {
        String date = Eel.compile("$( format.date( 'd/M/yyyy HH:mm', 1196676933 ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.date",
            "3/12/2007 10:15",
            date);

        String utc = Eel.compile("$( format.utc( 'd/M/yyyy HH:mm', '+2w' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.utc: " + utc, utc.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String local = Eel.compile("$( format.local( 'd/M/yyyy HH:mm', '-3h' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.local: " + local, local.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));

        String at = Eel.compile("$( format.at( 'Europe/Paris', 'd/M/yyyy HH:mm', '+12h', '3m' ) )")
            .evaluate()
            .asText();
        Assert.assertTrue("format.at: " + at, at.matches("\\d+/\\d+/\\d{4} \\d+:\\d+"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_format_number() {
        String hex = Eel.compile("$( format.hex( 1196676933 ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.hex", "4753d745", hex);

        String octal = Eel.compile("$( format.octal( 1196676933 ) )")
            .evaluate()
            .asText();
        Assert.assertEquals("format.octal", "10724753505", octal);

        String radixThree = Eel.compile("$( format.number( 1196676933, 3 ) )")
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
        long utc = Eel.compile("$( NUMBER(${date}) )")          // Convert Text -> Number -> Date
            .evaluate(symbols)
            .asNumber()
            .longValue();
        long setZone = Eel.compile("$( date.setZone( NUMBER(${date}), '+5') )")
            .evaluate(symbols)
            .asNumber()
            .longValue();
        long moveZone = Eel.compile("$( date.moveZone( NUMBER(${date}), '+5') )")
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
        Assert.assertEquals("trace", "3", Eel.compile("$( log.trace( 1 + 2 ) )").evaluate().asText());
        Assert.assertEquals("debug", "true", Eel.compile("$( log.trace( true | false ) )").evaluate().asText());
        Assert.assertEquals("info", "value", Eel.compile("$( log.info( 'message {}', 'value' ) )").evaluate().asText());
        Assert.assertEquals("warn", "false", Eel.compile("$( log.warn( 'message {}', ( 2=3 ) ) )").evaluate().asText());
        Assert.assertEquals("error", "ab", Eel.compile("$( log.error( 'message {} {}', 'test', 'a'~>'b' ) )").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_system() {
        String fileSeparator = Eel.compile("$( system.fileSeparator() )").evaluate().asText();
        String pwd = Eel.compile("$( system.pwd() )").evaluate().asText();
        String home = Eel.compile("$( system.home() )").evaluate().asText();
        String temp = Eel.compile("$( system.temp() )").evaluate().asText();

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
        Assert.assertEquals("realPath",
            System.getProperty("user.dir").replace('\\', '/') + "/a/c/d.txt",
            Eel.compile("$( realPath('a/b/../c/d.txt') )").evaluate().asText().replace('\\', '/'));
        Assert.assertEquals("baseName", "d", Eel.compile("$( baseName('/a/b/c/d.txt', '.txt') )").evaluate().asText());
        Assert.assertEquals("dirName",
            "/a/b/c",
            Eel.compile("$( dirName('/a/b/c/d.txt') )").evaluate().asText().replace('\\', '/').replaceFirst("^.:", ""));
        Assert.assertEquals("extension", ".tar.gz", Eel.compile("$( extension('/a/b/c/d.tar.gz') )").evaluate().asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_fileInfo() {
        Assert.assertTrue("POM exists", Eel.compile("$( exists('pom.xml') )").evaluate().asLogic());

        // FileControl will change the file attributes, so the best we can do is make sure these functions don't throw exceptions

        Eel.compile("$( createAt('pom.xml') )").evaluate().asDate();
        Eel.compile("$( accessedAt('pom.xml') )").evaluate().asDate();
        Eel.compile("$( modifiedAt('pom.xml') )").evaluate().asDate();
        Eel.compile("$( fileSize('pom.xml') )").evaluate().asNumber();
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_misc() {
        Assert.assertTrue("random", Eel.compile("$( random(10, 99) )").evaluate().asText().matches("\\d{2}"));
        Assert.assertTrue("uuid", Eel.compile("$( uuid() )").evaluate().asText().matches("[0-9a-f-]{36}"));
    }
}
