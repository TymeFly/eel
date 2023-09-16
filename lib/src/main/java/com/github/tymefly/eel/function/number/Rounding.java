package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Number Rounding functions
 */
@PackagedEelFunction
public class Rounding {
    /**
     * Entry point for the {@code number.round} function, which returns the {@code number} rounded to the
     * closest non-fractional number.
     * <br>
     * The EEL syntax for this function is <code>number.round( number )</code>
     * @param number    Number to round
     * @return          the {@code number} with its fractional part discarded
     * @see #truncate(BigDecimal)
     * @since 1.1
     */
    @EelFunction(name = "number.round")
    @Nonnull
    public BigDecimal round(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.HALF_UP);
    }


    /**
     * Entry point for the {@code number.truncate} function, which returns the {@code number} with its
     * fractional part discarded
     * <br>
     * The EEL syntax for this function is <code>number.truncate( number )</code>
     * @param number    Number to truncate
     * @return          the {@code number} with its fractional part discarded
     * @see #round(BigDecimal)
     * @since 1.1
     */
    @EelFunction(name = "number.truncate")
    @Nonnull
    public BigDecimal truncate(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.DOWN);
    }
}
