package com.github.tymefly.eel;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelSourceException}
 */
public class EelSourceExceptionTest {
    /**
     * Unit test {@link EelSourceException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelSourceException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSourceException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelSourceException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSourceException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSourceException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}