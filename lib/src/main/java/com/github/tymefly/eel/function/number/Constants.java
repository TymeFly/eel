package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that return mathematical constant.
 */
@PackagedEelFunction
public class Constants {
    private static final int SPEED_OF_LIGHT = 299_792_458;

    /**
     * Entry point for the {@code pi} function
     * <br>
     * The EEL syntax for this function is <code>number.pi()</code>
     * @param context   The current EEL Context
     * @return the value for {@code pi}
     * @since 2.0.0
     */
    @EelFunction(name = "number.pi")
    @Nonnull
    public BigDecimal pi(@Nonnull EelContext context) {
        return BigDecimalMath.pi(context.getMathContext());
    }

    /**
     * Entry point for the {@code e} function
     * <br>
     * The EEL syntax for this function is <code>number.e()</code>
     * @param context   The current EEL Context
     * @return the value for {@code e}
     * @since 2.0.0
     */
    @EelFunction(name = "number.e")
    @Nonnull
    public BigDecimal e(@Nonnull EelContext context) {
        return BigDecimalMath.e(context.getMathContext());
    }

    /**
     * Entry point for the {@code c} function, which returns the speed of light in meters/second
     * <br>
     * The EEL syntax for this function is <code>number.c()</code>
     * @return the value for {@code c}
     * @since 2.0.0
     */
    @EelFunction(name = "number.c")
    public long c() {
        return SPEED_OF_LIGHT;
    }
}
