package com.github.tymefly.eel.function.text;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.CharSetBuilder;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * An EEL function that generates random text.
 * @since 3.0
 */
@PackagedEelFunction
public class RandomText {
    private static final int MAX_ATTEMPTS = 10;
    private static final Map<String, String> CHAR_SET_CACHE = new WeakHashMap<>();


    /**
     * Generates random text of the specified length from a set of characters, which may be customised.
     * This function does not guarantee that a character is chosen from each range within {@code validCharacters}
     * or that all characters in the generated text are unique.
     * @param length           the length of the returned text; must be greater than 0
     * @param validCharacters  the characters that may appear in the returned text. Ranges can be specified
     *                         using a dash ({@code -}), for example {@code a-z} or {@code 0-9}. A literal
     *                         dash ({@code -}) can be included in the set of valid characters so long as it is
     *                         either the first or last character specified
     * @return                 random text of the specified length
     * @since 3.0
     */
    @EelFunction("text.random")
    @Nonnull
    public String random(@DefaultArgument("10") int length,
                         @DefaultArgument("A-Za-z0-9") @Nonnull String validCharacters) {
        Preconditions.checkArgument((length >= 0), "invalid text length %d", length);

        String distinctChars = CHAR_SET_CACHE.computeIfAbsent(validCharacters, this::parseCharacterSet);
        int entropy = minDistinctChars(length, distinctChars);
        Random random = new Random();
        int loop = MAX_ATTEMPTS;                // limit the number of attempts to ensure we return quickly
        long maxDistinct = -1;                  // Return the String with the highest number of distinct chars
        boolean done = false;
        String result = "";

        while (!done && (loop-- != 0)) {        // try MAX_ATTEMPT times for a string with at least entropy unique chars
            String current;
            long currentDistinct;
            StringBuilder buffer = new StringBuilder();

            random.ints(length, 0, distinctChars.length())
                .forEach(i -> buffer.append(distinctChars.charAt(i)));

            current = buffer.toString();
            currentDistinct = current.codePoints()
                .distinct()
                .count();

            if (currentDistinct > maxDistinct) {
                maxDistinct = currentDistinct;
                result = current;
                done = (maxDistinct >= entropy);
            }
        }

        return result;
    }


    /**
     * Returns the minimum number of distinct characters we would like to see in a random string.
     * @param width             length of the random string
     * @param distinctChars     a string of distinct characters that can be in the generated string
     * @return                  the minimum number of unique characters we want in the random string
     */
    @VisibleForTesting
    int minDistinctChars(int width, @Nonnull String distinctChars) {
        int entropy = Math.min(width, distinctChars.length());
        entropy = entropy - (entropy + 2 >> 2);

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
        char next = characterSet.charAt(0);
        boolean lastRange = false;

        while (++index <= end) {
            char current = characterSet.charAt(index);
            boolean isRange = (current == '-') && (index != end);

            if (lastRange && isRange) {
                throw new IllegalArgumentException(
                    "Invalid character set at position " + index + ": '" + characterSet + "'");
            } else if (isRange) {
                current = characterSet.charAt(++index);
                builder.range(next, current);
            } else {
                builder.with(next);
                next = current;
            }

            lastRange = isRange;
        }

        if (!lastRange) {
            builder.with(characterSet.charAt(end));
        }

        return builder.asString();
    }
}
