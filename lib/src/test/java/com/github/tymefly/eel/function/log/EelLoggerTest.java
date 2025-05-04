package com.github.tymefly.eel.function.log;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.Type;
import com.github.tymefly.eel.Value;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Unit test for {@link EelLogger}
 */
public class EelLoggerTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_noFormat() {
        Value actual = new EelLogger().error(Value.of("error\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "error\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: error\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_withFormat() {
        Value actual = new EelLogger().error(Value.of("\u0001This is at {}\n level"), Value.of(("error\t\n")));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "error\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: This is at error\t level"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_multipleArguments() {
        Value actual = new EelLogger().error(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_unlogged_value_returned() {
        Value actual = new EelLogger().error(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Number() {
        Value actual = new EelLogger().error(Value.of("My Number is {}"),
            Value.of(123));

        Assert.assertEquals("Unexpected type returned", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value returned", 123, actual.asInt());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Number is 123"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Logic() {
        Value actual = new EelLogger().error(Value.of("My Logic Value is {}"),
            Value.of(true));

        Assert.assertEquals("Unexpected type returned", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected value returned", actual.asLogic());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Logic Value is true"));
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Date() {
        ZonedDateTime myDate = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneId.of("UTC"));

        Value actual = new EelLogger().error(Value.of("My Date Value is {}"),
            Value.of(myDate));

        Assert.assertEquals("Unexpected type returned", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected value returned", myDate, actual.asDate());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Date Value is 2000-01-02T03:04:05Z"));
    }


    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_noFormat() {
        Value actual = new EelLogger().warn(Value.of("warn\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "warn\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: warn\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_withFormat() {
        Value actual = new EelLogger().warn(Value.of("\u0001This is at {}\n level"),
            Value.of("warn\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "warn\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: This is at warn\t level"));
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_multipleArguments() {
        Value actual = new EelLogger().warn(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_unlogged_value_returned() {
        Value actual = new EelLogger().warn(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_noFormat() {
        Value actual = new EelLogger().info(Value.of("info\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "info\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: info\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_withFormat() {
        Value actual = new EelLogger().info(Value.of("\u0001This is at {}\n level"), (Value.of("info\t\n")));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "info\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: This is at info\t level"));
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_multipleArguments() {
        Value actual = new EelLogger().info(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_unlogged_value_returned() {
        Value actual = new EelLogger().info(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_noFormat() {
        Value actual = new EelLogger().debug(Value.of("debug\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "debug\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: debug\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_withFormat() {
        Value actual = new EelLogger().debug(Value.of("\u0001This is at {}\n level"), Value.of("debug\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "debug\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: This is at debug\t level"));
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_multipleArguments() {
        Value actual = new EelLogger().debug(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_unlogged_value_returned() {
        Value actual = new EelLogger().debug(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_noFormat() {
        Value actual = new EelLogger().trace(Value.of("trace\t\nlevel"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "trace\t\nlevel", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: trace\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_withFormat() {
        Value actual = new EelLogger().trace(Value.of("\u0001This is at {}\n level"), Value.of("trace\t\n"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "trace\t\n", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: This is at trace\t level"));
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_multipleArguments() {
        Value actual = new EelLogger().trace(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "\u0003World\t", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_unlogged_value_returned() {
        Value actual = new EelLogger().trace(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        Assert.assertEquals("Unexpected type returned", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value returned", "xxx", actual.asText());
        Assert.assertTrue("Failed to log message: " + stdOut.getLinesNormalized(),
            stdOut.getLinesNormalized().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }
}