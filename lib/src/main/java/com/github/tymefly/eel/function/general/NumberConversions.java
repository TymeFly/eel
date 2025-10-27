package com.github.tymefly.eel.function.general;

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
public class NumberConversions {
    /**
     * Returns the specified radian {@code value} expressed in degrees.
     * @param context   the current EEL context
     * @param value     the radian value to convert to degrees
     * @return          the specified radian {@code value} expressed in degrees
     * @since 2.1.0
     */
    @EelFunction("toDegrees")
    @Nonnull
    public BigDecimal toDegrees(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toDegrees(value, context.getMathContext());
    }

    /**
     * Returns the specified degree {@code value} expressed in radians.
     * @param context   the current EEL context
     * @param value     the degree value to convert to radians
     * @return          the specified degree {@code value} expressed in radians
     * @since 2.1.0
     */
    @EelFunction("toRadians")
    @Nonnull
    public BigDecimal toRadians(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.toRadians(value, context.getMathContext());
    }
}
