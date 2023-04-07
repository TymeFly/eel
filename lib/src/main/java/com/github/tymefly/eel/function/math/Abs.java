package com.github.tymefly.eel.function.math;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns the absolute {@code value} of a number
 * <br>
 * The EEL syntax for this function is <code>abs( value )</code>
 * @see Sgn
 */
@PackagedEelFunction
public class Abs {
    /**
     * Entry point for the {@code abs} function
     * @param context   The current EEL Context
     * @param value     the {@code value} to calculate absolute value for
     * @return          the absolute {@code value} of a number with the precision specified in the {@code context}
     */
    @EelFunction(name = "abs")
    @Nonnull
    public BigDecimal abs(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return value.abs(context.getMathContext());
    }
}
