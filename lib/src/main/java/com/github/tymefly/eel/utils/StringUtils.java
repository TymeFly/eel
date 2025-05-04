package com.github.tymefly.eel.utils;

import javax.annotation.Nonnull;

/**
 * Static string manipulation functions
 */
public class StringUtils {
    private StringUtils() {
    }


    /**
     * Returns a string containing all the characters in the {@code text} String but in title case.
     * Characters will be converted upper case only if they are either the first character in the string or
     * if they are proceeded by at least one whitespace character. All other characters will be converted to
     * lower case. The definition of a whitespace, uppercase letter and lowercase letter are all based on the
     * functions in {@link Character}
     * @param text      text that will be returned with the first character in title case
     * @return a string containing all the characters in the {@code text} String but in title case.
     * @see Character#isWhitespace(int)
     * @see Character#isUpperCase(int)
     * @see Character#isLowerCase(int)
     * @see Character#toUpperCase(int)
     * @see Character#toLowerCase(int)
     */
    @Nonnull
    public static String toTitleCase(@Nonnull String text) {
        int length = text.length();
        StringBuilder builder = new StringBuilder(length);
        int index = 0;
        boolean toUpper = true;

        while (index < length) {
            int codePoint = text.codePointAt(index);

            if (toUpper && Character.isLowerCase(codePoint)) {
                codePoint = Character.toUpperCase(codePoint);
                toUpper = false;
            } else if (!toUpper && Character.isUpperCase(codePoint)) {
                codePoint = Character.toLowerCase(codePoint);
            } else {
                toUpper = Character.isWhitespace(codePoint);
            }

            builder.appendCodePoint(codePoint);

            index += Character.isBmpCodePoint(codePoint) ? 1 : 2;
        }

        return builder.toString();
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
     * Returns the left most part of a String containing at most {@code length} characters.
     * @param text      Text to take character from
     * @param length    If positive this is the maximum number of characters to return.
     *                  If negative this is an index from the end of the {@code text} where -1 is the last character
     * @return the specified substring
     */
    @Nonnull
    public static String left(@Nonnull String text, int length) {
        int count = text.length();
        String result;

        if (length < 0) {
            length = Math.max(count + length, 0);
        }

        if (length > count) {
            result = text;
        } else {
            result = text.substring(0, length);
        }

        return result;
    }

    /**
     * Returns the middle part of a String starting from index {@code start} and containing at most {@code length}
     * characters. The specification of this method deliberately matches the one used by bash
     * @param text      Text to take characters from
     * @param position  If positive this is a zero-based index of start of the {@code text}.
     *                  If negative this is an index from the end of the {@code text} where -1 is the last character
     * @param length    If positive this is the maximum number of characters to return.
     *                  If negative this is an index from the end of the {@code text} where -1 is the last character
     * @return          the specified substring
     * @see <a href=https://tldp.org/LDP/abs/html/abs-guide.html#SUBSTREXTR01>tldp.org</a>
     */
    @Nonnull
    public static String mid(@Nonnull String text, int position, int length) {
        String result;
        int stringLength = text.length();
        long start = position;                              // longs ensure (position + length) never overflow
        long longLength = length;
        long end;

        if ((start < 0 ) && (longLength < 0)) {             // position and length are both -ve
            longLength = -position + length;
            start = stringLength + position;
        } else if (longLength < 0) {                        // position is +ve, length is -ve.
            longLength = stringLength + length - start;         // length is an index from the end of text
        } else if (start < 0) {                             // position is -ve, length is positive
            start = stringLength + position;                    // position is neg 1-based index from the end
        } else {                                            // position and length are both +ve
            // No special action required
        }

        end = (int) Math.min(start + longLength, stringLength);

        if ((start < 0) || (end < start)) {
            result = "";
        } else {
            result = text.substring((int) start, (int) end);
        }

        return result;
    }

    /**
     * Returns the right most part of a String containing at most {@code length} characters.
     * If Count is negative return an empty string
     * @param text      Text to take character from
     * @param length    If positive this is the maximum number of characters to return.
     *                  If negative this is an index from the start of the {@code text} where -1 is the first character
     * @return          the specified substring
     */
    @Nonnull
    public static String right(@Nonnull String text, int length) {
        String result;
        int count = text.length();

        if (length < 0) {
            length = Math.max(count + length, 0);
        }

        if (length > count) {
            result = text;
        } else {
            result = text.substring(count - length);
        }

        return result;
    }
}
