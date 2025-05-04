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
     * Entry point for the {@code padLeft} function, which adds {@code pad} characters to the start of the
     * {@code text} so that it is at least {@code width} characters long
     * <br>
     * The EEL syntax for this function is <code>padLeft( text, width {, pad } )</code>
     * @param text      Text to pad
     * @param width     minimum length of the return text
     * @param pad       the padding character
     * @return          text that is at least {@code width} characters long.
     * @since 1.1
     */
    @EelFunction("padLeft")
    @Nonnull
    public String padLeft(@Nonnull String text, int width, @DefaultArgument(" ") char pad) {
        return pad(text, width, pad, true);
    }

    /**
     * Entry point for the {@code padRight} function, which adds {@code pad} characters to the end of the
     * {@code text} so that it is at least {@code width} characters long
     * <br>
     * The EEL syntax for this function is <code>padRight( text, width {, pad } )</code>
     * @param text      Text to pad
     * @param width     minimum length of the return text
     * @param pad       the padding character
     * @return          text that is at least {@code width} characters long.
     * @since 1.1
     */
    @EelFunction("padRight")
    @Nonnull
    public String padRight(@Nonnull String text, int width, @DefaultArgument(" ") char pad) {
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
