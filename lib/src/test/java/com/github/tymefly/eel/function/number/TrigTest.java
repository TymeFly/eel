package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Trig}
 */
public class TrigTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Trig#sin(EelContext, BigDecimal)}
     */
    @Test
    public void test_Sin() {
        Assert.assertEquals("sin", new BigDecimal("-0.21891"), new Trig().sin(context, new BigDecimal("12.345678")));
    }


    /**
     * Unit test {@link Trig#cos(EelContext, BigDecimal)}
     */
    @Test
    public void test_cos() {
        Assert.assertEquals("cos", new BigDecimal("0.32993"), new Trig().cos(context, new BigDecimal("1.2345678")));
    }


    /**
     * Unit test {@link Trig#tan(EelContext, BigDecimal)}
     */
    @Test
    public void test_tan() {
        Assert.assertEquals("tan", new BigDecimal("-0.22435"), new Trig().tan(context, new BigDecimal("12.345678")));
    }


    /**
     * Unit test {@link Trig#asin(EelContext, BigDecimal)}
     */
    @Test
    public void test_asin() {
        Assert.assertEquals("asin", new BigDecimal("0.12377"), new Trig().asin(context, new BigDecimal("0.12345678")));
    }

    /**
     * Unit test {@link Trig#asin(EelContext, BigDecimal)}
     */
    @Test
    public void test_asin_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Trig().asin(context, new BigDecimal("12345.678")));
    }


    /**
     * Unit test {@link Trig#acos(EelContext, BigDecimal)}
     */
    @Test
    public void test_acos() {
        Assert.assertEquals("acos", new BigDecimal("1.4470"), new Trig().acos(context, new BigDecimal("0.12345678")));
    }

    /**
     * Unit test {@link Trig#acos(EelContext, BigDecimal)}
     */
    @Test
    public void test_acos_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Trig().acos(context, new BigDecimal("12345.678")));
    }


    /**
     * Unit test {@link Trig#atan(EelContext, BigDecimal)}
     */
    @Test
    public void test_Atan() {
        Assert.assertEquals("atan", new BigDecimal("1.5627"), new Trig().atan(context, new BigDecimal("123.45678")));
    }
}