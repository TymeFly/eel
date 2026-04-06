package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(new BigDecimal("0"), new Rounding().round(BigDecimal.ZERO, 0), "Zero");
        assertEquals(new BigDecimal("1"), new Rounding().round(BigDecimal.ONE, 0), "One");
        assertEquals(new BigDecimal("10"), new Rounding().round(BigDecimal.TEN, 0), "Ten");
        assertEquals(new BigDecimal("0"), new Rounding().round(SMALL_FRACTION, 0), "Small Fraction");
        assertEquals(new BigDecimal("1"), new Rounding().round(HALF, 0), "Half");
        assertEquals(new BigDecimal("1"), new Rounding().round(BIG_FRACTION, 0), "Big Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().round(SMALL_FRACTION.negate(), 0), "Minus Small Fraction");
        assertEquals(new BigDecimal("-1"), new Rounding().round(HALF.negate(), 0), "Minus Half");
        assertEquals(new BigDecimal("-1"), new Rounding().round(BIG_FRACTION.negate(), 0), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775818"), new Rounding().round(LARGE, 0), "Large");

        assertEquals(new BigDecimal("1"), new Rounding().round(new BigDecimal("1.1"), 0), "1.1");
        assertEquals(new BigDecimal("2"), new Rounding().round(new BigDecimal("1.6"), 0), "1.6");
        assertEquals(new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1"), 0), "-1");
        assertEquals(new BigDecimal("-1"), new Rounding().round(new BigDecimal("-1.1"), 0), "-1.1");
        assertEquals(new BigDecimal("-2"), new Rounding().round(new BigDecimal("-1.6"), 0), "-1.6");
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_1DecimalPlace() {
        assertEquals(new BigDecimal("0.0"), new Rounding().round(BigDecimal.ZERO, 1), "Zero");
        assertEquals(new BigDecimal("1.0"), new Rounding().round(BigDecimal.ONE, 1), "One");
        assertEquals(new BigDecimal("10.0"), new Rounding().round(BigDecimal.TEN, 1), "Ten");
        assertEquals(new BigDecimal("0.5"), new Rounding().round(SMALL_FRACTION, 1), "Small Fraction");
        assertEquals(new BigDecimal("0.5"), new Rounding().round(HALF, 1), "Half");
        assertEquals(new BigDecimal("0.6"), new Rounding().round(BIG_FRACTION, 1), "Big Fraction");
        assertEquals(new BigDecimal("-0.5"), new Rounding().round(SMALL_FRACTION.negate(), 1), "Minus Small Fraction");
        assertEquals(new BigDecimal("-0.5"), new Rounding().round(HALF.negate(), 1), "Minus Half");
        assertEquals(new BigDecimal("-0.6"), new Rounding().round(BIG_FRACTION.negate(), 1), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817.6"), new Rounding().round(LARGE, 1), "Large");

        assertEquals(new BigDecimal("1.1"), new Rounding().round(new BigDecimal("1.1"), 1), "1.1");
        assertEquals(new BigDecimal("1.6"), new Rounding().round(new BigDecimal("1.6"), 1), "1.6");
        assertEquals(new BigDecimal("-1.0"), new Rounding().round(new BigDecimal("-1"), 1), "-1");
        assertEquals(new BigDecimal("-1.1"), new Rounding().round(new BigDecimal("-1.1"), 1), "-1.1");
        assertEquals(new BigDecimal("-1.6"), new Rounding().round(new BigDecimal("-1.6"), 1), "-1.6");
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_2DecimalPlaces() {
        assertEquals(new BigDecimal("0.00"), new Rounding().round(BigDecimal.ZERO, 2), "Zero");
        assertEquals(new BigDecimal("1.00"), new Rounding().round(BigDecimal.ONE, 2), "One");
        assertEquals(new BigDecimal("10.00"), new Rounding().round(BigDecimal.TEN, 2), "Ten");
        assertEquals(new BigDecimal("0.45"), new Rounding().round(SMALL_FRACTION, 2), "Small Fraction");
        assertEquals(new BigDecimal("0.50"), new Rounding().round(HALF, 2), "Half");
        assertEquals(new BigDecimal("0.55"), new Rounding().round(BIG_FRACTION, 2), "Big Fraction");
        assertEquals(new BigDecimal("-0.45"), new Rounding().round(SMALL_FRACTION.negate(), 2), "Minus Small Fraction");
        assertEquals(new BigDecimal("-0.50"), new Rounding().round(HALF.negate(), 2), "Minus Half");
        assertEquals(new BigDecimal("-0.55"), new Rounding().round(BIG_FRACTION.negate(), 2), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817.55"), new Rounding().round(LARGE, 2), "Large");

        assertEquals(new BigDecimal("1.10"), new Rounding().round(new BigDecimal("1.1"), 2), "1.1");
        assertEquals(new BigDecimal("1.60"), new Rounding().round(new BigDecimal("1.6"), 2), "1.6");
        assertEquals(new BigDecimal("-1.00"), new Rounding().round(new BigDecimal("-1"), 2), "-1");
        assertEquals(new BigDecimal("-1.10"), new Rounding().round(new BigDecimal("-1.1"), 2), "-1.1");
        assertEquals(new BigDecimal("-1.60"), new Rounding().round(new BigDecimal("-1.6"), 2), "-1.6");
    }

    /**
     * Unit test {@link Rounding#round(BigDecimal, int)}
     */
    @Test
    public void test_round_neg1DecimalPlace() {
        Exception actual = assertThrows(IllegalArgumentException.class,
            () -> new Rounding().round(BigDecimal.ZERO, -1));

        assertEquals("Invalid precision: -1", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_toInt() {
        assertEquals(new BigDecimal("0"), new Rounding().truncate(BigDecimal.ZERO, 0), "Zero");
        assertEquals(new BigDecimal("1"), new Rounding().truncate(BigDecimal.ONE, 0), "One");
        assertEquals(new BigDecimal("10"), new Rounding().truncate(BigDecimal.TEN, 0), "Ten");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION, 0), "Small Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(HALF, 0), "Half");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION, 0), "Big Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(SMALL_FRACTION.negate(), 0), "Minus Small Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(HALF.negate(), 0), "Minus Half");
        assertEquals(new BigDecimal("0"), new Rounding().truncate(BIG_FRACTION.negate(), 0), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817"), new Rounding().truncate(LARGE, 0), "Large");

        assertEquals(new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.1"), 0), "1.1");
        assertEquals(new BigDecimal("1"), new Rounding().truncate(new BigDecimal("1.6"), 0), "1.6");
        assertEquals(new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1"), 0), "-1");
        assertEquals(new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.1"), 0), "-1.1");
        assertEquals(new BigDecimal("-1"), new Rounding().truncate(new BigDecimal("-1.6"), 0), "-1.6");
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_1DecimalPlace() {
        assertEquals(new BigDecimal("0.0"), new Rounding().truncate(BigDecimal.ZERO, 1), "Zero");
        assertEquals(new BigDecimal("1.0"), new Rounding().truncate(BigDecimal.ONE, 1), "One");
        assertEquals(new BigDecimal("10.0"), new Rounding().truncate(BigDecimal.TEN, 1), "Ten");
        assertEquals(new BigDecimal("0.4"), new Rounding().truncate(SMALL_FRACTION, 1), "Small Fraction");
        assertEquals(new BigDecimal("0.5"), new Rounding().truncate(HALF, 1), "Half");
        assertEquals(new BigDecimal("0.5"), new Rounding().truncate(BIG_FRACTION, 1), "Big Fraction");
        assertEquals(new BigDecimal("-0.4"), new Rounding().truncate(SMALL_FRACTION.negate(), 1), "Minus Small Fraction");
        assertEquals(new BigDecimal("-0.5"), new Rounding().truncate(HALF.negate(), 1), "Minus Half");
        assertEquals(new BigDecimal("-0.5"), new Rounding().truncate(BIG_FRACTION.negate(), 1), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817.5"), new Rounding().truncate(LARGE, 1), "Large");

        assertEquals(new BigDecimal("1.1"), new Rounding().truncate(new BigDecimal("1.1"), 1), "1.1");
        assertEquals(new BigDecimal("1.6"), new Rounding().truncate(new BigDecimal("1.6"), 1), "1.6");
        assertEquals(new BigDecimal("-1.0"), new Rounding().truncate(new BigDecimal("-1"), 1), "-1");
        assertEquals(new BigDecimal("-1.1"), new Rounding().truncate(new BigDecimal("-1.1"), 1), "-1.1");
        assertEquals(new BigDecimal("-1.6"), new Rounding().truncate(new BigDecimal("-1.6"), 1), "-1.6");
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_2DecimalPlaces() {
        assertEquals(new BigDecimal("0.00"), new Rounding().truncate(BigDecimal.ZERO, 2), "Zero");
        assertEquals(new BigDecimal("1.00"), new Rounding().truncate(BigDecimal.ONE, 2), "One");
        assertEquals(new BigDecimal("10.00"), new Rounding().truncate(BigDecimal.TEN, 2), "Ten");
        assertEquals(new BigDecimal("0.45"), new Rounding().truncate(SMALL_FRACTION, 2), "Small Fraction");
        assertEquals(new BigDecimal("0.50"), new Rounding().truncate(HALF, 2), "Half");
        assertEquals(new BigDecimal("0.55"), new Rounding().truncate(BIG_FRACTION, 2), "Big Fraction");
        assertEquals(new BigDecimal("-0.45"), new Rounding().truncate(SMALL_FRACTION.negate(), 2), "Minus Small Fraction");
        assertEquals(new BigDecimal("-0.50"), new Rounding().truncate(HALF.negate(), 2), "Minus Half");
        assertEquals(new BigDecimal("-0.55"), new Rounding().truncate(BIG_FRACTION.negate(), 2), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817.55"), new Rounding().truncate(LARGE, 2), "Large");

        assertEquals(new BigDecimal("1.10"), new Rounding().truncate(new BigDecimal("1.1"), 2), "1.1");
        assertEquals(new BigDecimal("1.60"), new Rounding().truncate(new BigDecimal("1.6"), 2), "1.6");
        assertEquals(new BigDecimal("-1.00"), new Rounding().truncate(new BigDecimal("-1"), 2), "-1");
        assertEquals(new BigDecimal("-1.10"), new Rounding().truncate(new BigDecimal("-1.1"), 2), "-1.1");
        assertEquals(new BigDecimal("-1.60"), new Rounding().truncate(new BigDecimal("-1.6"), 2), "-1.6");
    }

    /**
     * Unit test {@link Rounding#truncate(BigDecimal, int)}
     */
    @Test
    public void test_truncate_neg1DecimalPlace() {
        Exception actual = assertThrows(IllegalArgumentException.class,
            () -> new Rounding().truncate(BigDecimal.ZERO, -1));

        assertEquals("Invalid precision: -1", actual.getMessage(), "Unexpected message");
    }


    /**
     * Unit test {@link Rounding#ceil(BigDecimal)}
     */
    @Test
    public void test_ceil() {
        assertEquals(new BigDecimal("0"), new Rounding().ceil(BigDecimal.ZERO), "Zero");
        assertEquals(new BigDecimal("1"), new Rounding().ceil(BigDecimal.ONE), "One");
        assertEquals(new BigDecimal("10"), new Rounding().ceil(BigDecimal.TEN), "Ten");
        assertEquals(new BigDecimal("1"), new Rounding().ceil(SMALL_FRACTION), "Small Fraction");
        assertEquals(new BigDecimal("1"), new Rounding().ceil(HALF), "Half");
        assertEquals(new BigDecimal("1"), new Rounding().ceil(BIG_FRACTION), "Big Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().ceil(SMALL_FRACTION.negate()), "Minus Small Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().ceil(HALF.negate()), "Minus Half");
        assertEquals(new BigDecimal("0"), new Rounding().ceil(BIG_FRACTION.negate()), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775818"), new Rounding().ceil(LARGE), "Large");

        assertEquals(new BigDecimal("2"), new Rounding().ceil(new BigDecimal("1.1")), "1.1");
        assertEquals(new BigDecimal("2"), new Rounding().ceil(new BigDecimal("1.6")), "1.6");
        assertEquals(new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1")), "-1");
        assertEquals(new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1.1")), "-1.1");
        assertEquals(new BigDecimal("-1"), new Rounding().ceil(new BigDecimal("-1.6")), "-1.6");
    }



    /**
     * Unit test {@link Rounding#floor(BigDecimal)}
     */
    @Test
    public void test_floor() {
        assertEquals(new BigDecimal("0"), new Rounding().floor(BigDecimal.ZERO), "Zero");
        assertEquals(new BigDecimal("1"), new Rounding().floor(BigDecimal.ONE), "One");
        assertEquals(new BigDecimal("10"), new Rounding().floor(BigDecimal.TEN), "Ten");
        assertEquals(new BigDecimal("0"), new Rounding().floor(SMALL_FRACTION), "Small Fraction");
        assertEquals(new BigDecimal("0"), new Rounding().floor(HALF), "Half");
        assertEquals(new BigDecimal("0"), new Rounding().floor(BIG_FRACTION), "Big Fraction");
        assertEquals(new BigDecimal("-1"), new Rounding().floor(SMALL_FRACTION.negate()), "Minus Small Fraction");
        assertEquals(new BigDecimal("-1"), new Rounding().floor(HALF.negate()), "Minus Half");
        assertEquals(new BigDecimal("-1"), new Rounding().floor(BIG_FRACTION.negate()), "Minus Big Fraction");
        assertEquals(new BigDecimal("9223372036854775817"), new Rounding().floor(LARGE), "Large");

        assertEquals(new BigDecimal("1"), new Rounding().floor(new BigDecimal("1.1")), "1.1");
        assertEquals(new BigDecimal("1"), new Rounding().floor(new BigDecimal("1.6")), "1.6");
        assertEquals(new BigDecimal("-1"), new Rounding().floor(new BigDecimal("-1")), "-1");
        assertEquals(new BigDecimal("-2"), new Rounding().floor(new BigDecimal("-1.1")), "-1.1");
        assertEquals(new BigDecimal("-2"), new Rounding().floor(new BigDecimal("-1.6")), "-1.6");
    }
}