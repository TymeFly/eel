package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Sgn}
 * @see Abs
 */
public class SgnTest {

    /**
     * Unit test {@link Sgn#sgn}
     */
    @Test
    public void test_sgn() {
        Assert.assertEquals("negative value", -1, new Sgn().sgn(new BigDecimal("-0.1")));
        Assert.assertEquals("zero", 0, new Sgn().sgn(BigDecimal.ZERO));
        Assert.assertEquals("positive value", 1, new Sgn().sgn(new BigDecimal("0.1")));
    }
}