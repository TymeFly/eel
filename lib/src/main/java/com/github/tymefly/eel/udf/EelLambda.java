package com.github.tymefly.eel.udf;

import java.util.function.Supplier;

import com.github.tymefly.eel.EelValue;

/**
 * UDF's can accept EelLambda objects as a parameters. The parameter will only be evaluated if and when the UDF
 * executes this EelLambda's {@link #get()} function.
 * <br>
 * This could be used to pass default values to the UDF that are only evaluate if they are required. The Eel
 * expression could pass:
 * <ul>
 *  <li>A literal constant, which is automatically converted into a EelLambda function that returns that EelValue</li>
 *  <li>A full Eel expression, which EEL will automatically compile into an EelLambda. This could include a
 *      call to {@code count()}, which the UDF could conditionally increment</li>
 *  <li>The {@code fail()} function, so the function will fail if a default value is required</li>
 * </ul>
 */
@FunctionalInterface
public interface EelLambda extends Supplier<EelValue> {
}
