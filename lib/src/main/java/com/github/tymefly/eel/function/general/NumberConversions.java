package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions for numeric conversions.
 * @since 2.1
 */
@PackagedEelFunction
public class NumberConversions {
    /**
     * Converts the specified radian value to degrees using the precision defined by the context.
     * @param context   the current EEL context
     * @param value     the angle in radians to be converted to degrees
     * @return          the angle expressed in degrees, calculated with the precision defined by the context
     * @see #toRadians(EelContext, BigDecimal)
     * @since 2.1
     */
    @EelFunction("toDegrees")
    @Nonnull
    public BigDecimal toDegrees(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toDegrees(value, context.getMathContext());
    }

    /**
     * Converts the specified degree value to radians using the precision defined by the context.
     * @param context   the current EEL context
     * @param value     the angle in degrees to be converted to radians
     * @return          the angle expressed in radians, calculated with the precision defined by the context
     * @see #toDegrees(EelContext, BigDecimal)
     * @since 2.1
     */
    @EelFunction("toRadians")
    @Nonnull
    public BigDecimal toRadians(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toRadians(value, context.getMathContext());
    }
}
