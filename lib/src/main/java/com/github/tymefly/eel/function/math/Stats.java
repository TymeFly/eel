package com.github.tymefly.eel.function.math;

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
     * Entry point for the {@code avg} function. That An EEL function that returns the average of a set of numbers
     * <br>
     * The EEL syntax for this function is <code>avg( values... )</code>
     * @param context   The current EEL Context
     * @param first     the first value to average
     * @param others    optional additional values to average
     * @return          the mean average of all the values passed to this method
     */
    @EelFunction(name = "avg")
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
     * Entry point for the {@code max} function that returns the largest value in a set of numbers.
     * <br>
     * The EEL syntax for this function is <code>max( values... )</code>
     * @param first     the first value to test
     * @param others    optional values to test
     * @return          the largest value passed to this method
     * @see #min(BigDecimal, BigDecimal...) 
     */
    @EelFunction(name = "max")
    @Nonnull
    public BigDecimal max(@Nonnull BigDecimal first, BigDecimal... others) {
        BigDecimal result = first;

        for (var test : others) {
            result = result.max(test);
        }

        return result;
    }

    /**
     * Entry point for the {@code min} function that returns the lowest value in a set of numbers.
     * <br>
     * The EEL syntax for this function is <code>min( values... )</code>
     * @param first     the first value to test
     * @param others    optional values to test
     * @return          the lowest value passed to this method
     * @see #max(BigDecimal, BigDecimal...) 
     */
    @EelFunction(name = "min")
    @Nonnull
    public BigDecimal min(@Nonnull BigDecimal first, BigDecimal... others) {
        BigDecimal result = first;

        for (var test : others) {
            result = result.min(test);
        }

        return result;
    }    
}
