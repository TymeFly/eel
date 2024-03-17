package com.github.tymefly.eel.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelSymbolsTableException}
 */
public class EelSymbolsTableExceptionTest {
    /**
     * Unit test {@link EelSymbolsTableException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelSymbolsTableException("Test Message");

        Assert.assertEquals("Unexpected message", "Test Message", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSymbolsTableException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelSymbolsTableException("Hello %s - %d", "World", 12);

        Assert.assertEquals("Unexpected message", "Hello World - 12", actual.getMessage());
        Assert.assertNull("Unexpected cause", actual.getCause());
    }

    /**
     * Unit test {@link EelSymbolsTableException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSymbolsTableException("Test", cause);

        Assert.assertEquals("Unexpected message", "Test", actual.getMessage());
        Assert.assertSame("Unexpected cause", cause, actual.getCause());
    }
}