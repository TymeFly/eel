package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that perform trigonometric calculations.
 * @since 1.0
 */
@PackagedEelFunction
public class Trigonometry {
    /**
     * Calculates the sine of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the angle in radians for which to calculate the sine
     * @return          the sine of {@code value}, calculated using the precision defined by the context
     * @see #asin(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("sin")
    @Nonnull
    public BigDecimal sin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.sin(value, context.getMathContext());
    }


    /**
     * Calculates the cosine of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the angle in radians for which to calculate the cosine
     * @return          the cosine of {@code value}, calculated using the precision defined by the context
     * @see #acos(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("cos")
    @Nonnull
    public BigDecimal cos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.cos(value, context.getMathContext());
    }


    /**
     * Calculates the tangent of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the angle in radians for which to calculate the tangent
     * @return          the tangent of {@code value}, calculated using the precision defined by the context
     * @see #atan(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("tan")
    @Nonnull
    public BigDecimal tan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.tan(value, context.getMathContext());
    }


    /**
     * Calculates the arcsine (inverse sine) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the value for which to calculate the arcsine
     * @return          the arcsine of {@code value}, calculated using the precision defined by the context
     * @see #sin(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("asin")
    @Nonnull
    public BigDecimal asin(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.asin(value, context.getMathContext());
    }


    /**
     * Calculates the arccosine (inverse cosine) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the value for which to calculate the arccosine
     * @return          the arccosine of {@code value}, calculated using the precision defined by the context
     * @see #cos(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("acos")
    @Nonnull
    public BigDecimal acos(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.acos(value, context.getMathContext());
    }


    /**
     * Calculates the arctangent (inverse tangent) of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the value for which to calculate the arctangent
     * @return          the arctangent of {@code value}, calculated using the precision defined by the context
     * @see #tan(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("atan")
    @Nonnull
    public BigDecimal atan(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.atan(value, context.getMathContext());
    }
}
