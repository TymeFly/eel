package com.github.tymefly.eel.function.number;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.BigDecimals;

/**
 * An EEL function that returns the sign of a numeric {@code value}
 * <br>
 * The EEL syntax for this function is <code>sgn( value )</code>
 * @see Abs
 */
@PackagedEelFunction
public class Sgn {
    /**
     * Entry point for the {@code sgn} function
     * @param value     the {@code value} to calculate the sign for
     * @return          {@literal -1} if {@code value} is less than {@literal 0};
     *                  {@literal 0} if {@code value} is exactly equal to {@literal 0};
     *                  {@literal +1} if {@code value} is greater than {@literal 0}
     */
    @EelFunction(name = "sgn")
    public int sgn(@Nonnull BigDecimal value) {
        int result;

        if (BigDecimals.eq(value, BigDecimal.ZERO)) {
            result = 0;
        } else if (BigDecimals.lt(value, BigDecimal.ZERO)) {
            result = -1;
        } else {
            result = 1;
        }

        return result;
    }
}
