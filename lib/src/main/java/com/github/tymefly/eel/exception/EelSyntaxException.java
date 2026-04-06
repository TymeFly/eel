package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Thrown to indicate that the source EEL expression contains a syntax error.
 */
public class EelSyntaxException extends EelCompileException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a simple message.
     * @param position  position in the EEL expression where the error occurred
     * @param message   human-readable message
     */
    public EelSyntaxException(int position, @Nonnull String message) {
        super("Error at position " + position + ": " + message);
    }

    /**
     * Constructor for a formatted message.
     * @param position  position in the EEL expression where the error occurred
     * @param message   formatted message string
     * @param args      formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    public EelSyntaxException(int position, @Nonnull String message, @Nonnull Object... args) {
        super("Error at position " + position + ": " + message, args);
    }

    /**
     * Constructor for a wrapped exception.
     * @param position  position in the EEL expression where the error occurred
     * @param message   human-readable message
     * @param cause     wrapped exception
     */
    public EelSyntaxException(int position, @Nonnull String message, @Nonnull Throwable cause) {
        super("Error at position " + position + ": " + message, cause);
    }
}
