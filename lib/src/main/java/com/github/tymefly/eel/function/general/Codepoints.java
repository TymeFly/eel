package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * Functions that convert between characters and their Unicode code points.
 * @since 1.1
 */
@PackagedEelFunction
public class Codepoints {
    private static final int MIN_CODEPOINT = 0;
    private static final int MAX_CODEPOINT = 0x10_FFFF;

    /**
     * Returns a single-character text value corresponding to the specified Unicode {@code codepoint}.
     * @param codepoint   a valid Unicode character value
     * @return            a single-character text value
     * @throws IllegalArgumentException if the {@code codepoint} is not in the range 0 to 0x10FFFF
     * @see #codepoint(char)
     * @since 1.1
     */
    @EelFunction("char")
    @Nonnull
    public String toChar(int codepoint) throws IllegalArgumentException {
        Preconditions.checkArgument((codepoint >= MIN_CODEPOINT) && (codepoint <= MAX_CODEPOINT),
            "Invalid codepoint %d", codepoint);

        return String.valueOf((char) codepoint);
    }


    /**
     * Returns the numeric Unicode code point of the first character in the specified {@code text}.
     * @param text      the text to evaluate, which must not be blank
     * @return          the Unicode code point of the first character
     * @see #toChar(int)
     * @since 1.1
     */
    @EelFunction("codepoint")
    public int codepoint(char text) {
        return text;
    }
}
