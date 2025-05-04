package com.github.tymefly.eel.function.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.CharSetBuilder;

/**
 * EEL functions for Random Text
 */
@PackagedEelFunction
public class RandomText {
    private static final int MAX_ATTEMPTS = 10;
    private static final Map<String, String> CHAR_SET_CACHE = new HashMap<>();


    /**
     * Entry point for the {@code text.random} function
     * <br>
     * The EEL syntax for this function is
     * <ul>
     *  <code>text.random()</code>
     *  <code>text.random( width )</code>
     *  <code>text.random( width, valid-characters )</code>
     * </ul>
     * <b>Note:</b> This function does not guarantee that a character is chosen from any ranges within the
     * {@code valid-characters} or the number unique characters in the generated text.
     * @param width                 length of the returned text. This defaults to 10
     * @param validCharacters       characters that may be in the returned text. This can contain ranges of chars
     *                              which are written by separating the first and last character with a dash
     *                              ({@literal -}). For example {@code a-z} or {@code 0-9}. A literal dash {@literal -})
     *                              can be passed if it either the first or last character in the text.
     *                              This defaults to {@code A-Za-z0-9}
     *
     * @return some random text
     * @since 3.0.0
     */
    @EelFunction("text.random")
    @Nonnull
    public String random(@DefaultArgument("10") int width,
                         @DefaultArgument("A-Za-z0-9") @Nonnull String validCharacters) {
        String distinctChars = CHAR_SET_CACHE.computeIfAbsent(validCharacters, this::parseCharacterSet);
        int entropy = minDistinctChars(width, distinctChars);
        Random random = new Random();
        int loop = MAX_ATTEMPTS;                // limit the number of attempts to ensure we return quickly
        boolean done = false;
        String result = "";

        while (!done && (loop-- != 0)) {        // try MAX_ATTEMPT times for a string with at least entropy unique chars
            StringBuilder buffer = new StringBuilder();

            random.ints(width, 0, distinctChars.length())
                .forEach(i -> buffer.append(distinctChars.charAt(i)));

            result = buffer.toString();
            done = result.codePoints().distinct().count() >= entropy;
        }

        return result;
    }


    /**
     * Returns the minimum number of distinct characters we would like to see in a random string.
     * This will be either half the generated characters or half the {@code distinctChars}, whichever is lowest.
     * @param width             length of the random string
     * @param distinctChars     a string of distinct characters that can be in the generated string
     * @return                  the minimum number of unique characters we want in the random string
     */
    @VisibleForTesting
    int minDistinctChars(int width, @Nonnull String distinctChars) {
        int entropy = Math.min(width, distinctChars.length());
        entropy = (entropy + 1) / 2;

        return entropy;
    }


    @Nonnull
    @VisibleForTesting
    String parseCharacterSet(@Nonnull String characterSet) {
        if (characterSet.isEmpty()) {
            throw new IllegalArgumentException("Empty character set");
        }

        CharSetBuilder builder = new CharSetBuilder();
        int index = 0;
        int end = characterSet.length() - 1;
        char last = characterSet.charAt(0);
        boolean lastRange = false;

        while (++index <= end) {
            char current = characterSet.charAt(index);
            boolean isRange = (current == '-') && (index != end);

            if (lastRange && isRange) {
                throw new IllegalArgumentException(
                    "Invalid character set at position " + index + ": '" + characterSet + "'");
            } else if (isRange) {
                current = characterSet.charAt(++index);
                builder.range(last, current);
            } else {
                builder.with(last);
                last = current;
            }

            lastRange = isRange;
        }

        if (!lastRange) {
            builder.with(characterSet.charAt(end));
        }

        return builder.asString();
    }
}
