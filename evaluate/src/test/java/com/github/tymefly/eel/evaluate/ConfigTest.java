package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.net.URI;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import test.functions1.Plus1;
import test.functions2.Plus2;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Unit test for {@link Config}
 */
public class ConfigTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    private Config badOption;
    private Config scriptAndExpression;
    private Config requestHelp;
    private Config requestVersion;
    private Config scripted;
    private Config minimal;
    private Config maximal;


    @Before
    public void setUp() throws Exception {
        URI propertiesFile = this.getClass()
            .getClassLoader()
            .getResource("symbols-table.properties")
            .toURI();
        String path = new File(propertiesFile).getCanonicalPath();

        badOption = Config.parse("--expected-error");
        scriptAndExpression = Config.parse("--script", "test.eel", "myExpression");
        requestHelp = Config.parse("--help");
        requestVersion = Config.parse("--version");
        scripted = Config.parse("--script", "test.eel");
        minimal = Config.parse("minimal");
        maximal = Config.parse(
            "--verbose",
            "--env",
            "--props",
            "-D", "key1=value1",
            "-D", "key2",
            "--definitions", path,
            "--default", "<undefined>",
            "--precision", "5",
            "--io-limit", "2048",
            "--start-of-week", "Sunday",
            "--days-in-first-week", "2",
            "--udf-class", "test.functions1.Plus1",
            "--udf-package", "test.functions2",
            "maximal");

        stdErr.clear();              // because badOption will pollute the standard Error
    }


    /**
     * Unit test {@link Config#isValid()}
     */
    @Test
    public void test_isValid() {
        Assert.assertFalse("badOption", badOption.isValid());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.isValid());
        Assert.assertTrue("requestHelp", requestHelp.isValid());
        Assert.assertTrue("requestVersion", requestVersion.isValid());
        Assert.assertTrue("scripted", scripted.isValid());
        Assert.assertTrue("minimal", minimal.isValid());
        Assert.assertTrue("maximal", maximal.isValid());
    }

    /**
     * Unit test {@link Config#requestHelp()}
     */
    @Test
    public void test_requestHelp() {
        Assert.assertFalse("badOption", badOption.requestHelp());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.requestHelp());
        Assert.assertTrue("requestHelp", requestHelp.requestHelp());
        Assert.assertFalse("requestVersion", requestVersion.requestHelp());
        Assert.assertFalse("scripted", scripted.requestHelp());
        Assert.assertFalse("minimal", minimal.requestHelp());
        Assert.assertFalse("maximal", maximal.requestHelp());
    }


    /**
     * Unit test {@link Config#requestVersion()}
     */
    @Test
    public void test_requestVersion() {
        Assert.assertFalse("badOption", badOption.requestVersion());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.requestVersion());
        Assert.assertFalse("requestHelp", requestHelp.requestVersion());
        Assert.assertTrue("requestVersion", requestVersion.requestVersion());
        Assert.assertFalse("scripted", scripted.requestVersion());
        Assert.assertFalse("minimal", minimal.requestVersion());
        Assert.assertFalse("maximal", maximal.requestVersion());
    }


    /**
     * Unit test {@link Config#verbose()}
     */
    @Test
    public void test_verbose() {
        Assert.assertFalse("badOption", badOption.verbose());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.verbose());
        Assert.assertFalse("requestHelp", requestHelp.verbose());
        Assert.assertFalse("requestVersion", requestVersion.verbose());
        Assert.assertFalse("scripted", scripted.verbose());
        Assert.assertFalse("minimal", minimal.verbose());
        Assert.assertTrue("maximal", maximal.verbose());
    }

    /**
     * Unit test {@link Config#useEnvironmentVariables()}
     */
    @Test
    public void test_useEnvironmentVariables() {
        Assert.assertFalse("badOption", badOption.useEnvironmentVariables());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.useEnvironmentVariables());
        Assert.assertFalse("requestHelp", requestHelp.useEnvironmentVariables());
        Assert.assertFalse("requestVersion", requestVersion.useEnvironmentVariables());
        Assert.assertFalse("scripted", scripted.useEnvironmentVariables());
        Assert.assertFalse("minimal", minimal.useEnvironmentVariables());
        Assert.assertTrue("maximal", maximal.useEnvironmentVariables());
    }

    /**
     * Unit test {@link Config#useProperties()}
     */
    @Test
    public void test_useProperties() {
        Assert.assertFalse("badOption", badOption.useProperties());
        Assert.assertFalse("scriptAndExpression", scriptAndExpression.useProperties());
        Assert.assertFalse("requestHelp", requestHelp.useProperties());
        Assert.assertFalse("requestVersion", requestVersion.useProperties());
        Assert.assertFalse("scripted", scripted.useProperties());
        Assert.assertFalse("minimal", minimal.useProperties());
        Assert.assertTrue("maximal", maximal.useProperties());
    }

    /**
     * Unit test {@link Config#definitions()}
     */
    @Test
    public void test_definitions() {
        Assert.assertEquals("badOption", Collections.emptyMap(), badOption.definitions());
        Assert.assertEquals("scriptAndExpression", Collections.emptyMap(), scriptAndExpression.definitions());
        Assert.assertEquals("requestHelp", Collections.emptyMap(), requestHelp.definitions());
        Assert.assertEquals("requestVersion", Collections.emptyMap(), requestVersion.definitions());
        Assert.assertEquals("scripted", Collections.emptyMap(), scripted.definitions());
        Assert.assertEquals("minimal", Collections.emptyMap(), minimal.definitions());
        Assert.assertEquals("maximal",
            Map.ofEntries(
                Map.entry("key1", "value1"),
                Map.entry("key2", ""),
                Map.entry("a", "true"),
                Map.entry("b", "123"),
                Map.entry("c", "Hello World"),
                Map.entry("d", "2000-01-02T03:04:05Z")),
            maximal.definitions());
    }

    /**
     * Unit test {@link Config#defaultValue()}
     */
    @Test
    public void test_defaultValue() {
        Assert.assertNull("badOption", badOption.defaultValue());
        Assert.assertNull("scriptAndExpression", scriptAndExpression.defaultValue());
        Assert.assertNull("requestHelp", requestHelp.defaultValue());
        Assert.assertNull("requestVersion", requestVersion.defaultValue());
        Assert.assertNull("scripted", scripted.defaultValue());
        Assert.assertNull("minimal", minimal.defaultValue());
        Assert.assertEquals("maximal", "<undefined>", maximal.defaultValue());
    }

    /**
     * Unit test {@link Config#precision()}
     */
    @Test
    public void test_precision() {
        Assert.assertEquals("badOption", EelContext.DEFAULT_PRECISION, badOption.precision());
        Assert.assertEquals("scriptAndExpression", EelContext.DEFAULT_PRECISION, scriptAndExpression.precision());
        Assert.assertEquals("requestHelp", EelContext.DEFAULT_PRECISION, requestHelp.precision());
        Assert.assertEquals("requestVersion", EelContext.DEFAULT_PRECISION, requestVersion.precision());
        Assert.assertEquals("scripted", EelContext.DEFAULT_PRECISION, scripted.precision());
        Assert.assertEquals("minimal", EelContext.DEFAULT_PRECISION, minimal.precision());
        Assert.assertEquals("maximal", 5, maximal.precision());
    }

    /**
     * Unit test {@link Config#ioLimit()}
     */
    @Test
    public void test_ioLimit() {
        Assert.assertEquals("badOption", EelContext.DEFAULT_IO_LIMIT, badOption.ioLimit());
        Assert.assertEquals("scriptAndExpression", EelContext.DEFAULT_IO_LIMIT, scriptAndExpression.ioLimit());
        Assert.assertEquals("requestHelp", EelContext.DEFAULT_IO_LIMIT, requestHelp.ioLimit());
        Assert.assertEquals("requestVersion", EelContext.DEFAULT_IO_LIMIT, requestVersion.ioLimit());
        Assert.assertEquals("scripted", EelContext.DEFAULT_IO_LIMIT, scripted.ioLimit());
        Assert.assertEquals("minimal", EelContext.DEFAULT_IO_LIMIT, minimal.ioLimit());
        Assert.assertEquals("maximal", 2048, maximal.ioLimit());
    }

    /**
     * Unit test {@link Config#daysInFirstWeek()}
     */
    @Test
    public void test_daysInFirstWeek() {
        Assert.assertEquals("badOption", 4, badOption.daysInFirstWeek());
        Assert.assertEquals("scriptAndExpression", 4, scriptAndExpression.daysInFirstWeek());
        Assert.assertEquals("requestHelp", 4, requestHelp.daysInFirstWeek());
        Assert.assertEquals("requestVersion", 4, requestVersion.daysInFirstWeek());
        Assert.assertEquals("scripted", 4, scripted.daysInFirstWeek());
        Assert.assertEquals("minimal", 4, minimal.daysInFirstWeek());
        Assert.assertEquals("maximal", 2, maximal.daysInFirstWeek());
    }

    /**
     * Unit test {@link Config#startOfWeek()}
     */
    @Test
    public void test_startOfWeek() {
        Assert.assertEquals("badOption", DayOfWeek.MONDAY, badOption.startOfWeek());
        Assert.assertEquals("scriptAndExpression", DayOfWeek.MONDAY, scriptAndExpression.startOfWeek());
        Assert.assertEquals("requestHelp", DayOfWeek.MONDAY, requestHelp.startOfWeek());
        Assert.assertEquals("requestVersion", DayOfWeek.MONDAY, requestVersion.startOfWeek());
        Assert.assertEquals("scripted", DayOfWeek.MONDAY, scripted.startOfWeek());
        Assert.assertEquals("minimal", DayOfWeek.MONDAY, minimal.startOfWeek());
        Assert.assertEquals("maximal", DayOfWeek.SUNDAY, maximal.startOfWeek());

        Assert.assertFalse("Invalid day", Config.parse("--start-of-week", "Notaday", "myExpression").isValid());
    }


    /**
     * Unit test {@link Config#functionList()}
     */
    @Test
    public void test_functionList() {
        Assert.assertEquals("badOption", Collections.emptyList(), badOption.functionList());
        Assert.assertEquals("scriptAndExpression", Collections.emptyList(), scriptAndExpression.functionList());
        Assert.assertEquals("requestHelp", Collections.emptyList(), requestHelp.functionList());
        Assert.assertEquals("requestVersion", Collections.emptyList(), requestVersion.functionList());
        Assert.assertEquals("scripted", Collections.emptyList(), scripted.functionList());
        Assert.assertEquals("minimal", Collections.emptyList(), minimal.functionList());
        Assert.assertEquals("maximal", List.of(Plus1.class), maximal.functionList());
    }

    /**
     * Unit test {@link Config#packageList()}
     */
    @Test
    public void test_packageList() {
        Assert.assertEquals("badOption", Collections.emptyList(), badOption.packageList());
        Assert.assertEquals("scriptAndExpression", Collections.emptyList(), scriptAndExpression.packageList());
        Assert.assertEquals("requestHelp", Collections.emptyList(), requestHelp.packageList());
        Assert.assertEquals("requestVersion", Collections.emptyList(), requestVersion.packageList());
        Assert.assertEquals("scripted", Collections.emptyList(), scripted.packageList());
        Assert.assertEquals("minimal", Collections.emptyList(), minimal.packageList());
        Assert.assertEquals("maximal", List.of(Plus2.class.getPackage()), maximal.packageList());
    }

    /**
     * Unit test {@link Config#getScriptFile()}
     */
    @Test
    public void test_getScriptFile() {
        Assert.assertNull("badOption", badOption.getScriptFile());
        Assert.assertEquals("scriptAndExpression", new File("test.eel"), scriptAndExpression.getScriptFile());
        Assert.assertNull("requestHelp", requestHelp.getScriptFile());
        Assert.assertNull("requestVersion", requestVersion.getScriptFile());
        Assert.assertEquals("scripted", new File("test.eel"), scripted.getScriptFile());
        Assert.assertNull("minimal", minimal.getScriptFile());
        Assert.assertNull("maximal", maximal.getScriptFile());
    }

    /**
     * Unit test {@link Config#getExpression()}
     */
    @Test
    public void test_getExpression() {
        Assert.assertNull("badOption", badOption.getExpression());
        Assert.assertEquals("scriptAndExpression", "myExpression", scriptAndExpression.getExpression());
        Assert.assertNull("requestHelp", requestHelp.getExpression());
        Assert.assertNull("requestVersion", requestVersion.getExpression());
        Assert.assertNull("scripted", scripted.getExpression());
        Assert.assertEquals("minimal", "minimal", minimal.getExpression());
        Assert.assertEquals("maximal", "maximal", maximal.getExpression());
    }

    /**
     * Unit test {@link Config#displayUsage()}
     */
    @Test
    public void test_displayUsage() {
        minimal.displayUsage();



        Assert.assertTrue("Help message not written",
            stdOut.getLinesNormalized().replaceAll("\r\n", "\n").startsWith("Usage:\n  java -jar evaluate-<version>.jar"));
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    public void test_badPackage() {
        Config config = Config.parse(
            "--udf-package", "test.functions-unknown",
            "should fail");

        Assert.assertFalse("should not be valid", config.isValid());
        Assert.assertTrue("Help message not written",
            stdErr.getLinesNormalized().contains("Error: Invalid package 'test.functions-unknown'"));
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    public void test_badFunction() {
        Config config = Config.parse(
            "--udf-class", "test.function.unknown",
            "should fail");

        Assert.assertFalse("should not be valid", config.isValid());
        Assert.assertTrue("Help message not written",
            stdErr.getLinesNormalized().contains("Error: Invalid function 'test.function.unknown'"));
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    public void test_duplicateDefinition() {
        Config config = Config.parse(
            "--define", "key=value1",
            "-D", "key=value2",
            "should fail");

        Assert.assertFalse("should not be valid", config.isValid());
        Assert.assertTrue("Error message not written",
            stdErr.getLinesNormalized().contains("Error: 'key' is already in the symbols table"));
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    public void test_badPropertiesFile() {
        Config config = Config.parse("--definitions", "path/to/unknown/file.xxxx");

        Assert.assertFalse("should not be valid", config.isValid());

        String log = stdErr.getLinesNormalized()
            .split("\n")[0]
            .replace('\\', '/');
        Assert.assertTrue("Error message not written",
            log.matches("Error: Can not read file '.*/path/to/unknown/file.xxxx'"));
    }

}