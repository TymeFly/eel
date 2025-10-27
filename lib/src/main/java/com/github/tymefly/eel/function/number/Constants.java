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
     * Returns the value of {@code pi} to the precision specified by the {@code context}.
     * @param context    the current EEL context.
     * @return           the value of {@code pi}.
     * @since 2.0.0
     */
    @EelFunction("number.pi")
    @Nonnull
    public BigDecimal pi(@Nonnull EelContext context) {
        return BigDecimalMath.pi(context.getMathContext());
    }

    /**
     * Returns the value of {@code e} to the precision specified by the {@code context}.
     * @param context    the current EEL context.
     * @return           the value of {@code e}.
     * @since 2.0.0
     */
    @EelFunction("number.e")
    @Nonnull
    public BigDecimal e(@Nonnull EelContext context) {
        return BigDecimalMath.e(context.getMathContext());
    }

    /**
     * Returns the speed of light in metres per second.
     * @return           the value of {@code c}.
     * @since 2.0.0
     */
    @EelFunction("number.c")
    public long c() {
        return SPEED_OF_LIGHT;
    }
}
