package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * An executable expression that contains one or more code {@link Term}s
 */
@FunctionalInterface
interface Expression {
    /**
     * Evaluate this expression
     * @param symbols   Accessor for values stored in the symbols table
     * @return          the evaluated Value
     */
    @Nonnull
    Result evaluate(@Nonnull SymbolsTable symbols);
}
