package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Conversions}
 */
public class ConversionsTest {
    private static final BigDecimal PI = BigDecimal.valueOf(Math.PI);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);
    private static final BigDecimal SIX = BigDecimal.valueOf(6);

    private EelContext context;
    private Conversions conversions;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(8)
            .build();

        conversions = new Conversions();
    }

    /**
     * Unit test {@link Conversions#toDegrees(EelContext, BigDecimal)}
     */
    @Test
    public void test_toDegrees() {
        Assert.assertEquals("1/6 pi radians", new BigDecimal("30.000000"), conversions.toDegrees(context, PI.divide(SIX, RoundingMode.HALF_UP)));
        Assert.assertEquals("1/4 pi radians", new BigDecimal("45.000000"), conversions.toDegrees(context, PI.divide(FOUR, RoundingMode.HALF_UP)));
        Assert.assertEquals("1/6 pi radians", new BigDecimal("60.000000"), conversions.toDegrees(context, PI.divide(THREE, RoundingMode.HALF_UP)));
        Assert.assertEquals("1/2 pi radians", new BigDecimal("90.000000"), conversions.toDegrees(context, PI.divide(TWO, RoundingMode.HALF_UP)));
        Assert.assertEquals("pi radians", new BigDecimal("180.00000"), conversions.toDegrees(context, PI));
        Assert.assertEquals("2pi radians", new BigDecimal("360.00000"), conversions.toDegrees(context, PI.multiply(TWO)));
    }

    /**
     * Unit test {@link Conversions#toRadians(EelContext, BigDecimal)}
     */
    @Test
    public void test_toRadians() {
        Assert.assertEquals("30 degrees", new BigDecimal("0.52359878"), conversions.toRadians(context, new BigDecimal("30")));
        Assert.assertEquals("45 degrees", new BigDecimal("0.78539816"), conversions.toRadians(context, new BigDecimal("45")));
        Assert.assertEquals("60 degrees", new BigDecimal("1.0471976"), conversions.toRadians(context, new BigDecimal("60")));
        Assert.assertEquals("90 degrees", new BigDecimal("1.5707963"), conversions.toRadians(context, new BigDecimal("90")));
        Assert.assertEquals("180 degrees", new BigDecimal("3.1415927"), conversions.toRadians(context, new BigDecimal("180")));
        Assert.assertEquals("360 degrees", new BigDecimal("6.2831853"), conversions.toRadians(context, new BigDecimal("360")));
    }
}