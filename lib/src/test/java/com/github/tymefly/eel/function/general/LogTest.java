package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Log}
 */
public class LogTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
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