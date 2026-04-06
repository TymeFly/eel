package com.github.tymefly.eel.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class for all exceptions thrown by EEL.
 */
public abstract sealed class EelException
        extends RuntimeException
        permits EelRuntimeException, EelCompileException, EelInternalException {
    /**
     * Constructor for a simple message.
     * @param message  human-readable message
     */
    EelException(@Nonnull String message) {
        super(message);
    }

    /**
     * Constructor for a formatted message.
     * @param message  formatted message string
     * @param args     formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    EelException(@Nonnull String message, Object... args) {
        super(message.formatted(args), optionalCause(args));
    }

    /**
     * Constructor for a wrapped exception.
     * @param message  human-readable message
     * @param cause    wrapped exception
     */
    EelException(@Nonnull String message, @Nonnull Throwable cause) {
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
