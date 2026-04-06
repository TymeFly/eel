package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
/**
 * Accessor for EEL values. Values use
 * '<a href="https://en.wikipedia.org/wiki/Thunk">Thunk</a>' style semantics for lazy evaluation.
 * As a result, calling methods in this interface may produce side effects such as logging or throwing exceptions.
 * @since 3.0
 */
@Immutable
public sealed interface Value extends ValueReader permits AbstractValue {
    /**
     * The EEL Value representing an empty {@link Type#TEXT}.
     * @since 2.0
     */
    Value BLANK = of("");

    /**
     * The EEL Value representing the {@link Type#LOGIC} value {@literal true}.
     * @since 2.0
     */

    Value TRUE = of(true);

    /**
     * The EEL Value representing the {@link Type#LOGIC} value {@literal false}.
     * @since 2.0
     */
    Value FALSE = of(false);

    /**
     * The EEL Value representing the {@link Type#NUMBER} {@literal 0}.
     * @since 2.0
     */
    Value ZERO = of(0);

    /**
     * The EEL Value representing the {@link Type#NUMBER} {@literal 1}.
     * @since 2.0
     */
    Value ONE = of(1);

    /**
     * The EEL Value representing the {@link Type#NUMBER} {@literal 10}.
     * @since 2.0
     */
    Value TEN = of(10);

    /**
     * The EEL Value representing the {@link Type#DATE} {@literal 1970-01-01 00:00:00 UTC}.
     * @since 2.0
     */
    Value EPOCH_START_UTC = of(EelContext.FALSE_DATE);


    /**
     * Creates an immutable Value from a String.
     * Values may be pooled for efficiency.
     * @param value  the backing string
     * @return       a Value backed by the given string
     */
    @Nonnull
    static Value of(@Nonnull String value) {
        return Constant.of(value);
    }

    /**
     * Creates an immutable Value from a Number.
     * Values may be pooled for efficiency.
     * @param value  the backing number
     * @return       a Value backed by the given number
     */
    @Nonnull
    static Value of(@Nonnull Number value) {
        return Constant.of(value);
    }

    /**
     * Creates an immutable Value from a boolean.
     * Values may be pooled for efficiency.
     * @param value  the backing boolean
     * @return       a Value backed by the given boolean
     */
    @Nonnull
    static Value of(boolean value) {
        return Constant.of(value);
    }

    /**
     * Creates an immutable Value from a ZonedDateTime.
     * Values may be pooled for efficiency.
     * @param value  the backing ZonedDateTime
     * @return       a Value backed by the given ZonedDateTime
     */
    @Nonnull
    static Value of(@Nonnull ZonedDateTime value) {
        return Constant.of(value);
    }


    /**
     * Returns the EEL Value as a {@link File}.
     * @return          the EEL Value as a File
     * @throws IOException if the value represents a sensitive location on the local file system
     * @deprecated       Does not respect custom factory functions set via
     *                   {@link com.github.tymefly.eel.builder.EelContextBuilder#withFileFactory(FileFactory)}
     * @see EelContext#getFile(Value)
     */
    @Deprecated
    @Nonnull
    File asFile() throws IOException;
}