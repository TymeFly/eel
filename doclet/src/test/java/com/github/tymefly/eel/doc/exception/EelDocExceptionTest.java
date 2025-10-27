package com.github.tymefly.eel.doc.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelDocException}
 */
public class EelDocExceptionTest {
    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelDocException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelDocException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelDocException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }


    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_FormattedMessageAndCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelDocException("Hello %s - %d", "World", 12, cause);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}