package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Rounding}
 */
public class RoundingTest {
    private static final BigDecimal SMALL_FRACTION = new BigDecimal("0.45");
    private static final BigDecimal HALF = new BigDecimal("0.5");
    private static final BigDecimal BIG_FRACTION = new BigDecimal("0.55");
    private static final BigDecimal LARGE = BigDecimal.valueOf(Long.MAX_VALUE)
        .add(BigDecimal.TEN)
        .add(HALF);


    /**
     * Unit test {@link Rounding#round(BigDecimal)}
     */
    @Test
    public void test_round() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().round(BigDecimal.ZERO));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().round(BigDecimal.ONE));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().round(BigDecimal.TEN));
        Assert.assertEquals("Small Fraction", new BigDecimal("0"), new Rounding().round(SMALL_FRACTION));
        Assert.assertEquals("Half", new BigDecimal("1"), new Rounding().round(HALF));
        Assert.assertEquals("Big Fraction", new BigDecimal("1"), new Rounding().round(BIG_FRACTION));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("0"), new Rounding().round(SMALL_FRACTION.negate()));
        Assert.assertEquals("Minus Half", new BigDecimal("-1"), new Rounding().round(HALF.negate()));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-1"), new Rounding().round(BIG_FRACTION.negate()));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775818"), new Rounding().round(LARGE));
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal)} 
     */
    @Test
    public void test_truncate() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().truncate(BigDecimal.ZERO));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().truncate(BigDecimal.ONE));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().truncate(BigDecimal.TEN));
        Assert.assertEquals("Small Fraction", new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION));
        Assert.assertEquals("Half", new BigDecimal("0"), new Rounding().truncate(HALF));
        Assert.assertEquals("Big Fraction", new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION.negate()));
        Assert.assertEquals("Minus Half", new BigDecimal("0"), new Rounding().truncate(HALF.negate()));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION.negate()));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817"), new Rounding().truncate(LARGE));
    }
}