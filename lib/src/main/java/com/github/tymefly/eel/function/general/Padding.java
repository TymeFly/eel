package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Padding functions
 */
@PackagedEelFunction
public class Padding {
    /**
     * Returns the specified {@code text} with {@code pad} characters added to the start
     * so that it is at least {@code width} characters long.
     * @param text      the text to pad
     * @param width     the minimum length of the returned text; must be at least {@literal 1}
     * @param pad       the padding character
     * @return          the {@code text} padded to be at least {@code width} characters long
     * @since 1.1
     */
    @EelFunction("padLeft")
    @Nonnull
    public String padLeft(@Nonnull String text,
                          int width,
                          @DefaultArgument(value = " ", description = "A single space") char pad) {
        return pad(text, width, pad, true);
    }

    /**
     * Returns the specified {@code text} with {@code pad} characters added to the end
     * so that it is at least {@code width} characters long.
     * @param text      the text to pad
     * @param width     the minimum length of the returned text; must be at least {@literal 1}
     * @param pad       the padding character
     * @return          the {@code text} padded to be at least {@code width} characters long
     * @since 1.1
     */
    @EelFunction("padRight")
    @Nonnull
    public String padRight(@Nonnull String text,
                           int width,
                           @DefaultArgument(value = " ", description = "A single space") char pad) {
        return pad(text, width, pad, false);
    }


    @Nonnull
    private String pad(@Nonnull String text, int width, char pad, boolean left) {
        int length = text.length();
        int padding = (width - length);
        String result;

        if (width < 0) {
            throw new IllegalArgumentException("Invalid width: " + width);
        } else if (padding <= 0) {
            result = text;
        } else if (left) {
            result = String.valueOf(pad).repeat(padding) + text;
        } else {
            result = text + String.valueOf(pad).repeat(padding);
        }

        return result;
    }
}
