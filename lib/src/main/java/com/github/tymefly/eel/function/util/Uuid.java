package com.github.tymefly.eel.function.util;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns a new UUID
 * <br>
 * The EEL syntax for this function is <code>uuid()</code>
 */
@PackagedEelFunction
public class Uuid {
    /**
     * Entry point for the {@code guid} function
     * @return a new random UUID
     */
    @EelFunction(name = "uuid")
    @Nonnull
    public String uuid() {
        return UUID.randomUUID().toString();
    }
}
