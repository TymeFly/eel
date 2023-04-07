package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Exception thrown to indicate an Arithmetic operation failed in an external function
 */
public class EelArithmeticException extends EelFunctionException {
    @Serial
    private static final long serialVersionUID = 0x1L;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public EelArithmeticException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    public EelArithmeticException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public EelArithmeticException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
