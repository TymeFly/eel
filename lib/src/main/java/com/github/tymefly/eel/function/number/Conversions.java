package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL numeric conversion functions
 */
@PackagedEelFunction
public class Conversions {
    /**
     * Entry point for the {@code toDegrees} function
     * The EEL syntax for this function is <code>toDegrees(radians)</code>
     * @param context   The current EEL Context
     * @param value     radian value to convert to degrees
     * @return          the {@code value} expressed in degrees
     * @since 2.1.0
     */
    @EelFunction(name = "toDegrees")
    @Nonnull
    public BigDecimal toDegrees(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toDegrees(value, context.getMathContext());
    }

    /**
     * Entry point for the {@code toRadians} function
     * The EEL syntax for this function is <code>toRadians(degrees)</code>
     * @param context   The current EEL Context
     * @param value     degree value to convert to radians
     * @return          the {@code value} expressed in degrees
     * @since 2.1.0
     */
    @EelFunction(name = "toRadians")
    @Nonnull
    public BigDecimal toRadians(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toRadians(value, context.getMathContext());
    }
}
