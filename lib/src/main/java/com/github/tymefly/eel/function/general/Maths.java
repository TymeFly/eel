package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions for general-purpose mathematical operations.
 * @since 1.0
 */
@PackagedEelFunction
public class Maths {
    /**
     * Returns the absolute value of the specified number using the precision defined by the context.
     * @param context   the current EEL context
     * @param value     the number whose absolute value is to be calculated
     * @return          the absolute value of the specified number, rounded according to the context
     * @see #sgn(BigDecimal)
     * @since 1.0
     */
    @EelFunction("abs")
    @Nonnull
    public BigDecimal abs(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return value.abs(context.getMathContext());
    }

    /**
     * Returns the sign of the specified numeric value.
     * @param value     the number whose sign is to be determined
     * @return          {@literal -1} if the value is less than 0;
     *                  {@literal 0} if the value is equal to 0;
     *                  {@literal +1} if the value is greater than 0
     * @see #abs(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("sgn")
    public int sgn(@Nonnull BigDecimal value) {
        return value.signum();
    }    
    
    
    /**
     * Returns the natural exponent of the specified value using the precision defined by the context.
     * This is {@literal e} raised to {@code value}.
     * @param context   the current EEL context
     * @param value     the number for which to calculate the exponent
     * @return          {@literal e} raised to {@code value}, rounded according to the context
     * @see #ln(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("exp")
    @Nonnull
    public BigDecimal exp(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.exp(value, context.getMathContext());
    }

    /**
     * Computes the factorial of the specified value using the precision defined by the context.
     * Throws an exception if the value is negative.
     * @param context   the current EEL context
     * @param value     the non-negative number whose factorial is to be computed
     * @return          the factorial of the specified value, calculated with the precision defined by the context
     * @since 1.0
     */
    @EelFunction("factorial")
    @Nonnull
    public BigDecimal factorial(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.factorial(value, context.getMathContext());
    }

    /**
     * Returns the natural logarithm (base {@literal e}) of the specified value using the precision defined
     * by the context.
     * Throws an exception if the value is not positive.
     * @param context   the current EEL context
     * @param value     the number whose natural logarithm is to be calculated
     * @return          the natural logarithm (base {@literal e}) of the specified value, calculated with the
     *                  precision defined by the context
     * @throws ArithmeticException if the value is not positive
     * @see #exp(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("ln")
    @Nonnull
    public BigDecimal ln(@Nonnull EelContext context, @Nonnull BigDecimal value) throws ArithmeticException {
        return BigDecimalMath.log(value, context.getMathContext());
    }


    /**
     * Returns the base-10 logarithm of the specified value using the precision defined by the context.
     * Throws an exception if the value is not positive.
     * @param context   the current EEL context
     * @param value     the number whose base-10 logarithm is to be calculated
     * @return          the base-10 logarithm of the specified value, calculated with the precision defined
     *                  by the context
     * @throws ArithmeticException if the value is not positive
     * @see #ln(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("log")
    @Nonnull
    public BigDecimal log(@Nonnull EelContext context, @Nonnull BigDecimal value) throws ArithmeticException {
        return BigDecimalMath.log10(value, context.getMathContext());
    }

    /**
     * Returns the n-th root of the specified value using the precision defined by the context.
     * @param context   the current EEL context
     * @param value     the number whose n-th root is to be calculated
     * @param base      the degree of the root (e.g. 2 for square root)
     * @return          the n-th root of the specified value, calculated with the precision defined by
     *                  the context
     * @throws ArithmeticException if the value is not positive
     * @see #exp(EelContext, BigDecimal)
     * @since 1.0
     */
    @EelFunction("root")
    @Nonnull
    public BigDecimal root(@Nonnull EelContext context,
                           @Nonnull BigDecimal value,
                           @DefaultArgument("2") @Nonnull BigDecimal base) throws ArithmeticException {
        return BigDecimalMath.root(value, base, context.getMathContext());
    }
}
