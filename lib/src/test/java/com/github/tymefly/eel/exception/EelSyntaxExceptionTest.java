package com.github.tymefly.eel.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelSyntaxException}
 */
public class EelSyntaxExceptionTest {
    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelSyntaxException(123, "Test Message");

        Assert.assertEquals("Unexpected message", "Error at position 123: Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelSyntaxException(123, "Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Error at position 123: Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_FormattedMessage_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSyntaxException(123, "Hello %s - %d", "World", 12, cause);

        Assert.assertEquals("Unexpected message", "Error at position 123: Hello World - 12", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }

    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSyntaxException(123, "Test", cause);

        Assert.assertEquals("Unexpected message", "Error at position 123: Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}