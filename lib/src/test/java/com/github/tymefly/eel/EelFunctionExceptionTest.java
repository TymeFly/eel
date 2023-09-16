package com.github.tymefly.eel;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelFunctionException}
 */
public class EelFunctionExceptionTest {
    /**
     * Unit test {@link EelFunctionException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelFunctionException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelFunctionException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelFunctionException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelFunctionException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelFunctionException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}