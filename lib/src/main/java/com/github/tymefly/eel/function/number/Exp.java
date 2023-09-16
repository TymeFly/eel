package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that calculates the natural exponent of value (e<sup>value</sup>)
 * <br>
 * The EEL syntax for this function is <code>exp( value )</code>
 * @see Ln
 */
@PackagedEelFunction
public class Exp {
    /**
     * Entry point for the {@code exp} function
     * @param context   The current EEL Context
     * @param value     the value to calculate the exponent for
     * @return          the natural exponent of {@code value} with the precision specified in the {@code context}
     */
    @EelFunction(name = "exp")
    @Nonnull
    public BigDecimal exp(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.exp(value, context.getMathContext());
    }
}
