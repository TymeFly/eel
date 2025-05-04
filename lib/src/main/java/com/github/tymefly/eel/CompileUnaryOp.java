package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Interface used to represent unary operations in the EEL language that will be compiled by
 * a {@link Compiler} instance.
 */
@FunctionalInterface
interface CompileUnaryOp {
    /**
     * Compiles the unary operation.
     * @param compiler      The compiler used to compile the operation
     * @param operand       The operand for the operation
     * @return the compiled unary operation
     */
    @Nonnull
    Term apply(@Nonnull Compiler compiler, @Nonnull Term operand);
}