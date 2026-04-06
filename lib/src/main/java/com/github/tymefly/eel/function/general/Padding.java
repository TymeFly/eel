package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that add padding to text.
 * @since 1.1
 */
@PackagedEelFunction
public class Padding {
    /**
     * Left-pads the specified text with the given character until it reaches the specified width.
     * @param text      the text to be padded
     * @param width     the minimum length of the resulting text; must be at least {@literal 0}
     * @param pad       the character used for padding
     * @return          the padded text with a length of at least the specified width
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
     * Right-pads the specified text with the given character until it reaches the specified width.
     * @param text      the text to be padded
     * @param width     the minimum length of the resulting text; must be at least {@literal 0}
     * @param pad       the character used for padding
     * @return          the padded text with a length of at least the specified width
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
