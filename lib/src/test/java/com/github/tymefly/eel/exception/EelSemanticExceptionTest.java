package com.github.tymefly.eel.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit test for {@link EelSemanticException}
 */
public class EelSemanticExceptionTest {
    /**
     * Unit test {@link EelSemanticException}
     */
    @Test
    public void test_Message() {
        Exception actual = new EelSemanticException(123, "Test Message");

        assertEquals("Error at position 123: Test Message", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelSemanticException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelSemanticException(123, "Hello %s - %d", "World", 12);

        assertEquals("Error at position 123: Hello World - 12", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelSemanticException}
     */
    @Test
    public void test_FormattedMessage_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSemanticException(123, "Hello %s - %d", "World", 12, cause);

        assertEquals("Error at position 123: Hello World - 12", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelSemanticException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelSemanticException(123, "Test", cause);

        assertEquals("Error at position 123: Test", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }
}