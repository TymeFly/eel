package com.github.tymefly.eel.function.general;

import java.util.concurrent.ThreadLocalRandom;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns random numbers
 */
@PackagedEelFunction
public class Random {
    /**
     * Returns a random number between {@code min} and {@code max}, <b>inclusive</b>.
     * @param min   the minimum value this function will return; defaults to 0
     * @param max   the maximum value this function will return; defaults to 99
     * @return      a random number between {@code min} and {@code max} inclusive
     */
    @EelFunction("random")
    public long random(@DefaultArgument("0") int min, @DefaultArgument("99") int max) {
        return ThreadLocalRandom.current()
            .nextLong(min, max + 1);                    // max is inclusive, but nextInt() upper bound is exclusive
    }
}
