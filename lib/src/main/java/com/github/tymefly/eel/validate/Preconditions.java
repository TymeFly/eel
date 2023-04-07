package com.github.tymefly.eel.validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class to check preconditions.
 * This class is loosely based on a similar class in Google Guava, but:
 * <ul>
 *  <li> we don't want to depend on a huge library for a few simple methods </li>
 *  <li> messages are required, in Guava they are optional</li>
 *  <li> message templates take more that just the {@code %s} formatter</li>
 * </ul>
 */
public class Preconditions {
    private Preconditions() {
    }


    /**
     * Check that {@code obj} is not null.
     * Unlike {@link java.util.Objects#requireNonNull(Object, String)} we can pass a formatted string without
     * having to write a Lambda.
     * @param obj                   Object to test
     * @param message               Formatting string used to generate a message if {@code obj} is null
     * @param messageArgs           Optional argument to the {@code message}
     * @param <T>                   Type of {@code obj}
     * @return                      {@code obj}, which is guaranteed not to be null
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
     * Check that {@code expression} evaluates to {@code true}
     * @param expression            expression to test
     * @param message               Formatting string used to generate a message if {@code expression} is {@code false}
     * @param messageArgs           Optional argument to the {@code message}
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
     * Check that {@code argument} evaluates to {@code true}. This is used to valuate argument
     * @param argument              argument to test
     * @param message               Formatting string used to generate a message if {@code argument} is {@code false}
     * @param messageArgs           Optional argument to the {@code message}
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
