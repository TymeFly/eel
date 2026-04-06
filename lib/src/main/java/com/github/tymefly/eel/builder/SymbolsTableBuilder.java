package com.github.tymefly.eel.builder;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Builder used to create a {@link com.github.tymefly.eel.SymbolsTable} that can return values from multiple
 * anonymous sources. If a key exists in multiple sources, the first source added takes priority.
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
     * Adds all system properties to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * @return  a fluent interface
     * @see System#getProperties()
     */
    @Nonnull
    SymbolsTableBuilder withProperties();

    /**
     * Adds all environment variables to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * @return  a fluent interface
     * @see System#getenv()
     */
    @Nonnull
    SymbolsTableBuilder withEnvironment();

    /**
     * Adds a map of key-value pairs to the generated {@link com.github.tymefly.eel.SymbolsTable}.
     * This method may be called multiple times to include more than one map.
     * @param values  the key-value pairs to add to the {@link com.github.tymefly.eel.SymbolsTable}
     * @return        a fluent interface
     */
    @Nonnull
    SymbolsTableBuilder withValues(@Nonnull Map<String, String> values);

    /**
     * Adds a callback function to the {@link com.github.tymefly.eel.SymbolsTable} that allows flexible symbol lookup.
     * This method may be called multiple times to include more than one lookup function.
     * <br>
     * <b>Note:</b> For any given key, the lookup function must always return the same value.
     * @param lookup  a function that receives a key and returns the associated value, or
     *                {@literal null} if no value exists
     * @return        a fluent interface
     * @see Function
     */
    @Nonnull
    SymbolsTableBuilder withLookup(@Nonnull Function<String, String> lookup);

    /**
     * Sets the value returned when a symbol cannot be found in the {@link com.github.tymefly.eel.SymbolsTable}.
     * As this always returns the {@code defaultValue}, no further strategies can be added.
     * @param defaultValue  the value returned when a symbol is not found
     * @return              a fluent interface
     */
    @Nonnull
    BasicSymbolTableBuilder withDefault(@Nonnull String defaultValue);
}