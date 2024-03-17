package com.github.tymefly.eel.function.number;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that return mathematical constant.
 */
@PackagedEelFunction
public class Constants {
    private static final int SPEED_OF_LIGHT = 299_792_458;

    /**
     * Entry point for the {@code pi} function
     * <br>
     * The EEL syntax for this function is <code>number.pi()</code>
     * @return the value for {@code pi}
     */
    @EelFunction(name = "number.pi")
    public double pi() {
        return Math.PI;
    }

    /**
     * Entry point for the {@code e} function
     * <br>
     * The EEL syntax for this function is <code>number.e()</code>
     * @return the value for {@code e}
     */
    @EelFunction(name = "number.e")
    public double e() {
        return Math.E;
    }

    /**
     * Entry point for the {@code c} function, which returns the speed of light in meters/second
     * <br>
     * The EEL syntax for this function is <code>number.c()</code>
     * @return the value for {@code c}
     */
    @EelFunction(name = "number.c")
    public long c() {
        return SPEED_OF_LIGHT;
    }
}
