package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Character conversion functions.
 */
@PackagedEelFunction
public class Codepoints {
    /**
     * Returns a single-character text value corresponding to the specified Unicode {@code codepoint}.
     * @param codepoint   the numeric Unicode character value
     * @return            a single-character text value
     * @see #codepoint(char)
     * @since 1.1
     */
    @EelFunction("char")
    @Nonnull
    public String toChar(int codepoint) {
        return String.valueOf((char) codepoint);
    }


    /**
     * Returns the numeric Unicode code point of the first character in the specified {@code text}.
     * @param text      the text to evaluate; must not be blank
     * @return          the Unicode code point of the first character
     * @see #toChar(int)
     * @since 1.1
     */
    @EelFunction("codepoint")
    public int codepoint(char text) {
        return text;
    }
}
