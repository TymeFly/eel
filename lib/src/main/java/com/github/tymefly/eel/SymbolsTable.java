package com.github.tymefly.eel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.builder.ScopedSymbolsTableBuilder;
import com.github.tymefly.eel.builder.SymbolsTableBuilder;
import com.github.tymefly.eel.exception.EelSymbolsTableException;
import com.github.tymefly.eel.validate.Preconditions;
/**
 * The {@link SymbolsTable} is a look-up mechanism that allows a compiled {@link Eel} to read external values.
 * A {@link SymbolsTable} can be as simple as a lookup from a map (see {@link #from(Map)}), a callback
 * function (see {@link #from(Function)}), or a fully fledged lookup strategy that can read values from multiple
 * sources (see {@link #factory()}) with priorities and defaults.
 */
public class SymbolsTable {
    private static class Builder implements SymbolsTableBuilder, ScopedSymbolsTableBuilder {
        private final String delimiter;
        private final List<SymbolsSource> sources;
        private final Set<String> scopeNames = new HashSet<>();


        private Builder(@Nullable String delimiter) {
            this.delimiter = delimiter;
            this.sources = new ArrayList<>();
        }

        @Nonnull
        @Override
        public SymbolsTableBuilder withProperties() {
            return unscoped(System.getProperties()::get);
        }

        @Nonnull
        @Override
        public ScopedSymbolsTableBuilder withProperties(@Nonnull String scopeName) {
            return addScoped(scopeName, System.getProperties()::get);
        }

        @Nonnull
        @Override
        public SymbolsTableBuilder withEnvironment() {
            return unscoped(System.getenv()::get);
        }

        @Nonnull
        @Override
        public ScopedSymbolsTableBuilder withEnvironment(@Nonnull String scopeName) {
            return addScoped(scopeName, System.getenv()::get);
        }

        @Nonnull
        @Override
        public SymbolsTableBuilder withValues(@Nonnull Map<String, String> values) {
            Preconditions.checkNotNull(values, "Can not evaluate a null SymbolsTable map");

            Map<String, String> copy = new HashMap<>(values);

            return unscoped(copy::get);
        }

        @Nonnull
        @Override
        public ScopedSymbolsTableBuilder withValues(@Nonnull String scopeName, @Nonnull Map<String, String> values) {
            Preconditions.checkNotNull(values, "Can not evaluate a null SymbolsTable map");

            Map<String, String> copy = new HashMap<>(values);

            return addScoped(scopeName, copy::get);
        }

        @Nonnull
        @Override
        public SymbolsTableBuilder withLookup(@Nonnull Function<String, String> lookup) {
            Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

            return unscoped(lookup);
        }

        @Nonnull
        @Override
        public ScopedSymbolsTableBuilder withLookup(@Nonnull String scopeName,
                                                    @Nonnull Function<String, String> lookup) {
            Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

            return addScoped(scopeName, lookup);
        }

        @Nonnull
        @Override
        public SymbolsTableBuilder withDefault(@Nonnull String defaultValue) {
            Preconditions.checkNotNull(defaultValue, "Can not evaluate a null symbols default value");

            sources.add(SymbolsSource.unscoped(k -> defaultValue));

            return this;
        }

        @Nonnull
        private SymbolsTableBuilder unscoped(@Nonnull Function<String, ?> strategy) {
            SymbolsSource symbolsSource = SymbolsSource.unscoped(strategy);

            sources.add(symbolsSource);

            return this;
        }

        @Nonnull
        private ScopedSymbolsTableBuilder addScoped(@Nonnull String scopeName, @Nonnull Function<String, ?> strategy) {
            boolean valid = scopeNames.add(scopeName);

            if (!valid) {
                throw new EelSymbolsTableException("Duplicate scope '%s'", scopeName);
            }

            SymbolsSource symbolsSource = SymbolsSource.scoped(scopeName, delimiter, strategy);

            sources.add(symbolsSource);

            return this;
        }

        @Nonnull
        @Override
        public SymbolsTable build() {
            return new SymbolsTable(sources);
        }
    }

    /** Default delimiter used by the convenience factory methods. */
    public static final String DEFAULT_DELIMITER = ".";

    /** A {@link SymbolsTable} that contains no data. */
    static final SymbolsTable EMPTY = new SymbolsTable(Collections.emptyList());

    private final List<SymbolsSource> sources;

    private SymbolsTable(@Nonnull List<SymbolsSource> sources) {
        this.sources = new ArrayList<>(sources);
    }

    /**
     * Returns a builder that can create flexible {@link SymbolsTable} objects capable of reading symbols
     * from multiple locations.
     * @return a builder for a {@link SymbolsTable}
     */
    @Nonnull
    public static SymbolsTableBuilder factory() {
        return new Builder(null);
    }

