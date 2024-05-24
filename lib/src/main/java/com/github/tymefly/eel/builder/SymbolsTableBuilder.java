package com.github.tymefly.eel.builder;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Builder class used to generate a SymbolsTable that can return values from multiple anonymous sources.
 * If the key exists in multiple sources then the source that is added to the table first takes priority.
 * Typical usage is:
 * <pre>{@code SymbolsTable.factory()
 *             .withProperties()
 *             .withValues(firstMapOfValues)
 *             .withValues(secondMapOfValues)
 *             .withLookup(firstCallBackFunction)
 *             .withLookup(secondCallBackFunction)
 *             .withDefault("<not set>")
 *             .build();
 * }</pre>
 */
public interface SymbolsTableBuilder extends BasicSymbolTableBuilder {
    /**
     * Adds all the system properties to the generated SymbolsTable.
     * @return      a fluent interface
     * @see System#getProperties()
     */
    @Nonnull
    SymbolsTableBuilder withProperties();

    /**
     * Adds all the environment variables to the generated SymbolsTable.
     * @return      a fluent interface
     * @see System#getenv()
     */
    @Nonnull
    SymbolsTableBuilder withEnvironment();

    /**
     * Adds a map to the generated SymbolsTable. This method may be called multiple times if more than
     * map is required
     * @param values    a collection of key-value pairs that will be added to the generated symbols table
     * @return          a fluent interface
     */
    @Nonnull
    SymbolsTableBuilder withValues(@Nonnull Map<String, String> values);

    /**
     * Adds a callback function from the symbols table that allows the client to read symbols in a flexible way.
     * This method may be called multiple times if more than one lookup function is required.
     * <br>
     * <b>Note:</b> It is required that for any given key the lookup function always returns the same value.
     * @param lookup    A function that is passed the lookup key and returns the associated value or
     *                      {@literal null} of there is no related value.
     * @return          a fluent interface
     */
    @Nonnull
    SymbolsTableBuilder withLookup(@Nonnull Function<String, String> lookup);

    /**
     * Set a default value that is returned when a symbol can not be found in the symbols table.
     * As this always returns the {@code defaultValue} no further strategies can be added to this symbols table
     * @param defaultValue  The default value that is returned when
     * @return              a fluent interface
     */
    @Nonnull
    BasicSymbolTableBuilder withDefault(@Nonnull String defaultValue);
}
