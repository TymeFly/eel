package com.github.tymefly.eel.function.util;

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
        Fail.EelFailException actual = Assert.assertThrows(Fail.EelFailException.class,
            () -> new Fail().fail("Bye!"));

        Assert.assertEquals("Unexpected message", "Bye!", actual.getMessage());
    }
}