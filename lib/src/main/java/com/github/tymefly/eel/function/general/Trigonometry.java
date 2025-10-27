package com.github.tymefly.eel.function.general;

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
public class Trigonometry {
    /**
     * Calculates the sine of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     an angle in radians
     * @return          the sine of {@code value} with the precision specified by the {@code context}
     * @see #asin(EelContext, BigDecimal)
     */
    @EelFunction("sin")
    @Nonnull
    public BigDecimal sin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.sin(value, context.getMathContext());
    }


    /**
     * Calculates the cosine of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     an angle in radians
     * @return          the cosine of {@code value} with the precision specified by the {@code context}
     * @see #acos(EelContext, BigDecimal)
     */
    @EelFunction("cos")
    @Nonnull
    public BigDecimal cos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.cos(value, context.getMathContext());
    }


    /**
     * Calculates the tangens of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     an angle in radians
     * @return          the tangent of {@code value} with the precision specified by the {@code context}
     * @see #atan(EelContext, BigDecimal)
     */
    @EelFunction("tan")
    @Nonnull
    public BigDecimal tan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.tan(value, context.getMathContext());
    }


    /**
     * Calculates the arcsine (inverse sine) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     a numeric value for which to calculate the arcsine
     * @return          the arcsine of {@code value} with the precision specified by the {@code context}
     * @see #sin(EelContext, BigDecimal)
     */
    @EelFunction("asin")
    @Nonnull
    public BigDecimal asin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.asin(value, context.getMathContext());
    }


    /**
     * Calculates the arccosine (inverse cosine) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     a numeric value for which to calculate the arccosine
     * @return          the arccosine of {@code value} with the precision specified by the {@code context}
     * @see #cos(EelContext, BigDecimal)
     */
    @EelFunction("acos")
    @Nonnull
    public BigDecimal acos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.acos(value, context.getMathContext());
    }


    /**
     * Calculates the arctangent (inverse tangent) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     a numeric value for which to calculate the arctangent
     * @return          the arctangent of {@code value} with the precision specified by the {@code context}
     * @see #tan(EelContext, BigDecimal)
     */
    @EelFunction("atan")
    @Nonnull
    public BigDecimal atan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.atan(value, context.getMathContext());
    }
}
