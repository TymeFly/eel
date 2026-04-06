package com.github.tymefly.eel.function.general;

import com.github.tymefly.eel.exception.EelFailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Fail}
 */
public class FailTest {
    /**
     * Unit test {@link Fail#fail(String)}
     */
    @Test
    public void test_Fail() {
        EelFailException actual = assertThrows(EelFailException.class,
            () -> new Fail().fail("Bye!"));

        assertEquals("Bye!", actual.getMessage(), "Unexpected message");
    }
}