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
        .add(BIG_FRACTION);


    /**
     * Unit test {@link Rounding#round(BigDecimal, int)} 
     */
    @Test
    public void test_round_toInt() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().round(BigDecimal.ZERO, 0));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().round(BigDecimal.ONE, 0));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().round(BigDecimal.TEN, 0));
        Assert.assertEquals("Small Fraction", new BigDecimal("0"), new Rounding().round(SMALL_FRACTION, 0));
        Assert.assertEquals("Half", new BigDecimal("1"), new Rounding().round(HALF, 0));
        Assert.assertEquals("Big Fraction", new BigDecimal("1"), new Rounding().round(BIG_FRACTION, 0));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("0"), new Rounding().round(SMALL_FRACTION.negate(), 0));
        Assert.assertEquals("Minus Half", new BigDecimal("-1"), new Rounding().round(HALF.negate(), 0));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-1"), new Rounding().round(BIG_FRACTION.negate(), 0));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775818"), new Rounding().round(LARGE, 0));

        Assert.assertEquals("1.1", new BigDecimal("1"), new Rounding().round(new BigDecimal("1.1"), 0));
        Assert.assertEquals("1.6", new BigDecimal("2"), new Rounding().round(new BigDecimal("1.6"), 0));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1"), 0));
        Assert.assertEquals("-1.1", new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1.1"), 0));
        Assert.assertEquals("-1.6", new BigDecimal("-2"), new Rounding().round(new BigDecimal("-1.6"), 0));
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_1DecimalPlace() {
        Assert.assertEquals("Zero", new BigDecimal("0.0"), new Rounding().round(BigDecimal.ZERO, 1));
        Assert.assertEquals("One", new BigDecimal("1.0"), new Rounding().round(BigDecimal.ONE, 1));
        Assert.assertEquals("Ten", new BigDecimal("10.0"), new Rounding().round(BigDecimal.TEN, 1));
        Assert.assertEquals("Small Fraction", new BigDecimal("0.5"), new Rounding().round(SMALL_FRACTION, 1));
        Assert.assertEquals("Half", new BigDecimal("0.5"), new Rounding().round(HALF, 1));
        Assert.assertEquals("Big Fraction", new BigDecimal("0.6"), new Rounding().round(BIG_FRACTION, 1));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("-0.5"), new Rounding().round(SMALL_FRACTION.negate(), 1));
        Assert.assertEquals("Minus Half", new BigDecimal("-0.5"), new Rounding().round(HALF.negate(), 1));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-0.6"), new Rounding().round(BIG_FRACTION.negate(), 1));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817.6"), new Rounding().round(LARGE, 1));

        Assert.assertEquals("1.1", new BigDecimal("1.1"), new Rounding().round(new BigDecimal("1.1"), 1));
        Assert.assertEquals("1.6", new BigDecimal("1.6"), new Rounding().round(new BigDecimal("1.6"), 1));
        Assert.assertEquals("-1", new BigDecimal("-1.0"), new Rounding().round(new BigDecimal("-1"), 1));
        Assert.assertEquals("-1.1", new BigDecimal("-1.1"), new Rounding().round(new BigDecimal("-1.1"), 1));
        Assert.assertEquals("-1.6", new BigDecimal("-1.6"), new Rounding().round(new BigDecimal("-1.6"), 1));
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_2DecimalPlaces() {
        Assert.assertEquals("Zero", new BigDecimal("0.00"), new Rounding().round(BigDecimal.ZERO, 2));
        Assert.assertEquals("One", new BigDecimal("1.00"), new Rounding().round(BigDecimal.ONE, 2));
        Assert.assertEquals("Ten", new BigDecimal("10.00"), new Rounding().round(BigDecimal.TEN, 2));
        Assert.assertEquals("Small Fraction", new BigDecimal("0.45"), new Rounding().round(SMALL_FRACTION, 2));
        Assert.assertEquals("Half", new BigDecimal("0.50"), new Rounding().round(HALF, 2));
        Assert.assertEquals("Big Fraction", new BigDecimal("0.55"), new Rounding().round(BIG_FRACTION, 2));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("-0.45"), new Rounding().round(SMALL_FRACTION.negate(), 2));
        Assert.assertEquals("Minus Half", new BigDecimal("-0.50"), new Rounding().round(HALF.negate(), 2));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-0.55"), new Rounding().round(BIG_FRACTION.negate(), 2));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817.55"), new Rounding().round(LARGE, 2));

        Assert.assertEquals("1.1", new BigDecimal("1.10"), new Rounding().round(new BigDecimal("1.1"), 2));
        Assert.assertEquals("1.6", new BigDecimal("1.60"), new Rounding().round(new BigDecimal("1.6"), 2));
        Assert.assertEquals("-1", new BigDecimal("-1.00"), new Rounding().round(new BigDecimal("-1"), 2));
        Assert.assertEquals("-1.1", new BigDecimal("-1.10"), new Rounding().round(new BigDecimal("-1.1"), 2));
        Assert.assertEquals("-1.6", new BigDecimal("-1.60"), new Rounding().round(new BigDecimal("-1.6"), 2));
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_neg1DecimalPlace() {
        Exception actual = Assert.assertThrows(IllegalArgumentException.class,
            () -> new Rounding().round(BigDecimal.ZERO, -1));

        Assert.assertEquals("Unexpected message", "Invalid precision: -1", actual.getMessage());
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_toInt() {
        Assert.assertEquals("Zero", new BigDecimal("0"), new Rounding().truncate(BigDecimal.ZERO, 0));
        Assert.assertEquals("One", new BigDecimal("1"), new Rounding().truncate(BigDecimal.ONE, 0));
        Assert.assertEquals("Ten", new BigDecimal("10"), new Rounding().truncate(BigDecimal.TEN, 0));
        Assert.assertEquals("Small Fraction", new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION, 0));
        Assert.assertEquals("Half", new BigDecimal("0"), new Rounding().truncate(HALF, 0));
        Assert.assertEquals("Big Fraction", new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION, 0));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION.negate(), 0));
        Assert.assertEquals("Minus Half", new BigDecimal("0"), new Rounding().truncate(HALF.negate(), 0));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION.negate(), 0));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817"), new Rounding().truncate(LARGE, 0));

        Assert.assertEquals("1.1", new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.1"), 0));
        Assert.assertEquals("1.6", new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.6"), 0));
        Assert.assertEquals("-1", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1"), 0));
        Assert.assertEquals("-1.1", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.1"), 0));
        Assert.assertEquals("-1.6", new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.6"), 0));
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_1DecimalPlace() {
        Assert.assertEquals("Zero", new BigDecimal("0.0"), new Rounding().truncate(BigDecimal.ZERO, 1));
        Assert.assertEquals("One", new BigDecimal("1.0"), new Rounding().truncate(BigDecimal.ONE, 1));
        Assert.assertEquals("Ten", new BigDecimal("10.0"), new Rounding().truncate(BigDecimal.TEN, 1));
        Assert.assertEquals("Small Fraction", new BigDecimal("0.4"), new Rounding().truncate(SMALL_FRACTION, 1));
        Assert.assertEquals("Half", new BigDecimal("0.5"), new Rounding().truncate(HALF, 1));
        Assert.assertEquals("Big Fraction", new BigDecimal("0.5"), new Rounding().truncate(BIG_FRACTION, 1));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("-0.4"), new Rounding().truncate(SMALL_FRACTION.negate(), 1));
        Assert.assertEquals("Minus Half", new BigDecimal("-0.5"), new Rounding().truncate(HALF.negate(), 1));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-0.5"), new Rounding().truncate(BIG_FRACTION.negate(), 1));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817.5"), new Rounding().truncate(LARGE, 1));

        Assert.assertEquals("1.1", new BigDecimal("1.1"), new Rounding().truncate(new BigDecimal("1.1"), 1));
        Assert.assertEquals("1.6", new BigDecimal("1.6"), new Rounding().truncate(new BigDecimal("1.6"), 1));
        Assert.assertEquals("-1", new BigDecimal("-1.0"), new Rounding().truncate(new BigDecimal("-1"), 1));
        Assert.assertEquals("-1.1", new BigDecimal("-1.1"), new Rounding().truncate(new BigDecimal("-1.1"), 1));
        Assert.assertEquals("-1.6", new BigDecimal("-1.6"), new Rounding().truncate(new BigDecimal("-1.6"), 1));
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_2DecimalPlaces() {
        Assert.assertEquals("Zero", new BigDecimal("0.00"), new Rounding().truncate(BigDecimal.ZERO, 2));
        Assert.assertEquals("One", new BigDecimal("1.00"), new Rounding().truncate(BigDecimal.ONE, 2));
        Assert.assertEquals("Ten", new BigDecimal("10.00"), new Rounding().truncate(BigDecimal.TEN, 2));
        Assert.assertEquals("Small Fraction", new BigDecimal("0.45"), new Rounding().truncate(SMALL_FRACTION, 2));
        Assert.assertEquals("Half", new BigDecimal("0.50"), new Rounding().truncate(HALF, 2));
        Assert.assertEquals("Big Fraction", new BigDecimal("0.55"), new Rounding().truncate(BIG_FRACTION, 2));
        Assert.assertEquals("Minus Small Fraction", new BigDecimal("-0.45"), new Rounding().truncate(SMALL_FRACTION.negate(), 2));
        Assert.assertEquals("Minus Half", new BigDecimal("-0.50"), new Rounding().truncate(HALF.negate(), 2));
        Assert.assertEquals("Minus Big Fraction", new BigDecimal("-0.55"), new Rounding().truncate(BIG_FRACTION.negate(), 2));
        Assert.assertEquals("Large", new BigDecimal("9223372036854775817.55"), new Rounding().truncate(LARGE, 2));

        Assert.assertEquals("1.1", new BigDecimal("1.10"), new Rounding().truncate(new BigDecimal("1.1"), 2));
        Assert.assertEquals("1.6", new BigDecimal("1.60"), new Rounding().truncate(new BigDecimal("1.6"), 2));
        Assert.assertEquals("-1", new BigDecimal("-1.00"), new Rounding().truncate(new BigDecimal("-1"), 2));
        Assert.assertEquals("-1.1", new BigDecimal("-1.10"), new Rounding().truncate(new BigDecimal("-1.1"), 2));
        Assert.assertEquals("-1.6", new BigDecimal("-1.60"), new Rounding().truncate(new BigDecimal("-1.6"), 2));
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_neg1DecimalPlace() {
        Exception actual = Assert.assertThrows(IllegalArgumentException.class,
            () -> new Rounding().truncate(BigDecimal.ZERO, -1));

        Assert.assertEquals("Unexpected message", "Invalid precision: -1", actual.getMessage());
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