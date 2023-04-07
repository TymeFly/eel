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
 * Unit test for {@link Exp}
 */
public class ExpTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
    }

    /**
     * Unit test {@link Exp#exp(EelContext, BigDecimal)}
     */
    @Test
    public void test_Exp() {
        Assert.assertEquals("exp", new BigDecimal("3.4369"), new Exp().exp(context, new BigDecimal("1.2345678")));
    }
}