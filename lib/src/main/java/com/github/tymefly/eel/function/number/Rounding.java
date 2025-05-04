package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Number Rounding functions
 */
@PackagedEelFunction
public class Rounding {
    /**
     * Entry point for the {@code number.round} function, that returns the {@code number} rounded to the
     * required precision
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <code>number.round( number )</code>
     *  <code>number.round( number, precision )</code>
     * </ul>
     * @param number    Number to round
     * @param precision precision of the rounded number, This defaults to 0 to round to an integral value.
     * @return          the {@code number} with its fractional part discarded
     * @throws IllegalArgumentException if {@code precision} is less than 0
     * @see #truncate(BigDecimal, int)
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 1.1
     */
    @EelFunction("number.round")
    @Nonnull
    public BigDecimal round(@Nonnull BigDecimal number,
                            @DefaultArgument("0") int precision) throws IllegalArgumentException {
        if (precision < 0) {
            throw new IllegalArgumentException("Invalid precision: " + precision);
        }

        return number.setScale(precision, RoundingMode.HALF_UP);
    }


    /**
     * Entry point for the {@code number.truncate} function, that returns the {@code number} truncated to the
     * required precision
     * <br>
     * The EEL syntax for this function is
     * <ul>
     *  <code>number.truncate( number )</code>
     *  <code>number.truncate( number, precision )</code>
     * </ul>
     * @param number    Number to truncate
     * @param precision precision of the rounded number, This defaults to 0 to truncate to an integral value.
     * @return          the {@code number} with its fractional part discarded
     * @throws IllegalArgumentException if {@code precision} is less than 0
     * @see #round(BigDecimal, int)
     * @see #ceil(BigDecimal)
     * @see #floor(BigDecimal)
     * @since 1.1
     */
    @EelFunction("number.truncate")
    @Nonnull
    public BigDecimal truncate(@Nonnull BigDecimal number,
                               @DefaultArgument("0") int precision) throws IllegalArgumentException {
        if (precision < 0) {
            throw new IllegalArgumentException("Invalid precision: " + precision);
        }

        return number.setScale(precision, RoundingMode.DOWN);
    }


    /**
     * Entry point for the {@code number.ceil} function, that returns value that is greater than or equal to
     * {@code number} and is equal to a mathematical integer.
     * <br>
     * The EEL syntax for this function is <code>number.ceil( number )</code>
     * @param number    Number to floor
     * @return          the value that is greater than or equal to {@code number} and is an integer
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
     * Entry point for the {@code number.floor} function, that returns value that is less than or equal to
     * {@code number} and is equal to a mathematical integer.
     * <br>
     * The EEL syntax for this function is <code>number.floor( number )</code>
     * @param number    Number to floor
     * @return          the value that is less than or equal to {@code number} and is an integer
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
