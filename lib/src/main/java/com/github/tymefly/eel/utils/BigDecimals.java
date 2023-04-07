package com.github.tymefly.eel.utils;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

/**
 * Utility functions for BigDecimals
 */
public class BigDecimals {
    private BigDecimals() {
    }

    /**
     * Returns {@literal true} only if {@code left} and {@code right} have the same value.
     * The scales of the values are not considered
     * @param left      left value for comparison
     * @param right     right value for comparison
     * @return {@literal true} only if {@code left} and {@code right} have the same value.
     */
    public static boolean eq(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        return (left.compareTo(right) == 0);
    }

    /**
     * Returns {@literal true} only if {@code left} is greater than the {@code right} value.
     * The scales of the values are not considered
     * @param left      left value for comparison
     * @param right     right value for comparison
     * @return {@literal true} only if {@code left} is greater than the {@code right} value.
     */
    public static boolean gt(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        return (left.compareTo(right) > 0);
    }

    /**
     * Returns {@literal true} only if {@code left} is greater than or equal to the {@code right} value.
     * The scales of the values are not considered
     * @param left      left value for comparison
     * @param right     right value for comparison
     * @return {@literal true} only if {@code left} is greater than or equal to the {@code right} value.
     */
    public static boolean ge(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        return (left.compareTo(right) >= 0);
    }

    /**
     * Returns {@literal true} only if {@code left} is less than the {@code right} value.
     * The scales of the values are not considered
     * @param left      left value for comparison
     * @param right     right value for comparison
     * @return {@literal true} only if {@code left} is less than the {@code right} value.
     */
    public static boolean lt(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        return (left.compareTo(right) < 0);
    }

    /**
     * Returns {@literal true} only if {@code left} is less than or equal to the {@code right} value.
     * The scales of the values are not considered
     * @param left      left value for comparison
     * @param right     right value for comparison
     * @return {@literal true} only if {@code left} is less than or equal to the {@code right} value.
     */
    public static boolean le(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        return (left.compareTo(right) <= 0);
    }


    /**
     * Convert a numeric value to a BigDecimal
     * @param value     value to convert
     * @return          {@code value} as a BigDecimal
     */
    @Nonnull
    public static BigDecimal toBigDecimal(@Nonnull Number value) {
        BigDecimal result;

        if (value instanceof BigDecimal) {
            result = (BigDecimal) value;
        } else {
            result = new BigDecimal(value.toString());
        }

        return result;
    }
}
