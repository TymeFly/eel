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

        Assert.assertEquals("1.1", new BigDecimal("1"), new Rounding().round(new BigDecimal("1.1")));
        Assert.assertEquals("1.6", new BigDecimal("2"), new Rounding().round(new BigDecimal("1.6")));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1")));
        Assert.assertEquals("-1.1", new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1.1")));
        Assert.assertEquals("-1.6", new BigDecimal("-2"), new Rounding().round(new BigDecimal("-1.6")));
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

        Assert.assertEquals("1.1", new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.1")));
        Assert.assertEquals("1.6", new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.6")));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1")));
        Assert.assertEquals("-1.1", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.1")));
        Assert.assertEquals("-1.6", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.6")));
    }

    /**
     * Unit test {@link Rounding#ceil(BigDecimal)}
     */
    @Test
    public void test_ceil() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().ceil(BigDecimal.ZERO));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().ceil(BigDecimal.ONE));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().ceil(BigDecimal.TEN));
        Assert.assertEquals("Small Fraction", new BigDecimal("1"), new Rounding().ceil(SMALL_FRACTION));
        Assert.assertEquals("Half", new BigDecimal("1"), new Rounding().ceil(HALF));
        Assert.assertEquals("Big Fraction", new BigDecimal("1"), new Rounding().ceil(BIG_FRACTION));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("0"), new Rounding().ceil(SMALL_FRACTION.negate()));
        Assert.assertEquals("Minus Half", new BigDecimal("0"), new Rounding().ceil(HALF.negate()));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("0"), new Rounding().ceil(BIG_FRACTION.negate()));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775818"), new Rounding().ceil(LARGE));

        Assert.assertEquals("1.1", new BigDecimal("2"), new Rounding().ceil(new BigDecimal("1.1")));
        Assert.assertEquals("1.6", new BigDecimal("2"), new Rounding().ceil(new BigDecimal("1.6")));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1")));
        Assert.assertEquals("-1.1", new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1.1")));
        Assert.assertEquals("-1.6", new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1.6")));
    }

    /**
     * Unit test {@link Rounding#floor(BigDecimal)}
     */
    @Test
    public void test_floor() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().floor(BigDecimal.ZERO));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().floor(BigDecimal.ONE));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().floor(BigDecimal.TEN));
        Assert.assertEquals("Small Fraction", new BigDecimal("0"), new Rounding().floor(SMALL_FRACTION));
        Assert.assertEquals("Half", new BigDecimal("0"), new Rounding().floor(HALF));
        Assert.assertEquals("Big Fraction", new BigDecimal("0"), new Rounding().floor(BIG_FRACTION));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("-1"), new Rounding().floor(SMALL_FRACTION.negate()));
        Assert.assertEquals("Minus Half", new BigDecimal("-1"), new Rounding().floor(HALF.negate()));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-1"), new Rounding().floor(BIG_FRACTION.negate()));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817"), new Rounding().floor(LARGE));

        Assert.assertEquals("1.1", new BigDecimal("1"), new Rounding().floor(new BigDecimal("1.1")));
        Assert.assertEquals("1.6", new BigDecimal("1"), new Rounding().floor(new BigDecimal("1.6")));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().floor(new BigDecimal("-1")));
        Assert.assertEquals("-1.1", new BigDecimal("-2"), new Rounding().floor(new BigDecimal("-1.1")));
        Assert.assertEquals("-1.6", new BigDecimal("-2"), new Rounding().floor(new BigDecimal("-1.6")));
    }
}