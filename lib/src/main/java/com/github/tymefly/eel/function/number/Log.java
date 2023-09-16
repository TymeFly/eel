package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that calculates Logs in base 10 for {@code value}
 * <br>
 * The EEL syntax for this function is <code>log( value )</code>
 */
@PackagedEelFunction
public class Log {
    /**
     * Entry point for the {@code log} function
     * @param context   The current EEL Context
     * @param value     value to take the log from
     * @return          the natural logarithm in base 10 with the precision specified in the {@code context}
     */
    @EelFunction(name = "log")
    @Nonnull
    public BigDecimal log(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.log10(value, context.getMathContext());
    }
}
