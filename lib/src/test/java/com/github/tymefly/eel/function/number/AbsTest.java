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
 * Unit test for {@link Abs}
 */
public class AbsTest {
    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
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