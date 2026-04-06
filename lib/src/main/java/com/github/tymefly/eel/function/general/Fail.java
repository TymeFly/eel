package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFailException;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that fails an expression by throwing an {@link EelFailException}.
 * @since 1.0
 */
@PackagedEelFunction
public class Fail {
    /**
     * An EEL function that fails the expression by throwing an {@link EelFailException}.
     * This function is typically used inside a condition, such as a ternary expression or a lookback. For example:
     * <br>
     * {@code $( condition ? value : fail("error message") ) }
     * @param message   the optional failure message
     * @throws EelFailException when invoked
     * @since 1.0
     */
    @EelFunction("fail")
    public void fail(@Nonnull
                     @DefaultArgument(value = "", description = "Empty text") String message) throws EelFailException {
        throw new EelFailException(message);
    }
}
