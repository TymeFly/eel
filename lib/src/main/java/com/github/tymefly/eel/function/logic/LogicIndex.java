package com.github.tymefly.eel.function.logic;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL function that returns the index of a {@literal true} value within a sequence.
 * @since 3.2
 */
@PackagedEelFunction
public class LogicIndex {
    /**
     * Returns the 1-based index of the first {@literal true} {@code value} in the given sequence.
     * This function can be used with a compound statement to determine the index of a lookback, for example:
     * <br>
     * {@code $( exp1; exp2; exp3; $[logic.index(value1, value2, value3)-NotFound] )}
     * @param values        one or more values to test, in the order they should be evaluated
     * @return              the 1-based index of the first {@literal true} value,
     *                      or {@code -1} if all values are {@literal false}
     * @since 3.2
     */
    @EelFunction("logic.index")
    public int index(@Nonnull Value... values) {
        int found = -1;
        int index = 0;

        for (var value : values) {
            index++;

            if (value.asLogic()) {
                found = index;
                break;
            }
        }

        return found;
    }
}
