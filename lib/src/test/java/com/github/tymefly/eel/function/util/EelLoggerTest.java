package com.github.tymefly.eel.function.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.Type;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Unit test for {@link EelLogger}
 */
public class EelLoggerTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_noFormat() {
        EelValue actual = new EelLogger().error(EelValue.of("error\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "error\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: error\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_withFormat() {
        EelValue actual = new EelLogger().error(EelValue.of("\u0001This is at {}\n level"), EelValue.of(("error\t\n")));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "error\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: This is at error\t level"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_multipleArguments() {
        EelValue actual = new EelLogger().error(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_unlogged_value_returned() {
        EelValue actual = new EelLogger().error(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"), EelValue.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_Number() {
        EelValue actual = new EelLogger().error(EelValue.of("My Number is {}"),
            EelValue.of(123));

        Assert.assertEquals("Unexpected type returned", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value returned", 123, actual.asNumber().intValue());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Number is 123"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_Logic() {
        EelValue actual = new EelLogger().error(EelValue.of("My Logic Value is {}"),
            EelValue.of(true));

        Assert.assertEquals("Unexpected type returned", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected value returned", actual.asLogic());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Logic Value is true"));
    }

    /**
     * Unit test {@link EelLogger#error(EelValue, EelValue...)}
     */
    @Test
    public void test_error_Date() {
        ZonedDateTime myDate = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneId.of("UTC"));

        EelValue actual = new EelLogger().error(EelValue.of("My Date Value is {}"),
            EelValue.of(myDate));

        Assert.assertEquals("Unexpected type returned", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected value returned", myDate, actual.asDate());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Date Value is 2000-01-02T03:04:05Z"));
    }


    /**
     * Unit test {@link EelLogger#warn(EelValue, EelValue...)}
     */
    @Test
    public void test_warn_noFormat() {
        EelValue actual = new EelLogger().warn(EelValue.of("warn\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "warn\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: warn\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#warn(EelValue, EelValue...)}
     */
    @Test
    public void test_warn_withFormat() {
        EelValue actual = new EelLogger().warn(EelValue.of("\u0001This is at {}\n level"),
            EelValue.of("warn\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "warn\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: This is at warn\t level"));
    }

    /**
     * Unit test {@link EelLogger#warn(EelValue, EelValue...)}
     */
    @Test
    public void test_warn_multipleArguments() {
        EelValue actual = new EelLogger().warn(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#warn(EelValue, EelValue...)}
     */
    @Test
    public void test_warn_unlogged_value_returned() {
        EelValue actual = new EelLogger().warn(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"), EelValue.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#info(EelValue, EelValue...)}
     */
    @Test
    public void test_info_noFormat() {
        EelValue actual = new EelLogger().info(EelValue.of("info\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "info\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: info\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#info(EelValue, EelValue...)}
     */
    @Test
    public void test_info_withFormat() {
        EelValue actual = new EelLogger().info(EelValue.of("\u0001This is at {}\n level"), (EelValue.of("info\t\n")));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "info\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: This is at info\t level"));
    }

    /**
     * Unit test {@link EelLogger#info(EelValue, EelValue...)}
     */
    @Test
    public void test_info_multipleArguments() {
        EelValue actual = new EelLogger().info(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"));

        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#info(EelValue, EelValue...)}
     */
    @Test
    public void test_info_unlogged_value_returned() {
        EelValue actual = new EelLogger().info(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"), EelValue.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#debug(EelValue, EelValue...)}
     */
    @Test
    public void test_debug_noFormat() {
        EelValue actual = new EelLogger().debug(EelValue.of("debug\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "debug\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: debug\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#debug(EelValue, EelValue...)}
     */
    @Test
    public void test_debug_withFormat() {
        EelValue actual = new EelLogger().debug(EelValue.of("\u0001This is at {}\n level"), EelValue.of("debug\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "debug\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: This is at debug\t level"));
    }

    /**
     * Unit test {@link EelLogger#debug(EelValue, EelValue...)}
     */
    @Test
    public void test_debug_multipleArguments() {
        EelValue actual = new EelLogger().debug(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#debug(EelValue, EelValue...)}
     */
    @Test
    public void test_debug_unlogged_value_returned() {
        EelValue actual = new EelLogger().debug(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"), EelValue.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#trace(EelValue, EelValue...)}
     */
    @Test
    public void test_trace_noFormat() {
        EelValue actual = new EelLogger().trace(EelValue.of("trace\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "trace\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: trace\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#trace(EelValue, EelValue...)}
     */
    @Test
    public void test_trace_withFormat() {
        EelValue actual = new EelLogger().trace(EelValue.of("\u0001This is at {}\n level"), EelValue.of("trace\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "trace\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: This is at trace\t level"));
    }

    /**
     * Unit test {@link EelLogger#trace(EelValue, EelValue...)}
     */
    @Test
    public void test_trace_multipleArguments() {
        EelValue actual = new EelLogger().trace(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#trace(EelValue, EelValue...)}
     */
    @Test
    public void test_trace_unlogged_value_returned() {
        EelValue actual = new EelLogger().trace(EelValue.of("\u0001{} {}\n!"),
            EelValue.of("\u0001Hello"), EelValue.of("\u0003World\t"), EelValue.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }
}