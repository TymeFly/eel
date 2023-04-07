package com.github.tymefly.eel.function.math;

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
 * Unit test for {@link Ln}
 */
public class LnTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
    }

    /**
     * Unit test {@link Ln#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln() {
        Assert.assertEquals("ln", new BigDecimal("7.1185"), new Ln().ln(context, new BigDecimal("1234.5678")));
    }

    /**
     * Unit test {@link Ln#ln(EelContext, BigDecimal)}
     */
    @Test
    public void test_ln_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Ln().ln(context, new BigDecimal("-1234.5678")));
    }
}