package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * Exception thrown by the {@code fail} function.
 */
public class EelFailException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor for a simple message.
     * @param message  human-readable message
     */
    public EelFailException(@Nonnull String message) {
        super(message);
    }
}