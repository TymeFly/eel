package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that calculates factorial of {@code value}.
 * <br>
 * The EEL syntax for this function is <code>factorial( value )</code>
 */
@PackagedEelFunction
public class Factorial {
    /**
     * Entry point for the {@code factorial} function
     * @param context   The current EEL Context
     * @param value     value to calculate the factorial for
     * @return          the factorial of {@code value} with the precision specified in the {@code context}
     */
    @EelFunction("factorial")
    @Nonnull
    public BigDecimal factorial(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.factorial(value, context.getMathContext());
    }
}
