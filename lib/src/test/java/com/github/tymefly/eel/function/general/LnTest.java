package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Ln}
 */
public class LnTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
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