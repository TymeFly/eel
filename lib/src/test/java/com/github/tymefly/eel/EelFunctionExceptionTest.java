package com.github.tymefly.eel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

        assertEquals("Test Message", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelFunctionException}
     */
    @Test
    public void test_FormattedMessage() {
        Exception actual = new EelFunctionException("Hello %s - %d", "World", 12);

        assertEquals("Hello World - 12", actual.getMessage(), "Unexpected message");
        assertNull(actual.getCause(), "Unexpected cause");
    }

    /**
     * Unit test {@link EelFunctionException}
     */
    @Test
    public void test_withCause() {
        Exception cause = new Exception("cause");
        Exception actual = new EelFunctionException("Test", cause);

        assertEquals("Test", actual.getMessage(), "Unexpected message");
        assertSame(cause, actual.getCause(), "Unexpected cause");
    }
}