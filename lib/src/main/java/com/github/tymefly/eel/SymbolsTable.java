package com.github.tymefly.eel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.builder.SymbolTableBuilder;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * The symbols table is a look-up mechanism which allows a compiled {@link Eel} to read external values.
 * The symbols table can be something as simple as a lookup from a map (see {@link #from(Map)}, a callback
 * function (see {@link #from(Function)}, or a fully fledged lookup strategy that can read values from multiple
 * sources (see {@link #factory()} with priorities and defaults.
 */
public class SymbolsTable {
    private static class Builder implements SymbolTableBuilder {
        private final List<Function<String, ?>> strategy;

        Builder() {
            strategy = new ArrayList<>();
        }

        @Nonnull
        @Override
        public SymbolTableBuilder withProperties() {
            strategy.add(System.getProperties()::get);

            return this;
        }

        @Nonnull
        @Override
        public SymbolTableBuilder withEnvironment() {
            strategy.add(System.getenv()::get);

            return this;
        }

        @Nonnull
        @Override
        public SymbolTableBuilder withValues(@Nonnull Map<String, String> values) {
            Preconditions.checkNotNull(values, "Can not evaluate a null symbols table map");

            Map<String, String> copy = new HashMap<>(values);

            strategy.add(copy::get);

            return this;
        }

        @Nonnull
        @Override
        public SymbolTableBuilder withLookup(@Nonnull Function<String, String> lookup) {
            Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

            strategy.add(lookup);

            return this;
        }

        @Nonnull
        @Override
        public SymbolTableBuilder withDefault(@Nonnull String defaultValue) {
            Preconditions.checkNotNull(defaultValue, "Can not evaluate a null symbols default value");

            strategy.add(k -> defaultValue);

            return this;
        }

        @Nonnull
        @Override
        public SymbolsTable build() {
            return new SymbolsTable(strategy);
        }
    }


    static final SymbolsTable EMPTY = new SymbolsTable(Collections.emptyList());

    private final List<Function<String, ?>> strategy;


    private SymbolsTable(@Nonnull List<Function<String, ?>> strategy) {
        this.strategy = new ArrayList<>(strategy);
    }


    /**
     * Returns a builder that can create flexible SymbolsTable which can read symbols from a multiple locations.
     * @return a builder for a SymbolsTable.
     */
    @Nonnull
    public static SymbolTableBuilder factory() {
        return new Builder();
    }

    /**
     * A convenience factory method that returns a new SymbolsTable that only contains the {@code values} in the map.
     * This is the equivalent of
     * <pre>{@code SymbolsTable.factory()
     *             .withValues(values)
     *             .build();
     * }</pre>
     * @param values    A collection of key-value pairs that will be used as the symbols table
     * @return          A new SymbolsTable object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull Map<String, String> values) {
        Preconditions.checkNotNull(values, "Can not evaluate a null symbols table map");

        Map<String, String> copy = new HashMap<>(values);

        return new SymbolsTable(List.of(copy::get));
    }

    /**
     * A convenience factory method that returns a new SymbolsTable which only delegates to a lookup function.
     * This is the equivalent of
     * <pre>{@code SymbolsTable.factory()
     *     .withLookup(lookup)
     *     .build();
     * }</pre>
     * @param lookup    A lookup function
     * @return          A new SymbolsTable object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull Function<String, String> lookup) {
        Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

        return new SymbolsTable(List.of(lookup));
    }

    /**
     * A convenience factory method that returns a new SymbolsTable which only contains the Environment Variables
     * This is the equivalent of
     * <pre>{@code SymbolsTable.factory()
     *     .withEnvironment()
     *     .build();
     * }</pre>
     * @return          A new SymbolsTable object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable fromEnvironment() {
        return from(System.getenv()::get);
    }

    /**
     * A convenience factory method that returns a new SymbolsTable which only contains the JVM Properties
     * This is the equivalent of
     * <pre>{@code SymbolsTable.factory()
     *     .withProperties()
     *     .build();
     * }</pre>
     * @return          A new SymbolsTable object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable fromProperties() {
        return from(k -> (String) System.getProperties().get(k));
    }


    /**
     * A convenience factory method that returns a new SymbolsTable which only contains a single hardcoded String
     * that is always returned. This is the equivalent of
     * <pre>{@code SymbolsTable.factory()
     *     .withDefault(string)
     *     .build();
     * }</pre>
     * @param defaultValue  The default value that is always applied
     * @return          A new SymbolsTable object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull String defaultValue) {
        return from(k -> defaultValue);
    }


    @Nullable
    String read(@Nonnull String key) {
        Object value = null;
        int size = strategy.size();
        int index = -1;

        while ((value == null) && (++index != size)) {
            value = strategy.get(index)
                .apply(key);
        }

        return (value != null ? value.toString() : null);
    }
}
