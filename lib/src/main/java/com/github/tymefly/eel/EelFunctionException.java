package com.github.tymefly.eel;

import java.io.Serial;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelRuntimeException;

/**
 * Exception thrown by the EEL runtime to indicate that an EEL function could not be invoked
 * or failed with an exception.
 */
public class EelFunctionException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;


    /**
     * Constructs an exception with a raw message.
     * @param message       a human-readable (raw) message
     */
    EelFunctionException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructs an exception with a formatted message.
     * @param message       a formatted message string
     * @param args          formatting arguments. The final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    EelFunctionException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }


    /**
     * Constructs an exception that wraps another exception.
     * @param message       a human-readable (raw) message
     * @param cause         the wrapped exception
     */
    EelFunctionException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}