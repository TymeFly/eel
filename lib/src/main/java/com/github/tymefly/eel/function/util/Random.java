package com.github.tymefly.eel.function.util;

import java.util.concurrent.ThreadLocalRandom;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns random numbers
 * <br>
 * The EEL syntax for this function is:
 * <ul>
 *     <li><code>random()</code> -
 *              return a random number in the range {@literal 0} to {@literal 99} inclusive</li>
 *     <li><code>random( minValue )</code> -
 *              return a random number in the range {@literal minValue} to {@literal 99} inclusive</li>
 *     <li><code>random( minValue, maxValue )</code> -
 *              return a random number in the range {@literal minValue} to {@literal maxValue} inclusive</li>
 * </ul>
 */
@PackagedEelFunction
public class Random {
    /**
     * Entry point for the {@code random} function
     * @param min   minimum value this function will return. This defaults to 0
     * @param max   maximum value this function will return. This defaults to 99
     * @return a random number between {@code min} and {@code max}
     */
    @EelFunction(name = "random")
    public long random(@DefaultArgument("0") int min, @DefaultArgument("99") int max) {
        return ThreadLocalRandom.current()
            .nextLong(min, max + 1);                    // max is inclusive, but nextInt() upper bound is exclusive
    }
}
