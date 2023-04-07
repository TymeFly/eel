package com.github.tymefly.eel.exception;

import javax.annotation.Nonnull;

/**
 * Base class for all EEL exceptions.
 */
public abstract sealed class EelException
        extends RuntimeException
        permits EelRuntimeException, EelCompileException, EelInternalException {
    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    EelException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    EelException(@Nonnull String message, @Nonnull Object... args) {
        super(String.format(message, args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    EelException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
