package com.github.tymefly.eel.builder;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.SymbolsTable;

/**
 * Part of the fluent interface for a {@link SymbolsTable} Builder.
 */
public interface BasicSymbolTableBuilder {
    /**
     * Returns a new SymbolsTable as configured by the implementation of this interface
     * @return a new SymbolsTable
     */
    @Nonnull
    SymbolsTable build();
}
