package com.github.tymefly.eel.function.general;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * General purpose maths functions
 */
@PackagedEelFunction
public class Maths {
    /**
     * Returns the absolute {@code value} of the specified number, with the precision specified by the {@code context}.
     * @param context   the current EEL context
     * @param value     the {@code value} for which to calculate the absolute value
     * @return          the absolute {@code value} of the specified number with the precision specified
     *                      by the {@code context}
     * @see #sgn(BigDecimal)
     */
    @EelFunction("abs")
    @Nonnull
    public BigDecimal abs(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return value.abs(context.getMathContext());
    }

    /**
     * Returns the sign of a numeric {@code value}.
     * @param value     the {@code value} to calculate the sign for
     * @return          {@literal -1} if {@code value} is less than {@literal 0};
     *                  {@literal 0} if {@code value} is equal to {@literal 0};
     *                  {@literal +1} if {@code value} is greater than {@literal 0}
     * @see #abs(EelContext, BigDecimal)
     */
    @EelFunction("sgn")
    public int sgn(@Nonnull BigDecimal value) {
        return value.signum();
    }    
    
    
    /**
     * Returns the natural exponent of {@code value} with the precision specified by the {@code context}.
     * This is {@literal e} raised to {@code value}
     * @param context   the current EEL context
     * @param value     the value for which to calculate the exponent
     * @return          {@literal e} raised to {@literal value}, with the precision specified by the {@code context}.
     * @see #ln(EelContext, BigDecimal) 
     */
    @EelFunction("exp")
    @Nonnull
    public BigDecimal exp(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.exp(value, context.getMathContext());
    }

    /**
     * Computes the factorial of the specified {@code value} with the precision specified by the {@code context}.
     * An exception is thrown if {@code value} is negative.
     * @param context   the current EEL context
     * @param value     the non-negative value whose factorial is to be computed
     * @return          the factorial of {@code value} calculated with the precision specified by the {@code context}
     */
    @EelFunction("factorial")
    @Nonnull
    public BigDecimal factorial(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.factorial(value, context.getMathContext());
    }

    /**
     * Returns the natural logarithm (base e) of the specified {@code value} with the precision specified
     * by the {@code context}.
     * An exception is thrown if {@code value} is negative.
     * @param context   the current EEL context
     * @param value     the value whose logarithm is to be calculated
     * @return          the natural logarithm (base {@literal e}) of {@code value} with the precision specified by
     *                      the {@code context}
     * @see #exp(EelContext, BigDecimal)
     */
    @EelFunction("ln")
    @Nonnull
    public BigDecimal ln(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.log(value, context.getMathContext());
    }


    /**
     * Returns the logarithm base 10 of the specified {@code value} with the precision specified by the {@code context}.
     * @param context   the current EEL context
     * @param value     the value whose logarithm is to be calculated
     * @return          the base-10 logarithm of {@code value} with the precision specified by the {@code context}
     */
    @EelFunction("log")
    @Nonnull
    public BigDecimal log(@Nonnull EelContext context, @Nonnull BigDecimal value) {
        return BigDecimalMath.log10(value, context.getMathContext());
    }

    /**
     * Calculates the {@code n}’th root of the specified {@code value}.
     * @param context   the current EEL context
     * @param value     the value to calculate the {@code n}’th root from
     * @param base      the base for the root; defaults to 2 for square roots
     * @return          the calculated {@code n}’th root of {@code value} with the precision specified by
     *                      the {@code context}
     */
    @EelFunction("root")
    @Nonnull
    public BigDecimal root(@Nonnull EelContext context,
                           @Nonnull BigDecimal value,
                           @DefaultArgument("2") @Nonnull BigDecimal base) {
        return BigDecimalMath.root(value, base, context.getMathContext());
    }
}
