package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Interface used to represent variable based operation in the EEL language. Unlike {@link CompileUnaryOp}
 * the arguments are Strings and are not bound to a specific compiler
 */
@FunctionalInterface
interface CompileVariableOp {
    /**
     * Apply the variable operation
     * @param value     the value of an EEL variable
     * @return the function result
     */
    @Nonnull
    String apply(@Nonnull String value);
}