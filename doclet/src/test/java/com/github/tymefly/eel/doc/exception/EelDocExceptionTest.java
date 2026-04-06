package com.github.tymefly.eel.doc.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

        assertEquals("Test Message", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelDocException("Hello %s - %d", "World", 12);

        assertEquals("Hello World - 12", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelDocException("Test", cause);

        assertEquals("Test", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelDocException}
     */
    @Test
    public void test_FormattedMessageAndCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelDocException("Hello %s - %d", "World", 12, cause);

        assertEquals("Hello World - 12", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }
}
