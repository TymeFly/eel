package com.github.tymefly.eel.function.util;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Data conversion functions
 */
@PackagedEelFunction
public class Convert {
    /**
     * Entry point for the {@code char} function, which converts a codepoint to a unicode character and returns it
     * as a single character {@code text} value.
     * <br>
     * The EEL syntax for this function is <code>char( codepoint )</code>
     * @param codepoint unicode character value
     * @return          a single character text value
     * @see #codepoint(char)
     * @since 1.1
     */
    @EelFunction(name = "char")
    @Nonnull
    public String toChar(int codepoint) {
        return String.valueOf((char) codepoint);
    }


    /**
     * Entry point for the {@code int} function, which returns the unicode codepoint of the first character in
     * the {@code text}
     * <br>
     * The EEL syntax for this function is <code>codepoint( text )</code>
     * @param character     the first character of the text value
     * @return              the unicode codepoint of the character
     * @see #toChar(int)
     * @since 1.1
     */
    @EelFunction(name = "codepoint")
    public int codepoint(char character) {
        return character;
    }
}
