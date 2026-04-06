package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that return mathematical constants.
 * @since 2.0
 */
@PackagedEelFunction
public class Constants {
    private static final int SPEED_OF_LIGHT = 299_792_458;


    /**
     * Returns the value of {@code pi} calculated to the precision defined by the context.
     * @param context    the current EEL context
     * @return           the value of {@code pi} at the precision defined by the context
     * @since 2.0
     */
    @EelFunction("number.pi")
    @Nonnull
    public BigDecimal pi(@Nonnull EelContext context) {
        return BigDecimalMath.pi(context.getMathContext());
    }

    /**
     * Returns the value of {@code e} calculated to the precision defined by the context.
     * @param context    the current EEL context
     * @return           the value of {@code e} at the precision defined by the context
     * @since 2.0
     */
    @EelFunction("number.e")
    @Nonnull
    public BigDecimal e(@Nonnull EelContext context) {
        return BigDecimalMath.e(context.getMathContext());
    }

    /**
     * Returns the speed of light in metres per second.
     * @return           the constant value of {@code c} in metres per second
     * @since 2.0
     */
    @EelFunction("number.c")
    public long c() {
        return SPEED_OF_LIGHT;
    }
}
