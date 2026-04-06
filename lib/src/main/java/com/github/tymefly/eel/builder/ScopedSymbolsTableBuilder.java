package com.github.tymefly.eel.builder;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
/**
 * Builder used to create a flexible {@link com.github.tymefly.eel.SymbolsTable} that can return values from multiple
 * named sources. A unique scope name is used to disambiguate keys that exist in multiple sources.
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
     * Adds all system properties to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * @param scopeName  unique the name of the scope for the properties
     * @return           a fluent interface
     * @see System#getProperties()
     */
    @Nonnull
    ScopedSymbolsTableBuilder withProperties(@Nonnull String scopeName);

    /**
     * Adds all environment variables to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * @param scopeName  unique name of the scope for the environment variables
     * @return           a fluent interface
     * @see System#getenv()
     */
    @Nonnull
    ScopedSymbolsTableBuilder withEnvironment(@Nonnull String scopeName);

    /**
     * Adds a map to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * This method may be called multiple times to include more than one map.
     * @param scopeName  unique name of the scope for the map
     * @param values     the key-value pairs to add to the generated {@link com.github.tymefly.eel.SymbolsTable}
     * @return           a fluent interface
     */
    @Nonnull
    ScopedSymbolsTableBuilder withValues(@Nonnull String scopeName, @Nonnull Map<String, String> values);

    /**
     * Adds a callback function to the {@link com.github.tymefly.eel.SymbolsTable} that enables flexible symbol lookup.
     * This method may be called multiple times to include more than one lookup function.
     * <br>
     * <b>Note:</b> For any given key, the lookup function must always return the same value.
     * @param scopeName  unique name of the scope for the lookup function
     * @param lookup     a function that receives the lookup key and returns the associated value,
     *                   or {@literal null} if no value is available
     * @return           a fluent interface
     * @see Function
     */
    @Nonnull
    ScopedSymbolsTableBuilder withLookup(@Nonnull String scopeName, @Nonnull Function<String, String> lookup);

    /**
     * Sets the value returned when a symbol cannot be found in the {@link com.github.tymefly.eel.SymbolsTable}.
     * As this always returns the {@code defaultValue}, no further strategies can be added.
     * @param defaultValue  unique value returned when a symbol is not found
     * @return              a fluent interface
     */
    @Nonnull
    BasicSymbolTableBuilder withDefault(@Nonnull String defaultValue);
}