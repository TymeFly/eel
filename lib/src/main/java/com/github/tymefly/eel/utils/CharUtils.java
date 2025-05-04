package com.github.tymefly.eel.utils;

/**
 * Utilities for characters
 */
public class CharUtils {
    private static final int HEX_VALUE_A = 10;

    private CharUtils() {
    }

    /**
     * Returns {@literal true} only if the {@code character} is an ASCII printable character
     * @param character     character to test
     * @return {@literal true} only if the {@code character} is an ASCII printable character
     */
    public static boolean isAsciiPrintable(char character) {
        return ((character >= ' ') && (character <= '~'));
    }


    /**
     * Convert a hex character to its binary value. If {@code in} is not a hex characters then {@literal -1}
     * is returned.
     * @param hex    character to convert.
     * @return      binary value, or {@literal -1} if {@code in} is not a hex characters.
     */
    public static int hexValue(char hex) {
        int value;

        if ((hex >= '0') && (hex <= '9')) {
            value = hex - '0';
        } else if ((hex >= 'A') && (hex <= 'F')) {
            value = hex - 'A' + HEX_VALUE_A;
        } else if ((hex >= 'a') && (hex <= 'f')) {
            value = hex - 'a' + HEX_VALUE_A;
        } else {
            value = -1;
        }

        return value;
    }
}
