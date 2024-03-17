package com.github.tymefly.eel;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelSymbolsTableException;

/**
 * A source of values in the symbols table.
 */
class SymbolsSource {
    private static final Predicate<String> NOT_SCOPED = s -> true;

    private final String prefix;
    private final Predicate<String> scopeCheck;
    private final Function<String, ?> strategy;

    private SymbolsSource(@Nonnull String prefix,
                          @Nonnull Predicate<String> scopeCheck,
                          @Nonnull Function<String, ?> strategy) {
        this.prefix = prefix;
        this.scopeCheck = scopeCheck;
        this.strategy = strategy;
    }


    @Nonnull
    static SymbolsSource unscoped(@Nonnull Function<String, ?> strategy) {
        return new SymbolsSource("", NOT_SCOPED, strategy);
    }


    @Nonnull
    static SymbolsSource scoped(@Nonnull String scopeName,
                         @Nullable String delimiter,
                         @Nonnull Function<String, ?> strategy) {
        String prefix = scopeName + delimiter;

        if ((delimiter == null) || scopeName.contains(delimiter)) {
            throw new EelSymbolsTableException("Scope name '%s' contains delimiter '%s'", scopeName, delimiter);
        }

        return new SymbolsSource(prefix, s -> s.startsWith(prefix), strategy);
    }


    @Nullable
    Object read(@Nonnull String key) {
        Object result;

        if (scopeCheck.test(key)) {
            key = key.substring(prefix.length());

            result = strategy.apply(key);
        } else {
            result = null;
        }

        return result;
    }
}
