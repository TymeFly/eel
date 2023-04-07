package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * An executable part of an Expression
 */
@FunctionalInterface
interface Executor {
    /**
     * Execute this part of the expression
     * @param symbols   Accessor for values stored in the symbols table
     * @return          the value represented by this function
     */
    @Nonnull
    Value execute(@Nonnull SymbolsTable symbols);
}
