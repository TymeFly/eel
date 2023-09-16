package com.github.tymefly.eel.integration;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelRuntimeException;

/**
 * Thrown by {@link EelProperties} to indicate that a property references an invalid value.
 * This is typically the result of an illegal forward reference
 */
public class UnknownEelPropertyException extends EelRuntimeException {
    UnknownEelPropertyException(@Nonnull String key) {
        super("Unknown key '%s'. This could be the result of an illegal forward reference", key);
    }
}
