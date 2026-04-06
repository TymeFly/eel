package com.github.tymefly.eel.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link BigDecimals}
 */
public class BigDecimalsTest {
    private final BigDecimal ZERO = new BigDecimal("0");
    private final BigDecimal ZERO_1 = new BigDecimal("0.0");
    private final BigDecimal ZERO_2 = new BigDecimal("0.00");
    private final BigDecimal ONE = new BigDecimal("1");
    private final BigDecimal NEG_ONE = new BigDecimal("-1");

    /**
     * Unit test {@link BigDecimals#eq(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_eq() {
        assertTrue(BigDecimals.eq(ZERO, new BigDecimal("0")), "Zero and 0");
        assertTrue(BigDecimals.eq(ZERO, ZERO), "Zero and Zero");
        assertTrue(BigDecimals.eq(ZERO, ZERO_1), "Zero and Zero_1");
        assertTrue(BigDecimals.eq(ZERO, ZERO_2), "Zero and Zero_2");

        assertFalse(BigDecimals.eq(ZERO, ONE), "Zero and ONE");
        assertFalse(BigDecimals.eq(ZERO, NEG_ONE), "Zero and NEG_ONE");
    }

    /**
     * Unit test {@link BigDecimals#gt(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_gt() {
        assertFalse(BigDecimals.gt(ZERO, new BigDecimal("0")), "Zero and 0");
        assertFalse(BigDecimals.gt(ZERO, ZERO), "Zero and Zero");
        assertFalse(BigDecimals.gt(ZERO, ZERO_1), "Zero and Zero_1");
        assertFalse(BigDecimals.gt(ZERO, ZERO_2), "Zero and Zero_2");

        assertFalse(BigDecimals.gt(ZERO, ONE), "Zero and ONE");
        assertTrue(BigDecimals.gt(ZERO, NEG_ONE), "Zero and NEG_ONE");
    }

    /**
     * Unit test {@link BigDecimals#ge(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_ge() {
        assertTrue(BigDecimals.ge(ZERO, new BigDecimal("0")), "Zero and 0");
        assertTrue(BigDecimals.ge(ZERO, ZERO), "Zero and Zero");
        assertTrue(BigDecimals.ge(ZERO, ZERO_1), "Zero and Zero_1");
        assertTrue(BigDecimals.ge(ZERO, ZERO_2), "Zero and Zero_2");

        assertFalse(BigDecimals.ge(ZERO, ONE), "Zero and ONE");
        assertTrue(BigDecimals.ge(ZERO, NEG_ONE), "Zero and NEG_ONE");
    }

    /**
     * Unit test {@link BigDecimals#lt(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_lt() {
        assertFalse(BigDecimals.lt(ZERO, new BigDecimal("0")), "Zero and 0");
        assertFalse(BigDecimals.lt(ZERO, ZERO), "Zero and Zero");
        assertFalse(BigDecimals.lt(ZERO, ZERO_1), "Zero and Zero_1");
        assertFalse(BigDecimals.lt(ZERO, ZERO_2), "Zero and Zero_2");

        assertTrue(BigDecimals.lt(ZERO, ONE), "Zero and ONE");
        assertFalse(BigDecimals.lt(ZERO, NEG_ONE), "Zero and NEG_ONE");
    }

    /**
     * Unit test {@link BigDecimals#le(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_le() {
        assertTrue(BigDecimals.le(ZERO, new BigDecimal("0")), "Zero and 0");
        assertTrue(BigDecimals.le(ZERO, ZERO), "Zero and Zero");
        assertTrue(BigDecimals.le(ZERO, ZERO_1), "Zero and Zero_1");
        assertTrue(BigDecimals.le(ZERO, ZERO_2), "Zero and Zero_2");

        assertTrue(BigDecimals.le(ZERO, ONE), "Zero and ONE");
        assertFalse(BigDecimals.le(ZERO, NEG_ONE), "Zero and NEG_ONE");
    }

    /**
     * Unit test {@link BigDecimals#toBigDecimal(Number)}
     */
    @Test
    public void test_toBigDecimal() {
        assertEquals(BigDecimal.ZERO, BigDecimals.toBigDecimal(new BigDecimal("0")), "Zero");
        assertEquals(BigDecimal.TEN, BigDecimals.toBigDecimal(new BigDecimal("10")), "Ten");
        assertEquals(new BigDecimal("1"), BigDecimals.toBigDecimal((byte) 1), "byte");
        assertEquals(new BigDecimal("2"), BigDecimals.toBigDecimal((short) 2), "short");
        assertEquals(new BigDecimal("3"), BigDecimals.toBigDecimal(3), "int");
        assertEquals(new BigDecimal("4"), BigDecimals.toBigDecimal(4), "long");
        assertEquals(new BigDecimal("5.6"), BigDecimals.toBigDecimal(5.6f), "float");
        assertEquals(new BigDecimal("7.8"), BigDecimals.toBigDecimal(7.8), "double");
        assertEquals(new BigDecimal("99"), BigDecimals.toBigDecimal(new BigInteger("99")), "decimal");

        assertEquals(new BigDecimal("1234"), BigDecimals.toBigDecimal(new AtomicLong(1234)), "other Number");
    }

    /**
     * Unit test {@link BigDecimals#isIntegral(BigDecimal)}
     */
    @Test
    void isIntegral() {
        assertTrue(BigDecimals.isIntegral(new BigDecimal("0")), "0");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("0.0")), "0.0");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("0.00")), "0.00");

        assertTrue(BigDecimals.isIntegral(new BigDecimal("1")), "1");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("10")), "10");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("100")), "100");

        assertTrue(BigDecimals.isIntegral(new BigDecimal("-1")), "-1");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("-10")), "-10");
        assertTrue(BigDecimals.isIntegral(new BigDecimal("-100")), "-100");

        assertFalse(BigDecimals.isIntegral(new BigDecimal("0.1")), "0.1");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("0.01")), "0.01");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("0.001")), "0.001");

        assertFalse(BigDecimals.isIntegral(new BigDecimal("1.1")), "1.1");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("1.01")), "1.01");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("1.001")), "1.001");

        assertFalse(BigDecimals.isIntegral(new BigDecimal("-0.1")), "-0.1");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-0.01")), "-0.01");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-0.001")), "-0.001");

        assertFalse(BigDecimals.isIntegral(new BigDecimal("-1.1")), "-0.1");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-1.01")), "-0.01");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-1.001")), "-0.001");

        assertFalse(BigDecimals.isIntegral(new BigDecimal("1e-8")), "small positive");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-1e-8")), "small negative");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("1e8").add(new BigDecimal("1e-8"))), "large positive");
        assertFalse(BigDecimals.isIntegral(new BigDecimal("-1e8").add(new BigDecimal("1e-8"))), "large negative");
    }
}