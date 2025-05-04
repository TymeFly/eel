package com.github.tymefly.eel.function.general;

import com.github.tymefly.eel.exception.EelFailException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Fail}
 */
public class FailTest {
    /**
     * Unit test {@link Fail#fail(String)}
     */
    @Test
    public void test_Fail() {
        EelFailException actual = Assert.assertThrows(EelFailException.class,
            () -> new Fail().fail("Bye!"));

        Assert.assertEquals("Unexpected message", "Bye!", actual.getMessage());
    }
}