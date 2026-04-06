package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link NumberConversions}
 */
public class NumberConversionsTest {
    private static final BigDecimal PI = BigDecimal.valueOf(Math.PI);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);
    private static final BigDecimal SIX = BigDecimal.valueOf(6);

    private EelContext context;
    private NumberConversions numberConversions;


    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withPrecision(8)
            .build();

        numberConversions = new NumberConversions();
    }

    /**
     * Unit test {@link NumberConversions#toDegrees(EelContext, BigDecimal)}
     */
    @Test
    public void test_toDegrees() {
        assertEquals(new BigDecimal("30.000000"), numberConversions.toDegrees(context, PI.divide(SIX, RoundingMode.HALF_UP)), "1/6 pi radians");
        assertEquals(new BigDecimal("45.000000"), numberConversions.toDegrees(context, PI.divide(FOUR, RoundingMode.HALF_UP)), "1/4 pi radians");
        assertEquals(new BigDecimal("60.000000"), numberConversions.toDegrees(context, PI.divide(THREE, RoundingMode.HALF_UP)), "1/6 pi radians");
        assertEquals(new BigDecimal("90.000000"), numberConversions.toDegrees(context, PI.divide(TWO, RoundingMode.HALF_UP)), "1/2 pi radians");
        assertEquals(new BigDecimal("180.00000"), numberConversions.toDegrees(context, PI), "pi radians");
        assertEquals(new BigDecimal("360.00000"), numberConversions.toDegrees(context, PI.multiply(TWO)), "2pi radians");
    }

    /**
     * Unit test {@link NumberConversions#toRadians(EelContext, BigDecimal)}
     */
    @Test
    public void test_toRadians() {
        assertEquals(new BigDecimal("0.52359878"), numberConversions.toRadians(context, new BigDecimal("30")), "30 degrees");
        assertEquals(new BigDecimal("0.78539816"), numberConversions.toRadians(context, new BigDecimal("45")), "45 degrees");
        assertEquals(new BigDecimal("1.0471976"), numberConversions.toRadians(context, new BigDecimal("60")), "60 degrees");
        assertEquals(new BigDecimal("1.5707963"), numberConversions.toRadians(context, new BigDecimal("90")), "90 degrees");
        assertEquals(new BigDecimal("3.1415927"), numberConversions.toRadians(context, new BigDecimal("180")), "180 degrees");
        assertEquals(new BigDecimal("6.2831853"), numberConversions.toRadians(context, new BigDecimal("360")), "360 degrees");
    }
}