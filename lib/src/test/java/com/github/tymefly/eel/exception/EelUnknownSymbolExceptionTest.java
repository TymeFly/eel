package com.github.tymefly.eel.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelUnknownSymbolException}
 */
public class EelUnknownSymbolExceptionTest {
    /**
     * Unit test {@link EelUnknownSymbolException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelUnknownSymbolException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelUnknownSymbolException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelUnknownSymbolException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelUnknownSymbolException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelUnknownSymbolException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}