package com.github.tymefly.eel.validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
/**
 * Utility class for checking preconditions.
 * This class is loosely based on a similar class in Google Guava, but:
 * <ul>
 *  <li>does not depend on a large library for a few simple methods</li>
 *  <li>requires messages, whereas Guava makes them optional</li>
 *  <li>supports message templates with more than just the {@code %s} formatter</li>
 * </ul>
 */
public class Preconditions {
    private Preconditions() {
    }

    /**
     * Checks that {@code obj} is not null.
     * Unlike {@link java.util.Objects#requireNonNull(Object, String)}, a formatted string can be passed
     * without using a lambda.
     * @param obj          the object to test
     * @param message      the formatting string used to generate a message if {@code obj} is null
     * @param messageArgs  optional arguments for the {@code message}
     * @param <T>          the type of {@code obj}
     * @return             {@code obj}, guaranteed not to be null
     * @throws NullPointerException if {@code obj} is null
     * @see java.util.Formatter
     */
    @Nonnull
    public static <T> T checkNotNull(@Nullable T obj,
                                     @Nonnull String message,
                                     @Nullable Object... messageArgs) throws NullPointerException {
        if (obj == null) {
            String formatted = String.format(message, messageArgs);
            throw new NullPointerException(formatted);
        }

        return obj;
    }

    /**
     * Checks that {@code obj} has been set.
     * @param obj   the object to test
     * @param name  the name of the object
     * @param <T>   the type of {@code obj}
     * @return      {@code obj}, guaranteed to be non-null
     * @throws IllegalStateException if {@code obj} is null
     */
    @Nonnull
    public static <T> T checkSet(@Nullable T obj, @Nonnull String name) throws IllegalStateException {
        if (obj == null) {
            throw new IllegalStateException(name + " has not been set");
        }
        return obj;
    }

    /**
     * Checks that {@code expression} evaluates to {@code true}.
     * @param expression  the expression to test
     * @param message     the formatting string used to generate a message if {@code expression} is {@code false}
     * @param messageArgs optional arguments for the {@code message}
     * @throws IllegalStateException if {@code expression} is false
     * @see java.util.Formatter
     */
    public static void checkState(boolean expression,
                                  @Nonnull String message,
                                  @Nullable Object... messageArgs) throws IllegalStateException {
        if (!expression) {
            String formatted = String.format(message, messageArgs);
            throw new IllegalStateException(formatted);
        }
    }

    /**
     * Checks that {@code argument} evaluates to {@code true}. Used for validating method arguments.
     * @param argument    the argument to test
     * @param message     the formatting string used to generate a message if {@code argument} is {@code false}
     * @param messageArgs optional arguments for the {@code message}
     * @throws IllegalArgumentException if {@code argument} is false
     * @see java.util.Formatter
     */
    public static void checkArgument(boolean argument,
                                     @Nonnull String message,
                                     @Nullable Object... messageArgs) throws IllegalArgumentException {
        if (!argument) {
            String formatted = String.format(message, messageArgs);
            throw new IllegalArgumentException(formatted);
        }
    }
}