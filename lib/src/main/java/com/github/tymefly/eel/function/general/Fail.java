package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFailException;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that fails the expression by throwing an {@link EelFailException}.
 * This function would typically be used inside a ternary expressions as
 * <br>
 * {@code $( condition ? value : fail("error message") ) }
 * <br>
 * The EEL syntax for this function is:
 * <ul>
 *     <li><code>fail()</code> - Fail the expression</li>
 *     <li><code>fail( message )</code> - Fail the expression with the specified error message</li>
 * </ul>
 */
@PackagedEelFunction
public class Fail {
    /**
     * Entry point for the {@code fail} function
     * @param message   The optional message that will be passed back to the client application
     * @throws EelFailException always
     */
    @EelFunction("fail")
    public void fail(@Nonnull @DefaultArgument("") String message) throws EelFailException {
        throw new EelFailException(message);
    }
}
