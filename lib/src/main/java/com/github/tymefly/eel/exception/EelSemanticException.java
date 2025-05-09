package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Thrown to indicate the source EEL expression has a semantic error
 * @since 3.0
 */
public class EelSemanticException extends EelCompileException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a raw message
     * @param position      position in the EEL expression the syntax occurred
     * @param message       Human readable (raw) message
     */
    public EelSemanticException(int position, @Nonnull String message) {
        super("Error at position " + position + ": " + message);
    }

    /**
     * Constructor for a formatted message
     * @param position      position in the EEL expression the syntax occurred
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    public EelSemanticException(int position, @Nonnull String message, @Nonnull Object... args) {
        super("Error at position " + position + ": " + message, args);
    }


    /**
     * Constructor for a wrapped exception
     * @param position      position in the EEL expression the syntax occurred
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public EelSemanticException(int position, @Nonnull String message, @Nonnull Throwable cause) {
        super("Error at position " + position + ": " + message, cause);
    }
}
