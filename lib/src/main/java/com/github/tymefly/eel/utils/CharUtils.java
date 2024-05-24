package com.github.tymefly.eel.utils;

/**
 * Utilities for characters
 */
public class CharUtils {
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
}
