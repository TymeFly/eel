package com.github.tymefly.eel;

import java.io.Serial;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelCompileException;

/**
 * Unchecked exception thrown if the expression source cannot be read.
 */
public class EelSourceException extends EelCompileException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructs an exception with a raw message.
     * @param message       a human-readable (raw) message
     */
    EelSourceException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructs an exception with a formatted message.
     * @param message       a formatted message string
     * @param args          formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    EelSourceException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }


    /**
     * Constructs an exception that wraps another exception.
     * @param message       a human-readable (raw) message
     * @param cause         the wrapped exception
     */
    EelSourceException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}