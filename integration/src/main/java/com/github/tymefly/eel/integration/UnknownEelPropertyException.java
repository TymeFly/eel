package com.github.tymefly.eel.integration;

import java.io.Serial;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelRuntimeException;

/**
 * Thrown by {@link EelProperties} to indicate that a property references an invalid value.
 * This is typically the result of an illegal forward reference
 */
public class UnknownEelPropertyException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor
     * @param key       Key that was not found in the Symbols Table
     */
    UnknownEelPropertyException(@Nonnull String key) {
        super("Unknown key '%s'. This could be the result of an illegal forward reference", key);
    }
}
