package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Statistical functions
 */
@PackagedEelFunction
public class Stats {
    /**
     * Returns the average of a set of numbers.
     * @param context   the current EEL context
     * @param first     the first value to average
     * @param others    optional additional values to average
     * @return          the mean of all values passed to this function
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
     * Returns the largest value in a set of numbers.
     * @param first     the first value to test
     * @param others    optional additional values to test
     * @return          the largest value among the values passed to this function
     * @see #min(BigDecimal, BigDecimal...)
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
     * Returns the smallest value in a set of numbers.
     * @param first     the first value to test
     * @param others    optional additional values to test
     * @return          the smallest value among the values passed to this function
     * @see #max(BigDecimal, BigDecimal...) 
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
