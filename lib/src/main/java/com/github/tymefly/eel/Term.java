package com.github.tymefly.eel;

import javax.annotation.Nonnull;

/**
 * Part of an {@link Expression}
 */
@FunctionalInterface
interface Term {
    /**
     * Evaluate this term
     * @param symbols   Accessor for values stored in the symbols table
     * @return          the evaluated Value
     */
    @Nonnull
    Value evaluate(@Nonnull SymbolsTable symbols);


    /**
     * Returns {@literal true} only if this Term represents a constant
     * @return {@literal true} only if this Term represents a constant
     */
    default boolean isConstant() {
        return false;
    }
}
