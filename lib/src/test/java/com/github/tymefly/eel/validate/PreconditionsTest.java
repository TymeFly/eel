package com.github.tymefly.eel.validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Preconditions}
 */
public class PreconditionsTest {

    /**
     * Unit test {@link Preconditions#checkNotNull}
     */
    @Test
    public void test_checkNotNull() {
        String result = Preconditions.checkNotNull("Not Null", "Hello %s", "world");

        assertSame("Not Null", result, "Unexpected result");

        Exception actual = assertThrows(NullPointerException.class,
                () -> Preconditions.checkNotNull(null, "Hello %s %d", "world", 1));

        assertEquals("Hello world 1", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link Preconditions#checkState}
     */
    @Test
    public void test_checkState() {
        Preconditions.checkState(true, "Hello %s", "world");

        Exception actual = assertThrows(IllegalStateException.class,
                () -> Preconditions.checkState(false, "Hello %s %d", "world", 1));

        assertEquals("Hello world 1", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link Preconditions#checkArgument}
     */
    @Test
    public void test_checkArgument() {
        Preconditions.checkArgument(true, "Hello %s", "world");

        Exception actual = assertThrows(IllegalArgumentException.class,
                () -> Preconditions.checkArgument(false, "Hello %s %d", "world", 1));

        assertEquals("Hello world 1", actual.getMessage(), "Unexpected message");
    }
}