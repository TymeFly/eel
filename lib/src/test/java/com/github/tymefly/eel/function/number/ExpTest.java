package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Exp}
 */
public class ExpTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Exp#exp(EelContext, BigDecimal)}
     */
    @Test
    public void test_Exp() {
        Assert.assertEquals("exp", new BigDecimal("3.4369"), new Exp().exp(context, new BigDecimal("1.2345678")));
    }
}