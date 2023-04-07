package com.github.tymefly.eel.function.math;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Trigonometric functions
 */
@PackagedEelFunction
public class Trig {
    /**
     * Entry point for the {@code sin} function* <br>
     * The EEL syntax for this function is <code>sin( value )</code>
     * @param context   The current EEL Context
     * @param value     an angle in radians
     * @return          sine of {@code value} with the precision specified in the {@code context}
     * @see #asin(EelContext, BigDecimal)
     */
    @EelFunction(name = "sin")
    @Nonnull
    public BigDecimal sin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.sin(value, context.getMathContext());
    }


    /**
     * Entry point for the {@code cos} function
     * <br>
     * The EEL syntax for this function is <code>cos( value )</code>
     * @param context   The current EEL Context
     * @param value     an angle in radians
     * @return          cosine of {@code value} with the precision specified in the {@code context}
     * @see #acos(EelContext, BigDecimal)
     */
    @EelFunction(name = "cos")
    @Nonnull
    public BigDecimal cos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.cos(value, context.getMathContext());
    }


    /**
     * Entry point for the {@code tan} function
     * @param context   The current EEL Context
     * @param value     an angle in radians
     * @return          tangens of {@code value} with the precision specified in the {@code context}
     * @see #atan(EelContext, BigDecimal)
     */
    @EelFunction(name = "tan")
    @Nonnull
    public BigDecimal tan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.tan(value, context.getMathContext());
    }


    /**
     * Entry point for the {@code asin} function
     * <br>
     * The EEL syntax for this function is <code>asin( value )</code>
     * @param context   The current EEL Context
     * @param value     the {@code value} to calculate the arc sine for
     * @return          arc sine of {@code value} with the precision specified in the {@code context}
     * @see #sin(EelContext, BigDecimal)
     */
    @EelFunction(name = "asin")
    @Nonnull
    public BigDecimal asin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.asin(value, context.getMathContext());
    }


    /**
     * Entry point for the {@code acos} function
     * <br>
     * The EEL syntax for this function is <code>acos( value )</code>
     * @param context   The current EEL Context
     * @param value     the {@code value} to calculate the arc cosine for
     * @return          arc cosine of {@code value} with the precision specified in the {@code context}
     * @see #cos(EelContext, BigDecimal)
     */
    @EelFunction(name = "acos")
    @Nonnull
    public BigDecimal acos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.acos(value, context.getMathContext());
    }


    /**
     * Entry point for the {@code atan} function
     * <br>
     * The EEL syntax for this function is <code>atan( value )</code>
     * @param context   The current EEL Context
     * @param value     the {@code value} to calculate the arc tangens for
     * @return          arc tangens of {@code value} with the precision specified in the {@code context}
     * @see #tan(EelContext, BigDecimal)
     */
    @EelFunction(name = "atan")
    @Nonnull
    public BigDecimal atan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.atan(value, context.getMathContext());
    }
}
