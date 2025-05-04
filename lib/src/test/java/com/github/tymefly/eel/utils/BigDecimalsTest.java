package com.github.tymefly.eel.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue("Zero and 0", BigDecimals.eq(ZERO, new BigDecimal("0")));
        Assert.assertTrue("Zero and Zero", BigDecimals.eq(ZERO, ZERO));
        Assert.assertTrue("Zero and Zero_1", BigDecimals.eq(ZERO, ZERO_1));
        Assert.assertTrue("Zero and Zero_2", BigDecimals.eq(ZERO, ZERO_2));

        Assert.assertFalse("Zero and ONE", BigDecimals.eq(ZERO, ONE));
        Assert.assertFalse("Zero and NEG_ONE", BigDecimals.eq(ZERO, NEG_ONE));
    }

    /**
     * Unit test {@link BigDecimals#gt(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_gt() {
        Assert.assertFalse("Zero and 0", BigDecimals.gt(ZERO, new BigDecimal("0")));
        Assert.assertFalse("Zero and Zero", BigDecimals.gt(ZERO, ZERO));
        Assert.assertFalse("Zero and Zero_1", BigDecimals.gt(ZERO, ZERO_1));
        Assert.assertFalse("Zero and Zero_2", BigDecimals.gt(ZERO, ZERO_2));

        Assert.assertFalse("Zero and ONE", BigDecimals.gt(ZERO, ONE));
        Assert.assertTrue("Zero and NEG_ONE", BigDecimals.gt(ZERO, NEG_ONE));
    }

    /**
     * Unit test {@link BigDecimals#ge(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_ge() {
        Assert.assertTrue("Zero and 0", BigDecimals.ge(ZERO, new BigDecimal("0")));
        Assert.assertTrue("Zero and Zero", BigDecimals.ge(ZERO, ZERO));
        Assert.assertTrue("Zero and Zero_1", BigDecimals.ge(ZERO, ZERO_1));
        Assert.assertTrue("Zero and Zero_2", BigDecimals.ge(ZERO, ZERO_2));

        Assert.assertFalse("Zero and ONE", BigDecimals.ge(ZERO, ONE));
        Assert.assertTrue("Zero and NEG_ONE", BigDecimals.ge(ZERO, NEG_ONE));
    }

    /**
     * Unit test {@link BigDecimals#lt(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_lt() {
        Assert.assertFalse("Zero and 0", BigDecimals.lt(ZERO, new BigDecimal("0")));
        Assert.assertFalse("Zero and Zero", BigDecimals.lt(ZERO, ZERO));
        Assert.assertFalse("Zero and Zero_1", BigDecimals.lt(ZERO, ZERO_1));
        Assert.assertFalse("Zero and Zero_2", BigDecimals.lt(ZERO, ZERO_2));

        Assert.assertTrue("Zero and ONE", BigDecimals.lt(ZERO, ONE));
        Assert.assertFalse("Zero and NEG_ONE", BigDecimals.lt(ZERO, NEG_ONE));
    }

    /**
     * Unit test {@link BigDecimals#le(BigDecimal, BigDecimal)}
     */
    @Test
    public void test_le() {
        Assert.assertTrue("Zero and 0", BigDecimals.le(ZERO, new BigDecimal("0")));
        Assert.assertTrue("Zero and Zero", BigDecimals.le(ZERO, ZERO));
        Assert.assertTrue("Zero and Zero_1", BigDecimals.le(ZERO, ZERO_1));
        Assert.assertTrue("Zero and Zero_2", BigDecimals.le(ZERO, ZERO_2));

        Assert.assertTrue("Zero and ONE", BigDecimals.le(ZERO, ONE));
        Assert.assertFalse("Zero and NEG_ONE", BigDecimals.le(ZERO, NEG_ONE));
    }

    /**
     * Unit test {@link BigDecimals#toBigDecimal(Number)}
     */
    @Test
    public void test_toBigDecimal() {
        Assert.assertEquals("Zero", BigDecimal.ZERO, BigDecimals.toBigDecimal(new BigDecimal("0")));
        Assert.assertEquals("Ten", BigDecimal.TEN, BigDecimals.toBigDecimal(new BigDecimal("10")));
        Assert.assertEquals("byte", new BigDecimal("1"), BigDecimals.toBigDecimal((byte) 1));
        Assert.assertEquals("short", new BigDecimal("2"), BigDecimals.toBigDecimal((short) 2));
        Assert.assertEquals("int", new BigDecimal("3"), BigDecimals.toBigDecimal(3));
        Assert.assertEquals("long", new BigDecimal("4"), BigDecimals.toBigDecimal(4));
        Assert.assertEquals("float", new BigDecimal("5.6"), BigDecimals.toBigDecimal(5.6f));
        Assert.assertEquals("double", new BigDecimal("7.8"), BigDecimals.toBigDecimal(7.8));
        Assert.assertEquals("decimal", new BigDecimal("99"), BigDecimals.toBigDecimal(new BigInteger("99")));

        Assert.assertEquals("other Number", new BigDecimal("1234"), BigDecimals.toBigDecimal(new AtomicLong(1234)));
    }
}