    /**
     * Returns a builder that can create flexible {@link SymbolsTable} objects capable of reading symbols
     * from multiple locations.
     * @param delimiter     Characters that separate the scope name from the key name
     * @return              A builder for a {@link SymbolsTable}
     */
    @Nonnull
    public static ScopedSymbolsTableBuilder factory(@Nonnull String delimiter) {
        Preconditions.checkArgument(!delimiter.isBlank(), "Scope delimiters can not be blank strings");

        return new Builder(delimiter);
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing only the {@code values} in
     * the map without a scope name. Equivalent to:
     * <pre>{@code SymbolsTable.factory()
     *             .withValues(values)
     *             .build();
     * }</pre>
     * @param values    A collection of key-value pairs for the {@link SymbolsTable}
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #from(String, Map)
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull Map<String, String> values) {
        Preconditions.checkNotNull(values, "Can not evaluate a null SymbolsTable map");

        Map<String, String> copy = new HashMap<>(values);

        return new SymbolsTable(List.of(SymbolsSource.unscoped(copy::get)));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing only the {@code values} in
     * the map with {@code scopeName} and the {@link #DEFAULT_DELIMITER}. Equivalent to:
     * <pre>{@code SymbolsTable.factory(DEFAULT_DELIMITER)
     *             .withValues(scopeName, values)
     *             .build();
     * }</pre>
     * @param scopeName Name of the scope for the map
     * @param values    A collection of key-value pairs for the {@link SymbolsTable}
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #from(Map)
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull String scopeName, @Nonnull Map<String, String> values) {
        Preconditions.checkNotNull(values, "Can not evaluate a null SymbolsTable map");

        Map<String, String> copy = new HashMap<>(values);

        return new SymbolsTable(List.of(SymbolsSource.scoped(scopeName, DEFAULT_DELIMITER, copy::get)));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} which delegates to a lookup function
     * without a scope name. Equivalent to:
     * <pre>{@code SymbolsTable.factory()
     *     .withLookup(lookup)
     *     .build();
     * }</pre>
     * @param lookup    A lookup function
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #from(String, Function)
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull Function<String, String> lookup) {
        Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

        return new SymbolsTable(List.of(SymbolsSource.unscoped(lookup)));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} which delegates to a lookup function
     * with {@code scopeName} and the {@link #DEFAULT_DELIMITER}. Equivalent to:
     * <pre>{@code SymbolsTable.factory(DEFAULT_DELIMITER)
     *     .withLookup(scopeName, lookup)
     *     .build();
     * }</pre>
     * @param scopeName Name of the scope for the lookup function
     * @param lookup    A lookup function
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #from(Function)
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull String scopeName, @Nonnull Function<String, String> lookup) {
        Preconditions.checkNotNull(lookup, "Can not evaluate a null symbols lookup");

        return new SymbolsTable(List.of(SymbolsSource.scoped(scopeName, DEFAULT_DELIMITER, lookup)));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing the environment variables
     * without a scope name. Equivalent to:
     * <pre>{@code SymbolsTable.factory()
     *     .withEnvironment()
     *     .build();
     * }</pre>
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #fromEnvironment(String)
     */
    @Nonnull
    public static SymbolsTable fromEnvironment() {
        return from(System.getenv()::get);
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing the environment variables
     * with {@code scopeName} and the {@link #DEFAULT_DELIMITER}. Equivalent to:
     * <pre>{@code SymbolsTable.factory(DEFAULT_DELIMITER)
     *     .withEnvironment(scopeName)
     *     .build();
     * }</pre>
     * @param scopeName Name of the scope for the environment variables
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #fromEnvironment()
     */
    @Nonnull
    public static SymbolsTable fromEnvironment(@Nonnull String scopeName) {
        return from(scopeName, System.getenv()::get);
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing JVM system properties
     * without a scope name. Equivalent to:
     * <pre>{@code SymbolsTable.factory()
     *     .withProperties()
     *     .build();
     * }</pre>
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #fromProperties(String)
     */
    @Nonnull
    public static SymbolsTable fromProperties() {
        return from(k -> (String) System.getProperties().get(k));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing JVM system properties
     * with {@code scopeName} and the {@link #DEFAULT_DELIMITER}. Equivalent to:
     * <pre>{@code SymbolsTable.factory(DEFAULT_DELIMITER)
     *     .withProperties(scopeName)
     *     .build();
     * }</pre>
     * @param scopeName Name of the scope for the properties
     * @return          A new {@link SymbolsTable} object
     * @see #factory()
     * @see #fromProperties(String)
     */
    @Nonnull
    public static SymbolsTable fromProperties(@Nonnull String scopeName) {
        return from(scopeName, k -> (String) System.getProperties().get(k));
    }

    /**
     * A convenience factory method that returns a new {@link SymbolsTable} containing a single hardcoded
     * {@code defaultValue} that is always returned. Equivalent to:
     * <pre>{@code SymbolsTable.factory()
     *     .withDefault(defaultValue)
     *     .build();
     * }</pre>
     * @param defaultValue  The default value that is always applied
     * @return              A new {@link SymbolsTable} object
     * @see #factory()
     */
    @Nonnull
    public static SymbolsTable from(@Nonnull String defaultValue) {
        return from(k -> defaultValue);
    }

    @Nullable
    String read(@Nonnull String key) {
        Object value = null;
        int size = sources.size();
        int index = -1;

        while ((value == null) && (++index != size)) {
            value = sources.get(index)
                .read(key);
        }

        return (value != null ? value.toString() : null);
    }
}