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
 * Unit test for {@link Log}
 */
public class LogTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
    }

    /**
     * Unit test {@link Log#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log() {
        Assert.assertEquals("log", new BigDecimal("3.0915"), new Log().log(context, new BigDecimal("1234.5678")));
    }

    /**
     * Unit test {@link Log#log(EelContext, BigDecimal)}
     */
    @Test
    public void test_log_rangeError() {
        Assert.assertThrows(ArithmeticException.class,
            () -> new Log().log(context, new BigDecimal("-1234.5678")));
    }
}