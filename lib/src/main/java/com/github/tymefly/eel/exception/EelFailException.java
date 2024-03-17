package com.github.tymefly.eel.exception;

import java.io.Serial;

import javax.annotation.Nonnull;

/**
 * The exception that will be thrown by the {@code fail} function
 */
public class EelFailException extends EelRuntimeException {
    @Serial
    private static final long serialVersionUID = 0x1L;

    /**
     * Constructor
     * @param message       Human-readable message
     */
    public EelFailException(@Nonnull String message) {
        super(message);
    }
}