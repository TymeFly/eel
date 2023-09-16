package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import test.functions1.Plus1;
import test.functions2.Plus2;

/**
 * Unit test for {@link Config}
 */
public class ConfigTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

    private Config badOption;
    private Config requestHelp;
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
        requestHelp = Config.parse("--help");
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
            "--udf-class", "test.functions1.Plus1",
            "--udf-package", "test.functions2",
            "maximal");

        stdErr.clearLog();              // because badOption will pollute the standard Error
    }


    /**
     * Unit test {@link Config#isValid()}
     */
    @Test
    public void test_isValid() {
        Assert.assertFalse("badOption", badOption.isValid());
        Assert.assertTrue("requestHelp", requestHelp.isValid());
        Assert.assertTrue("minimal", minimal.isValid());
        Assert.assertTrue("maximal", maximal.isValid());
    }

    /**
     * Unit test {@link Config#requestHelp()}
     */
    @Test
    public void test_requestHelp() {
        Assert.assertFalse("badOption", badOption.requestHelp());
        Assert.assertTrue("requestHelp", requestHelp.requestHelp());
        Assert.assertFalse("minimal", minimal.requestHelp());
        Assert.assertFalse("maximal", maximal.requestHelp());
    }

    /**
     * Unit test {@link Config#verbose()}
     */
    @Test
    public void test_verbose() {
        Assert.assertFalse("badOption", badOption.verbose());
        Assert.assertFalse("requestHelp", requestHelp.verbose());
        Assert.assertFalse("minimal", minimal.verbose());
        Assert.assertTrue("maximal", maximal.verbose());
    }

    /**
     * Unit test {@link Config#useEnvironmentVariables()}
     */
    @Test
    public void test_useEnvironmentVariables() {
        Assert.assertFalse("badOption", badOption.useEnvironmentVariables());
        Assert.assertFalse("requestHelp", requestHelp.useEnvironmentVariables());
        Assert.assertFalse("minimal", minimal.useEnvironmentVariables());
        Assert.assertTrue("maximal", maximal.useEnvironmentVariables());
    }

    /**
     * Unit test {@link Config#useProperties()}
     */
    @Test
    public void test_useProperties() {
        Assert.assertFalse("badOption", badOption.useProperties());
        Assert.assertFalse("requestHelp", requestHelp.useProperties());
        Assert.assertFalse("minimal", minimal.useProperties());
        Assert.assertTrue("maximal", maximal.useProperties());
    }

    /**
     * Unit test {@link Config#definitions()}
     */
    @Test
    public void test_definitions() {
        Assert.assertEquals("badOption", Collections.emptyMap(), badOption.definitions());
        Assert.assertEquals("requestHelp", Collections.emptyMap(), requestHelp.definitions());
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
        Assert.assertNull("requestHelp", requestHelp.defaultValue());
        Assert.assertNull("minimal", minimal.defaultValue());
        Assert.assertEquals("maximal", "<undefined>", maximal.defaultValue());
    }

    /**
     * Unit test {@link Config#precision()}
     */
    @Test
    public void test_precision() {
        Assert.assertEquals("badOption", EelContext.DEFAULT_PRECISION, badOption.precision());
        Assert.assertEquals("requestHelp", EelContext.DEFAULT_PRECISION, requestHelp.precision());
        Assert.assertEquals("minimal", EelContext.DEFAULT_PRECISION, minimal.precision());
        Assert.assertEquals("maximal", 5, maximal.precision());
    }

    /**
     * Unit test {@link Config#functionList()}
     */
    @Test
    public void test_functionList() {
        Assert.assertEquals("badOption", Collections.emptyList(), badOption.functionList());
        Assert.assertEquals("requestHelp", Collections.emptyList(), requestHelp.functionList());
        Assert.assertEquals("minimal", Collections.emptyList(), minimal.functionList());
        Assert.assertEquals("maximal", List.of(Plus1.class), maximal.functionList());
    }

    /**
     * Unit test {@link Config#packageList()}
     */
    @Test
    public void test_packageList() {
        Assert.assertEquals("badOption", Collections.emptyList(), badOption.packageList());
        Assert.assertEquals("requestHelp", Collections.emptyList(), requestHelp.packageList());
        Assert.assertEquals("minimal", Collections.emptyList(), minimal.packageList());
        Assert.assertEquals("maximal", List.of(Plus2.class.getPackage()), maximal.packageList());
    }

    /**
     * Unit test {@link Config#getExpression()}
     */
    @Test
    public void test_getExpression() {
        Assert.assertEquals("badOption", "", badOption.getExpression());
        Assert.assertEquals("requestHelp", "", requestHelp.getExpression());
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
            stdOut.getLog().replaceAll("\r\n", "\n").startsWith("Usage:\n  java -jar evaluate-<version>.jar"));
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
            stdErr.getLog().contains("Error: Invalid package 'test.functions-unknown'"));
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
            stdErr.getLog().contains("Error: Invalid function 'test.function.unknown'"));
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
            stdErr.getLog().contains("Error: 'key' is already in the symbols table"));
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    public void test_badPropertiesFile() {
        Config config = Config.parse("--definitions", "path/to/unknown/file.xxxx");

        Assert.assertFalse("should not be valid", config.isValid());

        String log = stdErr.getLogWithNormalizedLineSeparator()
            .split("\n")[0]
            .replace('\\', '/');
        Assert.assertTrue("Error message not written",
            log.matches("Error: Can not read file '.*/path/to/unknown/file.xxxx'"));
    }
}