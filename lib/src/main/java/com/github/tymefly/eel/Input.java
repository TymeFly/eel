package com.github.tymefly.eel;

/**
 * The contract for reading data
 */
interface Input {
    char END = (char) 0;

    /**
     * Returns the current character from the input. This was previously the {@link #next} character
     * @return the current character from the input
     */
    char current();

    /**
     * Returns the lookahead character. The current character is not consumed.
     * @return the lookahead character
     */
    char next();

    /**
     * Reads the next character from the input
     * @return the current character from the input
     * @see #current()
     */
    char read();
}
