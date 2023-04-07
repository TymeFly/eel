package com.github.tymefly.eel.function.util;

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
     * Unit test {@link EelLogger#error(String, String...)}
     */
    @Test
    public void test_error_noFormat() {
        String actual = new EelLogger().error("error\t\nlevel");

        Assert.assertEquals("Unexpected value returned", "error\tlevel", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: error\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#error(String, String...)}
     */
    @Test
    public void test_error_withFormat() {
        String actual = new EelLogger().error("\u0001This is at {}\n level", "error\t\n");

        Assert.assertEquals("Unexpected value returned", "error\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: This is at error\t level"));
    }

    /**
     * Unit test {@link EelLogger#error(String, String...)}
     */
    @Test
    public void test_error_multipleArguments() {
        String actual = new EelLogger().error("\u0001{} {}\n!", "\u0001Hello", "\u0003World\t");

        Assert.assertEquals("Unexpected value returned", "World\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[ERROR] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#warn(String, String...)}
     */
    @Test
    public void test_warn_noFormat() {
        String actual = new EelLogger().warn("warn\t\nlevel");

        Assert.assertEquals("Unexpected value returned", "warn\tlevel", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: warn\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#warn(String, String...)}
     */
    @Test
    public void test_warn_withFormat() {
        String actual = new EelLogger().warn("\u0001This is at {}\n level", "warn\t\n");

        Assert.assertEquals("Unexpected value returned", "warn\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: This is at warn\t level"));
    }

    /**
     * Unit test {@link EelLogger#warn(String, String...)}
     */
    @Test
    public void test_warn_multipleArguments() {
        String actual = new EelLogger().warn("\u0001{} {}\n!", "\u0001Hello", "\u0003World\t");

        Assert.assertEquals("Unexpected value returned", "World\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[WARN ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }

    /**
     * Unit test {@link EelLogger#info(String, String...)}
     */
    @Test
    public void test_info_noFormat() {
        String actual = new EelLogger().info("info\t\nlevel");

        Assert.assertEquals("Unexpected value returned", "info\tlevel", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: info\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#info(String, String...)}
     */
    @Test
    public void test_info_withFormat() {
        String actual = new EelLogger().info("\u0001This is at {}\n level", "info\t\n");

        Assert.assertEquals("Unexpected value returned", "info\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: This is at info\t level"));
    }

    /**
     * Unit test {@link EelLogger#info(String, String...)}
     */
    @Test
    public void test_info_multipleArguments() {
        String actual = new EelLogger().info("\u0001{} {}\n!", "\u0001Hello", "\u0003World\t");

        Assert.assertEquals("Unexpected value returned", "World\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#debug(String, String...)}
     */
    @Test
    public void test_debug_noFormat() {
        String actual = new EelLogger().debug("debug\t\nlevel");

        Assert.assertEquals("Unexpected value returned", "debug\tlevel", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: debug\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#debug(String, String...)}
     */
    @Test
    public void test_debug_withFormat() {
        String actual = new EelLogger().debug("\u0001This is at {}\n level", "debug\t\n");

        Assert.assertEquals("Unexpected value returned", "debug\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: This is at debug\t level"));
    }

    /**
     * Unit test {@link EelLogger#debug(String, String...)}
     */
    @Test
    public void test_debug_multipleArguments() {
        String actual = new EelLogger().debug("\u0001{} {}\n!", "\u0001Hello", "\u0003World\t");

        Assert.assertEquals("Unexpected value returned", "World\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[DEBUG] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }


    /**
     * Unit test {@link EelLogger#trace(String, String...)}
     */
    @Test
    public void test_trace_noFormat() {
        String actual = new EelLogger().trace("trace\t\nlevel");

        Assert.assertEquals("Unexpected value returned", "trace\tlevel", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: trace\tlevel"));
    }

    /**
     * Unit test {@link EelLogger#trace(String, String...)}
     */
    @Test
    public void test_trace_withFormat() {
        String actual = new EelLogger().trace("\u0001This is at {}\n level", "trace\t\n");

        Assert.assertEquals("Unexpected value returned", "trace\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: This is at trace\t level"));
    }

    /**
     * Unit test {@link EelLogger#trace(String, String...)}
     */
    @Test
    public void test_trace_multipleArguments() {
        String actual = new EelLogger().trace("\u0001{} {}\n!", "\u0001Hello", "\u0003World\t");

        Assert.assertEquals("Unexpected value returned", "World\t", actual);
        Assert.assertTrue("Failed to log message: " + stdOut.getLog(),
            stdOut.getLog().contains("[TRACE] com.github.tymefly.eel.log - Logged EEL Message: Hello World\t!"));
    }
}