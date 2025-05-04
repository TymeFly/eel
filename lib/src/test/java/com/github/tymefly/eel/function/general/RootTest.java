package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Root}
 */
public class RootTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Root#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_positive() {
        Assert.assertEquals("Positive",
            new BigDecimal("6.5812"),
            new Root().root(context, new BigDecimal("12345.678"), new BigDecimal("5")));
    }

    /**
     * Unit test {@link Root#root(EelContext, BigDecimal, BigDecimal)}
     */
    @Test
    public void test_root_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Root().root(context, new BigDecimal("-12345.678"), new BigDecimal("5")));
    }
}