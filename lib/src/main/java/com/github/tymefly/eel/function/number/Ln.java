package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that calculates natural Logs for {@code value}
 * <br>
 * The EEL syntax for this function is <code>ln( value )</code>
 * @see Ln
 * @see Exp
 */
@PackagedEelFunction
public class Ln {
    /**
     * Entry point for the {@code ln} function
     * @param context   The current EEL Context
     * @param value     value to take the log from
     * @return          the natural logarithm in base e with the precision specified in the {@code context}
     */
    @EelFunction(name = "ln")
    @Nonnull
    public BigDecimal ln(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.log(value, context.getMathContext());
    }
}
