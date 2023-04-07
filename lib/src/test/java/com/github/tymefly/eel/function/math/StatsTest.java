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
 * Unit test for {@link Stats}
 */
public class StatsTest {
    private static final BigDecimal NEG_ONE = new BigDecimal(-1);

    private EelContext context;

    @Before
    public void setUp() {
        context = mock(EelContext.class);

        when(context.getMathContext())
            .thenAnswer( i -> new MathContext(5, RoundingMode.HALF_UP));
    }

    /**
     * Unit test {@link Stats#avg(EelContext, BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_avg() {
        Assert.assertEquals("Single Value",
            new BigDecimal("123.46"),
            new Stats().avg(context, new BigDecimal("123.45678")));
        Assert.assertEquals("Two Values",
            new BigDecimal("2"),
            new Stats().avg(context, new BigDecimal("1"), new BigDecimal("3")));
        Assert.assertEquals("Three Values",
            new BigDecimal("1.5"),
            new Stats().avg(context, new BigDecimal("0"), new BigDecimal("1.5"), new BigDecimal("3")));
    }

    /**
     * Unit test {@link Stats#max(BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_max() {
        Stats stats = new Stats();

        Assert.assertEquals("Single Number", BigDecimal.TEN, stats.max(BigDecimal.TEN));
        Assert.assertEquals("Two Numbers", BigDecimal.TEN, stats.max(BigDecimal.ZERO, BigDecimal.TEN));
        Assert.assertEquals("Three Numbers", BigDecimal.ONE, stats.max(BigDecimal.ZERO, BigDecimal.ONE, NEG_ONE));
    }

    /**
     * Unit test {@link Stats#min(BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_Min() {
        Stats stats = new Stats();

        Assert.assertEquals("Single Number", BigDecimal.TEN, stats.min(BigDecimal.TEN));
        Assert.assertEquals("Two Numbers", BigDecimal.ZERO, stats.min(BigDecimal.ZERO, BigDecimal.TEN));
        Assert.assertEquals("Three Numbers", NEG_ONE, stats.min(BigDecimal.ZERO, BigDecimal.ONE, NEG_ONE));
    }
}