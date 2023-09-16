package com.github.tymefly.eel;

import java.io.Serial;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelRuntimeException;

/**
 * Exception thrown by EEL runtime to indicate an EEL function could not be invoked or failed with an Exception
 */
public class EelFunctionException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    EelFunctionException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    EelFunctionException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    EelFunctionException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
