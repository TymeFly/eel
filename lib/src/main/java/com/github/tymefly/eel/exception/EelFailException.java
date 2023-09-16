package com.github.tymefly.eel.exception;

import javax.annotation.Nonnull;

/**
 * The exception that will be thrown by the {@code fail} function
 */
public class EelFailException extends EelRuntimeException {
    /**
     * Constructor
     * @param message       Human-readable message
     */
    public EelFailException(@Nonnull String message) {
        super(message);
    }
}