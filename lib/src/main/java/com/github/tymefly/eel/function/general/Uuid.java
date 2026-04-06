package com.github.tymefly.eel.function.general;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions for generating and handling universally unique identifiers (UUIDs).
 * @since 1.0
 */
@PackagedEelFunction
public class Uuid {
    /**
     * Returns a new pseudo-randomly generated UUID using a cryptographically strong
     * pseudo-random number generator.
     * @return  a newly generated pseudo-random UUID
     * @since 1.0
     */
    @EelFunction("uuid")
    @Nonnull
    public String uuid() {
        return UUID.randomUUID().toString();
    }
}
