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
 * Unit test for {@link Root}
 */
public class RootTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
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