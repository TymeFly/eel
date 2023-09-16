package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Factorial}
 */
public class FactorialTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
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