package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Exception thrown to indicate an external function failed or could not be invoked.
 */
public class EelFunctionException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public EelFunctionException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    public EelFunctionException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public EelFunctionException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
