package com.github.tymefly.eel.builder;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Builder class used to generate a flexible SymbolsTable that can return values from multiple named sources.
 * A unique scope name is used to disambiguate keys that exist in multiple sources.
 * Typical usage is:
 * <pre>{@code SymbolsTable.factory(".")
 *             .withProperties("props")
 *             .withEnvironment("env")
 *             .withValues("map1", firstMapOfValues)
 *             .withValues("map2", secondMapOfValues)
 *             .withLookup("func1", firstCallBackFunction)
 *             .withLookup("func1", secondCallBackFunction)
 *             .withDefault("<not set>")
 *             .build();
 * }</pre>
 */
public interface ScopedSymbolsTableBuilder extends BasicSymbolTableBuilder {
    /**
     * Adds all the system properties to the generated SymbolsTable.
     * @param scopeName Name of the scope for the properties
     * @return          a fluent interface
     * @see System#getProperties()
     */
    @Nonnull
    ScopedSymbolsTableBuilder withProperties(@Nonnull String scopeName);

    /**
     * Adds all the environment variables to the generated SymbolsTable.
     * @param scopeName Name of the scope for the environment variables
     * @return          a fluent interface
     * @see System#getenv()
     */
    @Nonnull
    ScopedSymbolsTableBuilder withEnvironment(@Nonnull String scopeName);

    /**
     * Adds a map to the generated SymbolsTable. This method may be called multiple times if more than
     * map is required
     * @param scopeName Name of the scope for the map
     * @param values    a collection of key-value pairs that will be added to the generated symbols table
     * @return          a fluent interface
     */
    @Nonnull
    ScopedSymbolsTableBuilder withValues(@Nonnull String scopeName, @Nonnull Map<String, String> values);

    /**
     * Adds a callback function from the symbols table that allows the client to read symbols in a flexible way.
     * This method may be called multiple times if more than one lookup function is required.
     * <br>
     * <b>Note:</b> It is required that for any given key the lookup function always returns the same value.
     * @param scopeName Name of the scope for lookup function
     * @param lookup    A function that is passed the lookup key and returns the associated value or
     *                      {@literal null} of there is no related value.
     * @return          a fluent interface
     */
    @Nonnull
    ScopedSymbolsTableBuilder withLookup(@Nonnull String scopeName, @Nonnull Function<String, String> lookup);

    /**
     * Set a default value that is returned when a symbol can not be found in the symbols table.
     * As this always returns the {@code defaultValue} no further strategies can be added to this symbols table
     * @param defaultValue  The default value that is returned when
     * @return              a fluent interface
     */
    @Nonnull
    BasicSymbolTableBuilder withDefault(@Nonnull String defaultValue);
}
