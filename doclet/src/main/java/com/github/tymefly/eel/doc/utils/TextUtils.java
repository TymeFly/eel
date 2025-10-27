package com.github.tymefly.eel.doc.utils;

import javax.annotation.Nonnull;

/**
 * Text manipulation utilities
 */
public class TextUtils {
    private TextUtils() {
    }


    /**
     * Capitalise the first letter in a {@code sentence}. If {@code sentence} contains more than one
     * sentence then only the first is capitalised. Leading white space is removed.
     * @param sentence      sentence which might not be in sentence case
     * @return sentence with the first letter in upper case
     */
    @Nonnull
    public static String capitalise(@Nonnull String sentence) {
        sentence = sentence.stripLeading();

        if (!sentence.isEmpty()) {
            char first = sentence.charAt(0);
            char upper = Character.toUpperCase(first);

            if (first == upper) {
                // Do nothing - it's already capitalised
            } else if (sentence.length() == 1) {
                sentence = Character.toString(upper);
            } else {
                sentence = upper + sentence.substring(1);
            }
        }

        return sentence;
    }
}
