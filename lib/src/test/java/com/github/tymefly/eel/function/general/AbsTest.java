package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Abs}
 */
public class AbsTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Abs#abs(EelContext, BigDecimal)}
     */
    @Test
    public void test_abs() {
        Assert.assertEquals("Positive", new BigDecimal("123.46"), new Abs().abs(context, new BigDecimal("123.45678")));
        Assert.assertEquals("Negative", new BigDecimal("123.46"), new Abs().abs(context, new BigDecimal("-123.45678")));
    }
}