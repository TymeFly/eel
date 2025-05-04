package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Values accessor. Values use '<a href="https://en.wikipedia.org/wiki/Thunk">Thunk</a>' style semantics for
 * lazy evaluation. As a result calling methods in this interface may cause side effects such as writing to
 * the logs or throwing exceptions.
 * @since 3.0.0
 */
@Immutable
public sealed interface Value extends ValueReader permits AbstractValue {
    /**
     * The Eel Value for empty {@link Type#TEXT}
     * @since 2.0.0
     */
    Value BLANK = of("");

    /**
     * The Eel Value for the {@link Type#LOGIC} value {@literal true}
     * @since 2.0.0
     */
    Value TRUE = of(true);

    /**
     * The Eel Value for the {@link Type#LOGIC} value {@literal false}
     * @since 2.0.0
     */
    Value FALSE = of(false);

    /**
     * The Eel Value for the {@link Type#NUMBER} {@literal 0}
     * @since 2.0.0
     */
    Value ZERO = of(0);

    /**
     * The Eel Value for the {@link Type#NUMBER} {@literal 1}
     * @since 2.0.0
     */
    Value ONE = of(1);

    /**
     * The Eel Value for the {@link Type#NUMBER} {@literal 10}
     * @since 2.0.0
     */
    Value TEN = of(10);

    /**
     * The Eel Value for the {@link Type#DATE} {@literal 1970-01-01 00:00:00}
     * @since 2.0.0
     */
    Value EPOCH_START_UTC = of(EelContext.FALSE_DATE);


    /**
     * Factory method that returns an immutable Value for the String {@code value}.
     * For efficiency Values may be pooled and reused.
     * @param value     backing string
     * @return          A Value backed by a string
     */
    @Nonnull
    static Value of(@Nonnull String value) {
        return Constant.of(value);
    }

    /**
     * Factory method that returns an immutable Value for the Number {@code value}.
     * For efficiency Values may be pooled and reused.
     * @param value     backing number
     * @return          A Value backed by a number
     */
    @Nonnull
    static Value of(@Nonnull Number value) {
        return Constant.of(value);
    }

    /**
     * Factory method that returns an immutable Value for the boolean {@code value}.
     * For efficiency Values may be pooled and reused.
     * @param value     backing boolean
     * @return          A Value backed by a boolean
     */
    @Nonnull
    static Value of(boolean value) {
        return Constant.of(value);
    }

    /**
     * Factory method that returns an immutable Value for the ZonedDateTime {@code value}.
     * For efficiency Values may be pooled and reused.
     * @param value     backing ZonedDateTime
     * @return          A Value backed by a ZonedDateTime
     */
    @Nonnull
    static Value of(@Nonnull ZonedDateTime value) {
        return Constant.of(value);
    }


    /**
     * Return the EEL Value as a {@code File}.
     * @return the EEL Value as a File
     * @throws IOException if the value represents a sensitive location on the local file system
     */
    @Nonnull
    File asFile() throws IOException;
}
