package com.github.tymefly.eel.utils;

import javax.annotation.Nonnull;

/**
 * Static string manipulation functions
 */
public class StringUtils {
    private StringUtils() {
    }


    /**
     * Returns a new string containing all the characters in the {@code text} String but with the
     * case of the first character changed to upper case
     * If the first character is not alphabetical then the returned string is equal to the input {@code text}
     * @param text      text that will be returned with the first character in upper case
     * @return a string containing all the characters in {@code text} but with the case of the first character
     *          in uppercase
     */
    @Nonnull
    public static String upperFirst(@Nonnull String text) {
        if (!text.isEmpty()) {
            char first = text.charAt(0);

            if (!Character.isUpperCase(first)) {
                text = Character.toUpperCase(first) + text.substring(1);
            }
        }

        return text;
    }

    /**
     * Returns a new string containing all the characters in the {@code text} String but with the
     * case of the first character changed to lower case
     * If the first character is not alphabetical then the returned string is equal to the input {@code text}
     * @param text      text that will be returned with the first character in lower case
     * @return a string containing all the characters in {@code text} but with the case of the first character
     *          in lowercase
     */
    @Nonnull
    public static String lowerFirst(@Nonnull String text) {
        if (!text.isEmpty()) {
            char first = text.charAt(0);

            if (!Character.isLowerCase(first)) {
                text = Character.toLowerCase(first) + text.substring(1);
            }
        }

        return text;
    }

    /**
     * Returns a new string containing all the characters in the {@code text} String but with the
     * case of the first character toggled
     * If the first character is not alphabetical then the returned string is equal to the input {@code text}
     * @param text      text that will be returned with the first character toggled
     * @return a string containing all the characters in {@code text} but with the case of the first character toggled
     */
    @Nonnull
    public static String toggleFirst(@Nonnull String text) {
        if (!text.isEmpty()) {
            char first = text.charAt(0);

            first = Character.isUpperCase(first) ? Character.toLowerCase(first) :  Character.toUpperCase(first);
            text = first + text.substring(1);
        }

        return text;
    }

    /**
     * Returns a new string containing all the characters in the {@code text} String with their case toggled
     * Non-alphabetical characters are not effected
     * @param text      text to toggle
     * @return a string containing all the characters in the {@code text} String with their case toggled
     */
    @Nonnull
    public static String toggleAll(@Nonnull String text) {
        if (!text.isEmpty()) {
            StringBuilder builder = new StringBuilder(text);
            int index = builder.length();

            while (index-- != 0) {
                char change = builder.charAt(index);

                change = Character.isUpperCase(change) ? Character.toLowerCase(change) :  Character.toUpperCase(change);
                builder.setCharAt(index, change);
            }

            text = builder.toString();
        }

        return text;
    }


    /**
     * Returns the left most part of a String containing at most {@code count} characters.
     * If Count is negative return an empty string
     * @param text      Text to take character from
     * @param count     the maximum number of characters to take
     * @return the specified substring
     */
    @Nonnull
    public static String left(@Nonnull String text, int count) {
        String result;

        if (count <= 0) {
            result = "";
        } else if (count > text.length()) {
            result = text;
        } else {
            result = text.substring(0, count);
        }

        return result;
    }

    /**
     * Returns the middle part of a String starting from index {@code start} and terminating at index {@code end}
     * If the {@code start} is beyond {@code end} then return an empty string.
     * If {@code start} is negative cap it to the start of the {@code text}
     * If {@code end} is beyond the length of the text then cap it to the end of the {@code text}
     * @param text      Text to take character from
     * @param start     zero based index of first character to return
     * @param count     maximum number of characters to return
     * @return          the specified substring
     */
    @Nonnull
    public static String mid(@Nonnull String text, int start, int count) {
        String result;
        int length = text.length();

        start = Math.max(start, 0);
        int end = Math.min(start + count, length);

        if (end < start) {
            result = "";
        } else {
            result = text.substring(start, end);
        }

        return result;
    }

    /**
     * Returns the right most part of a String containing at most {@code count} characters.
     * If Count is negative return an empty string
     * @param text      Text to take character from
     * @param count     the maximum number of characters to take
     * @return          the specified substring
     */
    @Nonnull
    public static String right(@Nonnull String text, int count) {
        String result;
        int length = text.length();

        if (count <= 0) {
            result = "";
        } else if (count > length) {
            result = text;
        } else {
            result = text.substring(length - count);
        }

        return result;
    }
}
