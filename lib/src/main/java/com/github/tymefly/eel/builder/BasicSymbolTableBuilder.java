package com.github.tymefly.eel.builder;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.SymbolsTable;

/**
 * Part of the fluent interface for {@link SymbolsTable} builders.
 */
public interface BasicSymbolTableBuilder {
    /**
     * Creates a new {@link SymbolsTable} based on the current builder configuration.
     * <p>
     * This method finalises the builder and returns an immutable {@link SymbolsTable} instance.
     * @return the constructed {@link SymbolsTable} instance
     * @see SymbolsTable
     */
    @Nonnull
    SymbolsTable build();
}