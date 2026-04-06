package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that perform statistical calculations.
 * @since 1.0
 */
@PackagedEelFunction
public class Stats {
    /**
     * Returns the average (arithmetic mean) of the specified numbers using the precision defined by the context.
     * @param context   the current EEL context
     * @param first     the first value to include in the average
     * @param others    optional additional values to include in the average
     * @return          the arithmetic mean of all provided values, calculated with the precision defined by the context
     * @since 1.0
     */
    @EelFunction("avg")
    @Nonnull
    public BigDecimal avg(@Nonnull EelContext context, @Nonnull BigDecimal first, BigDecimal... others) {
        BigDecimal sum = first;
        BigDecimal count = BigDecimal.valueOf(others.length + 1);

        for (var test : others) {
            sum = sum.add(test);
        }

        return sum.divide(count, context.getMathContext());
    }


    /**
     * Returns the largest value among the specified numbers.
     * @param first     the first value to compare
     * @param others    optional additional values to compare
     * @return          the maximum value among all provided values
     * @see #min(BigDecimal, BigDecimal...)
     * @since 1.0
     */
    @EelFunction("max")
    @Nonnull
    public BigDecimal max(@Nonnull BigDecimal first, BigDecimal... others) {
        BigDecimal result = first;

        for (var test : others) {
            result = result.max(test);
        }

        return result;
    }

    /**
     * Returns the smallest value among the specified numbers.
     * @param first     the first value to compare
     * @param others    optional additional values to compare
     * @return          the minimum value among all provided values
     * @see #max(BigDecimal, BigDecimal...)
     * @since 1.0
     */
    @EelFunction("min")
    @Nonnull
    public BigDecimal min(@Nonnull BigDecimal first, BigDecimal... others) {
        BigDecimal result = first;

        for (var test : others) {
            result = result.min(test);
        }

        return result;
    }    
}
