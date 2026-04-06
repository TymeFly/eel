package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Thrown to indicate that a source EEL expression contains a semantic error.
 * @since 3.0
 */
public class EelSemanticException extends EelCompileException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a simple message.
     * @param position  position in the EEL expression where the error occurred
     * @param message   human-readable message
     */
    public EelSemanticException(int position, @Nonnull String message) {
        super("Error at position " + position + ": " + message);
    }

    /**
     * Constructor for a formatted message.
     * @param position  position in the EEL expression where the error occurred
     * @param message   formatted message string
     * @param args      formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    public EelSemanticException(int position, @Nonnull String message, @Nonnull Object... args) {
        super("Error at position " + position + ": " + message, args);
    }

    /**
     * Constructor for a wrapped exception.
     * @param position  position in the EEL expression where the error occurred
     * @param message   human-readable message
     * @param cause     wrapped exception
     */
    public EelSemanticException(int position, @Nonnull String message, @Nonnull Throwable cause) {
        super("Error at position " + position + ": " + message, cause);
    }
}
