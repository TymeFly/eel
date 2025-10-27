package com.github.tymefly.eel.doc.exception;

import java.io.Serial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Unchecked exception for EelDoc errors.
 */
public class EelDocException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 0x01;


    /**
     * Constructor for a raw message
     * @param message       Human readable (raw) message
     */
    public EelDocException(@Nonnull String message) {
        super(message);
    }


    /**
     * Constructor for a formatted message
     * @param message       formatted message string
     * @param args          formatting arguments
     * @see java.util.Formatter
     */
    public EelDocException(@Nonnull String message, Object... args) {
        super(message.formatted(args), optionalCause(args));
    }


    /**
     * Constructor for a wrapped exception
     * @param message       Human readable (raw) message
     * @param cause         Wrapped exception
     */
    public EelDocException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }



    @Nullable
    private static Throwable optionalCause(Object... args) {
        Throwable cause;

        if (args.length == 0) {
            cause = null;
        } else if (args[args.length - 1] instanceof Throwable throwable) {
            cause = throwable;
        } else {
            cause = null;
        }

        return cause;
    }
}

