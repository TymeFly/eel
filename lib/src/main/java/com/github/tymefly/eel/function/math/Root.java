package com.github.tymefly.eel.function.math;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that Calculates the n'th root of {@code value}.
 * <br>
 * The EEL syntax for this function is <code>root( value, base )</code>
 */
@PackagedEelFunction
public class Root {
    /**
     * Entry point for the {@code root} function
     * @param context   The current EEL Context
     * @param value     value to calculate the n'th root from
     * @param base      The base for the root. This defaults to 2 for square roots
     * @return          the calculated n'th root of {@code value} with the precision specified in the {@code context}
     */
    @EelFunction(name = "root")
    @Nonnull
    public BigDecimal root(@Nonnull EelContext context,
                           @Nonnull BigDecimal value,
                           @DefaultArgument (of = "2") @Nonnull BigDecimal base) {
        return BigDecimalMath.root(value, base, context.getMathContext());
    }
}
