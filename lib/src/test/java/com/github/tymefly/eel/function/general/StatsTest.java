package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link Stats}
 */
public class StatsTest {
    private static final BigDecimal NEG_ONE = new BigDecimal(-1);

    private EelContext context;

    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(5)
            .build();
    }

    /**
     * Unit test {@link Stats#avg(EelContext, BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_avg() {
        assertEquals(new BigDecimal("123.46"),
            new Stats().avg(context, new BigDecimal("123.45678")),
            "Single Value");
        assertEquals(new BigDecimal("2"),
            new Stats().avg(context, new BigDecimal("1"), new BigDecimal("3")),
            "Two Values");
        assertEquals(new BigDecimal("1.5"),
            new Stats().avg(context, new BigDecimal("0"), new BigDecimal("1.5"), new BigDecimal("3")),
            "Three Values");
    }

    /**
     * Unit test {@link Stats#max(BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_max() {
        Stats stats = new Stats();

        assertEquals(BigDecimal.TEN, stats.max(BigDecimal.TEN), "Single Number");
        assertEquals(BigDecimal.TEN, stats.max(BigDecimal.ZERO, BigDecimal.TEN), "Two Numbers");
        assertEquals(BigDecimal.ONE, stats.max(BigDecimal.ZERO, BigDecimal.ONE, NEG_ONE), "Three Numbers");
    }

    /**
     * Unit test {@link Stats#min(BigDecimal, BigDecimal...)}
     */
    @Test
    public void test_Min() {
        Stats stats = new Stats();

        assertEquals(BigDecimal.TEN, stats.min(BigDecimal.TEN), "Single Number");
        assertEquals(BigDecimal.ZERO, stats.min(BigDecimal.ZERO, BigDecimal.TEN), "Two Numbers");
        assertEquals(NEG_ONE, stats.min(BigDecimal.ZERO, BigDecimal.ONE, NEG_ONE), "Three Numbers");
    }
}