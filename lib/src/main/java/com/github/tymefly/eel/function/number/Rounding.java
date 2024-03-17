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
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
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
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 1.1
     */
    @EelFunction(name = "number.truncate")
    @Nonnull
    public BigDecimal truncate(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.DOWN);
    }


    /**
     * Entry point for the {@code number.ceil} function, which returns value that is greater than or equal to
     * {@code number} and is equal to a mathematical integer.
     * <br>
     * The EEL syntax for this function is <code>number.ceil( number )</code>
     * @param number    Number to floor
     * @return          the value that is greater than or equal to {@code number} and is an integer
     * @see #round(BigDecimal)
     * @see #truncate(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 2.0
     */
    @EelFunction(name = "number.ceil")
    @Nonnull
    public BigDecimal ceil(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.CEILING);
    }


    /**
     * Entry point for the {@code number.floor} function, which returns value that is less than or equal to
     * {@code number} and is equal to a mathematical integer.
     * <br>
     * The EEL syntax for this function is <code>number.floor( number )</code>
     * @param number    Number to floor
     * @return          the value that is less than or equal to {@code number} and is an integer
     * @see #round(BigDecimal)
     * @see #truncate(BigDecimal)
     * @see #ceil(BigDecimal)
     * @since 2.0
     */
    @EelFunction(name = "number.floor")
    @Nonnull
    public BigDecimal floor(@Nonnull BigDecimal number) {
        return number.setScale(0, RoundingMode.FLOOR);
    }
}
