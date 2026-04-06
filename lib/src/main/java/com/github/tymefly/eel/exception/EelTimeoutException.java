package com.github.tymefly.eel.exception;

import java.io.Serial;
import java.time.Duration;

import javax.annotation.Nonnull;

/**
 * Thrown if the execution time for evaluating an expression exceeds the limit set by
 * {@link com.github.tymefly.eel.builder.EelContextBuilder#withTimeout(Duration)}.
 */
public class EelTimeoutException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a simple message.
     * @param message  human-readable message
     */
    public EelTimeoutException(@Nonnull String message) {
        super(message);
    }

    /**
     * Constructor for a formatted message.
     * @param message  formatted message string
     * @param args     formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    public EelTimeoutException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }

    /**
     * Constructor for a wrapped exception.
     * @param message  human-readable message
     * @param cause    wrapped exception
     */
    public EelTimeoutException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
