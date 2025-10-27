package com.github.tymefly.eel.function.general;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL functions for universally unique identifiers (UUIDs)
 */
@PackagedEelFunction
public class Uuid {
    /**
     * Returns a new pseudo-randomly generated UUID, created using a cryptographically strong
     * pseudo-random number generator.
     * @return  a new pseudo-randomly generated UUID
     */
    @EelFunction("uuid")
    @Nonnull
    public String uuid() {
        return UUID.randomUUID().toString();
    }
}
