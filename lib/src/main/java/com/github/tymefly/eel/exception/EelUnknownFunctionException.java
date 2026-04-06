package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Exception thrown by the EEL runtime to indicate that an EEL function does not exist.
 * @see com.github.tymefly.eel.builder.EelContextSettingBuilder#withUdfClass(Class)
 * @see com.github.tymefly.eel.builder.EelContextSettingBuilder#withUdfPackage(Package)
 */
public class EelUnknownFunctionException extends EelCompileException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a simple message.
     * @param message  human-readable message
     */
    public EelUnknownFunctionException(@Nonnull String message) {
        super(message);
    }

    /**
     * Constructor for a formatted message.
     * @param message  formatted message string
     * @param args     formatting arguments; the final argument may be a {@link Throwable} cause
     * @see java.util.Formatter
     */
    public EelUnknownFunctionException(@Nonnull String message, @Nonnull Object... args) {
        super(message, args);
    }

    /**
     * Constructor for a wrapped exception.
     * @param message  human-readable message
     * @param cause    wrapped exception
     */
    public EelUnknownFunctionException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }
}
