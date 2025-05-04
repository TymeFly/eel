package com.github.tymefly.eel.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelInternalException}
 */
public class EelInternalExceptionTest {
    /**
     * Unit test {@link EelInternalException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelInternalException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelInternalException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelInternalException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_FormattedMessage_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelInternalException("Hello %s - %d", "World", 12, cause);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }

    /**
     * Unit test {@link EelInternalException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelInternalException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}