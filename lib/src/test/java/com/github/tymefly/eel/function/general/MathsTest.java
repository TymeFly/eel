package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Maths}
 */
public class MathsTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Maths#abs(EelContext, BigDecimal)}
     */
    @Test
    public void test_abs() {
        Assert.assertEquals("Positive", new BigDecimal("123.46"), new Maths().abs(context, new BigDecimal("123.45678")));
        Assert.assertEquals("Negative", new BigDecimal("123.46"), new Maths().abs(context, new BigDecimal("-123.45678")));
    }

    /**
     * Unit test {@link Maths#sgn}
     */
    @Test
    public void test_sgn() {
        Assert.assertEquals("negative value", -1, new Maths().sgn(new BigDecimal("-0.1")));
        Assert.assertEquals("zero", 0, new Maths().sgn(BigDecimal.ZERO));
        Assert.assertEquals("positive value", 1, new Maths().sgn(new BigDecimal("0.1")));
    }

    /**
     * Unit test {@link Maths#exp(EelContext, BigDecimal)}
     */
    @Test
    public void test_Exp() {
        Assert.assertEquals("exp", new BigDecimal("3.4369"), new Maths().exp(context, new BigDecimal("1.2345678")));
    }


    /**
     * Unit test {@link Maths#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial() {
        Assert.assertEquals("integer", new BigDecimal("40320"), new Maths().factorial(context, new BigDecimal("8")));
        Assert.assertEquals("fractional", new BigDecimal("52.343"), new Maths().factorial(context, new BigDecimal("4.5")));
    }

    /**
     * Unit test {@link Maths#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Maths().factorial(context, new BigDecimal("-8")));
    }


    /**
     * Unit test {@link Maths#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln() {
        Assert.assertEquals("ln", new BigDecimal("7.1185"), new Maths().ln(context, new BigDecimal("1234.5678")));
    }

    /**
     * Unit test {@link Maths#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Maths().ln(context, new BigDecimal("-1234.5678")));
    }



    /**
     * Unit test {@link Maths#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log() {
        Assert.assertEquals("log", new BigDecimal("3.0915"), new Maths().log(context, new BigDecimal("1234.5678")));
    }

    /**
     * Unit test {@link Maths#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Maths().log(context, new BigDecimal("-1234.5678")));
    }


    /**
     * Unit test {@link Maths#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_positive() {
        Assert.assertEquals("Positive",
            new BigDecimal("6.5812"),
            new Maths().root(context, new BigDecimal("12345.678"), new BigDecimal("5")));
    }

    /**
     * Unit test {@link Maths#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Maths().root(context, new BigDecimal("-12345.678"), new BigDecimal("5")));
    }
}