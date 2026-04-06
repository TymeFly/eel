package com.github.tymefly.eel.function.log;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.Type;
import com.github.tymefly.eel.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link EelLogger}
 */
@ExtendWith(SystemStubsExtension.class)
public class EelLoggerTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_noFormat() {
        Value actual = new EelLogger().error(Value.of("error\t\nlevel"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("error\t\nlevel", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: error\tlevel"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_withFormat() {
        Value actual = new EelLogger().error(Value.of("\u0001This is at {}\n level"), Value.of(("error\t\n")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("error\t\n", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: This is at error\t level"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_multipleArguments() {
        Value actual = new EelLogger().error(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("\u0003World\t", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_unlogged_value_returned() {
        Value actual = new EelLogger().error(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("xxx", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Number() {
        Value actual = new EelLogger().error(Value.of("My Number is {}"),
            Value.of(123));

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type returned");
        assertEquals(123, actual.asInt(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Number is 123"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Logic() {
        Value actual = new EelLogger().error(Value.of("My Logic Value is {}"),
            Value.of(true));

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type returned");
        assertTrue(actual.asLogic(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Logic Value is true"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#error(Value, Value...)}
     */
    @Test
    public void test_error_Date() {
        ZonedDateTime myDate = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneId.of("UTC"));

        Value actual = new EelLogger().error(Value.of("My Date Value is {}"),
            Value.of(myDate));

        assertEquals(Type.DATE, actual.getType(), "Unexpected type returned");
        assertEquals(myDate, actual.asDate(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: My Date Value is 2000-01-02T03:04:05Z"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }


    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_noFormat() {
        Value actual = new EelLogger().warn(Value.of("warn\t\nlevel"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("warn\t\nlevel", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: warn\tlevel"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_withFormat() {
        Value actual = new EelLogger().warn(Value.of("\u0001This is at {}\n level"),
            Value.of("warn\t\n"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("warn\t\n", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: This is at warn\t level"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_multipleArguments() {
        Value actual = new EelLogger().warn(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("\u0003World\t", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#warn(Value, Value...)}
     */
    @Test
    public void test_warn_unlogged_value_returned() {
        Value actual = new EelLogger().warn(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("xxx", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }


    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_noFormat() {
        Value actual = new EelLogger().info(Value.of("info\t\nlevel"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("info\t\nlevel", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: info\tlevel"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_withFormat() {
        Value actual = new EelLogger().info(Value.of("\u0001This is at {}\n level"), (Value.of("info\t\n")));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("info\t\n", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: This is at info\t level"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_multipleArguments() {
        Value actual = new EelLogger().info(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        assertEquals("\u0003World\t", actual.asText(), "Unexpected value returned");
        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#info(Value, Value...)}
     */
    @Test
    public void test_info_unlogged_value_returned() {
        Value actual = new EelLogger().info(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("xxx", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }


    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_noFormat() {
        Value actual = new EelLogger().debug(Value.of("debug\t\nlevel"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("debug\t\nlevel", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: debug\tlevel"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_withFormat() {
        Value actual = new EelLogger().debug(Value.of("\u0001This is at {}\n level"), Value.of("debug\t\n"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("debug\t\n", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: This is at debug\t level"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_multipleArguments() {
        Value actual = new EelLogger().debug(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("\u0003World\t", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#debug(Value, Value...)}
     */
    @Test
    public void test_debug_unlogged_value_returned() {
        Value actual = new EelLogger().debug(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("xxx", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }


    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_noFormat() {
        Value actual = new EelLogger().trace(Value.of("trace\t\nlevel"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("trace\t\nlevel", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: trace\tlevel"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_withFormat() {
        Value actual = new EelLogger().trace(Value.of("\u0001This is at {}\n level"), Value.of("trace\t\n"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("trace\t\n", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: This is at trace\t level"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_multipleArguments() {
        Value actual = new EelLogger().trace(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("\u0003World\t", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }

    /**
     * Unit test {@link EelLogger#trace(Value, Value...)}
     */
    @Test
    public void test_trace_unlogged_value_returned() {
        Value actual = new EelLogger().trace(Value.of("\u0001{} {}\n!"),
            Value.of("\u0001Hello"), Value.of("\u0003World\t"), Value.of("xxx"));

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type returned");
        assertEquals("xxx", actual.asText(), "Unexpected value returned");
        assertTrue(stdOut.getLinesNormalized()
                .contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"),
            "Failed to log message: " + stdOut.getLinesNormalized());
    }
}