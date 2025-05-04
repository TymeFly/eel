package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Interface used to represent binary operations in the EEL language that will be compiled by
 * a {@link Compiler} instance.
 */
@FunctionalInterface
interface CompileBinaryOp {
    /**
     * Compiles the binary operation.
     * @param compiler      The compiler used to compile the operation
     * @param left          The first operand for the operation
     * @param right         The second operand for the operation
     * @return the compiled binary operation
     */
    @Nonnull
    Term apply(@Nonnull Compiler compiler, @Nonnull Term left, @Nonnull Term right);
}