package com.github.tymefly.eel.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit test for {@link EelInterruptedException}
 */
public class EelInterruptedExceptionTest {
    /**
     * Unit test {@link EelInterruptedException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelInterruptedException("Test Message");

        assertEquals("Test Message", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelInterruptedException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelInterruptedException("Hello %s - %d", "World", 12);

        assertEquals("Hello World - 12", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelSyntaxException}
     */
    @Test
    public void test_FormattedMessage_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelInterruptedException("Hello %s - %d", "World", 12, cause);

        assertEquals("Hello World - 12", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelInterruptedException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelInterruptedException("Test", cause);

        assertEquals("Test", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }
}