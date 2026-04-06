package com.github.tymefly.eel.function.text;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns the index of a specific value within a sequence of values.
 * @since 3.2
 */
@PackagedEelFunction
public class TextIndex {
    /**
     * Returns the 1-based index of the first {@link Value} whose text representation matches the {@code find} text.
     * This function can be used with a compound statement to determine the index of a lookback, for example:
     * <br>
     * {@code $( exp1; exp2; exp3; $[text.index('search', value1, value2, value3)-NotFound] )}
     * @param find          the value to search for
     * @param values        one or more values to test, in the order they should be evaluated
     * @return              the 1-based index of the first matching value, or {@code -1} if no match is found
     * @since 3.2
     */
    @EelFunction("text.index")
    public int index(@Nonnull String find, @Nonnull Value... values) {
        int found = -1;
        int index = 0;

        for (var value : values) {
            index++;

            if (value.asText().equals(find)) {
                found = index;
                break;
            }
        }

        return found;
    }
}
