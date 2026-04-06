package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that perform number rounding operations.
 * @since 1.1
 */
@PackagedEelFunction
public class Rounding {
    /**
     * Returns the {@code number} rounded to the specified precision.
     * @param number     the number to round
     * @param precision  the number of decimal places to round to
     * @return           the {@code number} rounded to the specified precision
     * @throws IllegalArgumentException if {@code precision} is negative
     * @see #truncate(BigDecimal, int)
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 1.1
     */
    @EelFunction("number.round")
    @Nonnull
    public BigDecimal round(@Nonnull BigDecimal number,
                            @DefaultArgument(value = "0", description = "round to an integral value") int precision)
            throws IllegalArgumentException {
        if (precision < 0) {
            throw new IllegalArgumentException("Invalid precision: " + precision);
        }

        return number.setScale(precision, RoundingMode.HALF_UP);
    }


    /**
     * Returns the {@code number} truncated to the specified precision.
     * @param number     the number to truncate
     * @param precision  the number of decimal places to retain
     * @return           the {@code number} with digits beyond the specified precision discarded
     * @throws IllegalArgumentException if {@code precision} is negative
     * @see #round(BigDecimal, int)
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 1.1
     */
    @EelFunction("number.truncate")
    @Nonnull
    public BigDecimal truncate(@Nonnull BigDecimal number,
                               @DefaultArgument(value = "0", description = "round to an integral value") int precision)
            throws IllegalArgumentException {
        if (precision < 0) {
            throw new IllegalArgumentException("Invalid precision: " + precision);
        }

        return number.setScale(precision, RoundingMode.DOWN);
    }


    /**
     * Returns the smallest integer value that is greater than or equal to the given {@code number}.
     * @param number     the number to round up
     * @return           the smallest integer greater than or equal to {@code number}
     * @see #round(BigDecimal, int)
     * @see #truncate(BigDecimal, int)
     * @see #floor(BigDecimal)
     * @since 2.0
     */
    @EelFunction("number.ceil")
    @Nonnull
    public BigDecimal ceil(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.CEILING);
    }


    /**
     * Returns the largest integer value that is less than or equal to the given {@code number}.
     * @param number     the number to round down
     * @return           the largest integer less than or equal to {@code number}
     * @see #round(BigDecimal, int)
     * @see #truncate(BigDecimal, int)
     * @see #ceil(BigDecimal)
     * @since 2.0
     */
    @EelFunction("number.floor")
    @Nonnull
    public BigDecimal floor(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.FLOOR);
    }
}
