package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Thrown when the thread executing the EEL expression is interrupted.
 */
public class EelInterruptedException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;


    /**
     * Constructor for a simple message.
     * @param message  human-readable message
     */
    public EelInterruptedException(@Nonnull String message) {
        super(message);
    }

    /**
     * Constructor for a formatted message.
     * @param message  formatted message string
     * @param args     formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    public EelInterruptedException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }

    /**
     * Constructor for a wrapped exception.
     * @param message  human-readable message
     * @param cause    wrapped exception
     */
    public EelInterruptedException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
