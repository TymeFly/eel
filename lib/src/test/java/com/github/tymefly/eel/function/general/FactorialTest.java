package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Factorial}
 */
public class FactorialTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Factorial#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial() {
        Assert.assertEquals("integer", new BigDecimal("40320"), new Factorial().factorial(context, new BigDecimal("8")));
        Assert.assertEquals("fractional", new BigDecimal("52.343"), new Factorial().factorial(context, new BigDecimal("4.5")));
    }

    /**
     * Unit test {@link Factorial#factorial(EelContext, BigDecimal)}
     */
    @Test
    public void test_factorial_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Factorial().factorial(context, new BigDecimal("-8")));
    }
}