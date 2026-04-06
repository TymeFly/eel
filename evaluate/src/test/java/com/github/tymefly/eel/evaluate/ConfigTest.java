package com.github.tymefly.eel.evaluate;

import java.io.File;
import java.net.URI;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.functions1.Plus1;
import test.functions2.Plus2;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit test for {@link Config}
 */
@ExtendWith(SystemStubsExtension.class)
class ConfigTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    private Config badOption;
    private Config scriptAndExpression;
    private Config requestHelp;
    private Config requestVersion;
    private Config scripted;
    private Config minimal;
    private Config maximal;


    @BeforeEach
    void setUp() throws Exception {
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

        stdErr.clear(); // because badOption will pollute the standard Error
    }


    /**
     * Unit test {@link Config#isValid()}
     */
    @Test
    void test_isValid() {
        assertFalse(badOption.isValid(), "badOption");
        assertFalse(scriptAndExpression.isValid(), "scriptAndExpression");
        assertTrue(requestHelp.isValid(), "requestHelp");
        assertTrue(requestVersion.isValid(), "requestVersion");
        assertTrue(scripted.isValid(), "scripted");
        assertTrue(minimal.isValid(), "minimal");
        assertTrue(maximal.isValid(), "maximal");
    }

    /**
     * Unit test {@link Config#requestHelp()}
     */
    @Test
    void test_requestHelp() {
        assertFalse(badOption.requestHelp(), "badOption");
        assertFalse(scriptAndExpression.requestHelp(), "scriptAndExpression");
        assertTrue(requestHelp.requestHelp(), "requestHelp");
        assertFalse(requestVersion.requestHelp(), "requestVersion");
        assertFalse(scripted.requestHelp(), "scripted");
        assertFalse(minimal.requestHelp(), "minimal");
        assertFalse(maximal.requestHelp(), "maximal");
    }

    /**
     * Unit test {@link Config#requestVersion()}
     */
    @Test
    void test_requestVersion() {
        assertFalse(badOption.requestVersion(), "badOption");
        assertFalse(scriptAndExpression.requestVersion(), "scriptAndExpression");
        assertFalse(requestHelp.requestVersion(), "requestHelp");
        assertTrue(requestVersion.requestVersion(), "requestVersion");
        assertFalse(scripted.requestVersion(), "scripted");
        assertFalse(minimal.requestVersion(), "minimal");
        assertFalse(maximal.requestVersion(), "maximal");
    }

    /**
     * Unit test {@link Config#verbose()}
     */
    @Test
    void test_verbose() {
        assertFalse(badOption.verbose(), "badOption");
        assertFalse(scriptAndExpression.verbose(), "scriptAndExpression");
        assertFalse(requestHelp.verbose(), "requestHelp");
        assertFalse(requestVersion.verbose(), "requestVersion");
        assertFalse(scripted.verbose(), "scripted");
        assertFalse(minimal.verbose(), "minimal");
        assertTrue(maximal.verbose(), "maximal");
    }

    /**
     * Unit test {@link Config#useEnvironmentVariables()}
     */
    @Test
    void test_useEnvironmentVariables() {
        assertFalse(badOption.useEnvironmentVariables(), "badOption");
        assertFalse(scriptAndExpression.useEnvironmentVariables(), "scriptAndExpression");
        assertFalse(requestHelp.useEnvironmentVariables(), "requestHelp");
        assertFalse(requestVersion.useEnvironmentVariables(), "requestVersion");
        assertFalse(scripted.useEnvironmentVariables(), "scripted");
        assertFalse(minimal.useEnvironmentVariables(), "minimal");
        assertTrue(maximal.useEnvironmentVariables(), "maximal");
    }

    /**
     * Unit test {@link Config#useProperties()}
     */
    @Test
    void test_useProperties() {
        assertFalse(badOption.useProperties(), "badOption");
        assertFalse(scriptAndExpression.useProperties(), "scriptAndExpression");
        assertFalse(requestHelp.useProperties(), "requestHelp");
        assertFalse(requestVersion.useProperties(), "requestVersion");
        assertFalse(scripted.useProperties(), "scripted");
        assertFalse(minimal.useProperties(), "minimal");
        assertTrue(maximal.useProperties(), "maximal");
    }

    /**
     * Unit test {@link Config#definitions()}
     */
    @Test
    void test_definitions() {
        assertEquals(Collections.emptyMap(), badOption.definitions(), "badOption");
        assertEquals(Collections.emptyMap(), scriptAndExpression.definitions(), "scriptAndExpression");
        assertEquals(Collections.emptyMap(), requestHelp.definitions(), "requestHelp");
        assertEquals(Collections.emptyMap(), requestVersion.definitions(), "requestVersion");
        assertEquals(Collections.emptyMap(), scripted.definitions(), "scripted");
        assertEquals(Collections.emptyMap(), minimal.definitions(), "minimal");
        assertEquals(
            Map.ofEntries(
                Map.entry("key1", "value1"),
                Map.entry("key2", ""),
                Map.entry("a", "true"),
                Map.entry("b", "123"),
                Map.entry("c", "Hello World"),
                Map.entry("d", "2000-01-02T03:04:05Z")),
            maximal.definitions(),
            "maximal");
    }

    /**
     * Unit test {@link Config#defaultValue()}
     */
    @Test
    void test_defaultValue() {
        assertNull(badOption.defaultValue(), "badOption");
        assertNull(scriptAndExpression.defaultValue(), "scriptAndExpression");
        assertNull(requestHelp.defaultValue(), "requestHelp");
        assertNull(requestVersion.defaultValue(), "requestVersion");
        assertNull(scripted.defaultValue(), "scripted");
        assertNull(minimal.defaultValue(), "minimal");
        assertEquals("<undefined>", maximal.defaultValue(), "maximal");
    }

    /**
     * Unit test {@link Config#precision()}
     */
    @Test
    void test_precision() {
        assertEquals(EelContext.DEFAULT_PRECISION, badOption.precision(), "badOption");
        assertEquals(EelContext.DEFAULT_PRECISION, scriptAndExpression.precision(), "scriptAndExpression");
        assertEquals(EelContext.DEFAULT_PRECISION, requestHelp.precision(), "requestHelp");
        assertEquals(EelContext.DEFAULT_PRECISION, requestVersion.precision(), "requestVersion");
        assertEquals(EelContext.DEFAULT_PRECISION, scripted.precision(), "scripted");
        assertEquals(EelContext.DEFAULT_PRECISION, minimal.precision(), "minimal");
        assertEquals(5, maximal.precision(), "maximal");
    }

    /**
     * Unit test {@link Config#ioLimit()}
     */
    @Test
    void test_ioLimit() {
        assertEquals(EelContext.DEFAULT_IO_LIMIT, badOption.ioLimit(), "badOption");
        assertEquals(EelContext.DEFAULT_IO_LIMIT, scriptAndExpression.ioLimit(), "scriptAndExpression");
        assertEquals(EelContext.DEFAULT_IO_LIMIT, requestHelp.ioLimit(), "requestHelp");
        assertEquals(EelContext.DEFAULT_IO_LIMIT, requestVersion.ioLimit(), "requestVersion");
        assertEquals(EelContext.DEFAULT_IO_LIMIT, scripted.ioLimit(), "scripted");
        assertEquals(EelContext.DEFAULT_IO_LIMIT, minimal.ioLimit(), "minimal");
        assertEquals(2048, maximal.ioLimit(), "maximal");
    }

    /**
     * Unit test {@link Config#daysInFirstWeek()}
     */
    @Test
    void test_daysInFirstWeek() {
        assertEquals(4, badOption.daysInFirstWeek(), "badOption");
        assertEquals(4, scriptAndExpression.daysInFirstWeek(), "scriptAndExpression");
        assertEquals(4, requestHelp.daysInFirstWeek(), "requestHelp");
        assertEquals(4, requestVersion.daysInFirstWeek(), "requestVersion");
        assertEquals(4, scripted.daysInFirstWeek(), "scripted");
        assertEquals(4, minimal.daysInFirstWeek(), "minimal");
        assertEquals(2, maximal.daysInFirstWeek(), "maximal");
    }

    /**
     * Unit test {@link Config#startOfWeek()}
     */
    @Test
    void test_startOfWeek() {
        assertEquals(DayOfWeek.MONDAY, badOption.startOfWeek(), "badOption");
        assertEquals(DayOfWeek.MONDAY, scriptAndExpression.startOfWeek(), "scriptAndExpression");
        assertEquals(DayOfWeek.MONDAY, requestHelp.startOfWeek(), "requestHelp");
        assertEquals(DayOfWeek.MONDAY, requestVersion.startOfWeek(), "requestVersion");
        assertEquals(DayOfWeek.MONDAY, scripted.startOfWeek(), "scripted");
        assertEquals(DayOfWeek.MONDAY, minimal.startOfWeek(), "minimal");
        assertEquals(DayOfWeek.SUNDAY, maximal.startOfWeek(), "maximal");

        assertFalse(Config.parse("--start-of-week", "Notaday", "myExpression").isValid(), "Invalid day");
    }

    /**
     * Unit test {@link Config#functionList()}
     */
    @Test
    void test_functionList() {
        assertEquals(Collections.emptyList(), badOption.functionList(), "badOption");
        assertEquals(Collections.emptyList(), scriptAndExpression.functionList(), "scriptAndExpression");
        assertEquals(Collections.emptyList(), requestHelp.functionList(), "requestHelp");
        assertEquals(Collections.emptyList(), requestVersion.functionList(), "requestVersion");
        assertEquals(Collections.emptyList(), scripted.functionList(), "scripted");
        assertEquals(Collections.emptyList(), minimal.functionList(), "minimal");
        assertEquals(List.of(Plus1.class), maximal.functionList(), "maximal");
    }

    /**
     * Unit test {@link Config#packageList()}
     */
    @Test
    void test_packageList() {
        assertEquals(Collections.emptyList(), badOption.packageList(), "badOption");
        assertEquals(Collections.emptyList(), scriptAndExpression.packageList(), "scriptAndExpression");
        assertEquals(Collections.emptyList(), requestHelp.packageList(), "requestHelp");
        assertEquals(Collections.emptyList(), requestVersion.packageList(), "requestVersion");
        assertEquals(Collections.emptyList(), scripted.packageList(), "scripted");
        assertEquals(Collections.emptyList(), minimal.packageList(), "minimal");
        assertEquals(List.of(Plus2.class.getPackage()), maximal.packageList(), "maximal");
    }

    /**
     * Unit test {@link Config#getScriptFile()}
     */
    @Test
    void test_getScriptFile() {
        assertNull(badOption.getScriptFile(), "badOption");
        assertEquals(new File("test.eel"), scriptAndExpression.getScriptFile(), "scriptAndExpression");
        assertNull(requestHelp.getScriptFile(), "requestHelp");
        assertNull(requestVersion.getScriptFile(), "requestVersion");
        assertEquals(new File("test.eel"), scripted.getScriptFile(), "scripted");
        assertNull(minimal.getScriptFile(), "minimal");
        assertNull(maximal.getScriptFile(), "maximal");
    }

    /**
     * Unit test {@link Config#getExpression()}
     */
    @Test
    void test_getExpression() {
        assertNull(badOption.getExpression(), "badOption");
        assertEquals("myExpression", scriptAndExpression.getExpression(), "scriptAndExpression");
        assertNull(requestHelp.getExpression(), "requestHelp");
        assertNull(requestVersion.getExpression(), "requestVersion");
        assertNull(scripted.getExpression(), "scripted");
        assertEquals("minimal", minimal.getExpression(), "minimal");
        assertEquals("maximal", maximal.getExpression(), "maximal");
    }

    /**
     * Unit test {@link Config#displayUsage()}
     */
    @Test
    void test_displayUsage() {
        minimal.displayUsage();

        assertTrue(stdOut.getLinesNormalized().replaceAll("\r\n", "\n")
                .startsWith("Usage:\n  java -jar evaluate-<version>.jar"),
            "Help message not written");
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    void test_badPackage() {
        Config config = Config.parse("--udf-package", "test.functions-unknown", "should fail");

        assertFalse(config.isValid(), "should not be valid");
        assertTrue(stdErr.getLinesNormalized().contains("Error: Invalid package 'test.functions-unknown'"),
            "Help message not written");
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    void test_badFunction() {
        Config config = Config.parse("--udf-class", "test.function.unknown", "should fail");

        assertFalse(config.isValid(), "should not be valid");
        assertTrue(stdErr.getLinesNormalized().contains("Error: Invalid function 'test.function.unknown'"),
            "Help message not written");
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    void test_duplicateDefinition() {
        Config config = Config.parse("--define", "key=value1", "-D", "key=value2", "should fail");

        assertFalse(config.isValid(), "should not be valid");
        assertTrue(stdErr.getLinesNormalized().contains("Error: 'key' is already in the SymbolsTable"),
            "Error message not written");
    }

    /**
     * Unit test {@link Config}
     */
    @Test
    void test_badPropertiesFile() {
        Config config = Config.parse("--definitions", "path/to/unknown/file.xxxx");

        assertFalse(config.isValid(), "should not be valid");

        String log = stdErr.getLinesNormalized().split("\n")[0].replace('\\', '/');
        assertTrue(log.matches("Error: Can not read file '.*/path/to/unknown/file.xxxx'"),
            "Error message not written");
    }
}
