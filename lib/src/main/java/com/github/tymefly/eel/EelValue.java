package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * EEL values accessor.
 */
@Immutable
public non-sealed interface EelValue extends ValueAccessor {
    /** The Eel Value for empty {@link Type#TEXT} */
    EelValue BLANK = of("");

    /** The Eel Value for the {@link Type#LOGIC} value {@literal true} */
    EelValue TRUE = of(true);

    /** The Eel Value for the {@link Type#LOGIC} value {@literal false} */
    EelValue FALSE = of(false);

    /** The Eel Value for the {@link Type#NUMBER} {@literal 0} */
    EelValue ZERO = of(0);

    /** The Eel Value for the {@link Type#NUMBER} {@literal 1} */
    EelValue ONE = of(1);

    /** The Eel Value for the {@link Type#NUMBER} {@literal 10} */
    EelValue TEN = of(10);

    /** The Eel Value for the {@link Type#DATE} {@literal 1970-01-01 00:00:00} */
    EelValue EPOCH_START_UTC = of(EelContext.FALSE_DATE);


    /**
     * Returns an immutable EelValue for the specified String {@code value}.
     * For efficiency EelValues may be pooled and reused.
     * @param value     backing string
     * @return          An EelValue backing by a string
     */
    @Nonnull
    static EelValue of(@Nonnull String value) {
        return Value.of(value);
    }

    /**
     * Returns an immutable EelValue for the specified Number {@code value}.
     * For efficiency EelValues may be pooled and reused.
     * @param value     backing number
     * @return          An EelValue backing by a number
     */
    @Nonnull
    static EelValue of(@Nonnull Number value) {
        return Value.of(value);
    }

    /**
     * Returns an immutable EelValue for the specified boolean {@code value}.
     * For efficiency EelValues may be pooled and reused.
     * @param value     backing boolean
     * @return          An EelValue backing by a boolean
     */
    @Nonnull
    static EelValue of(boolean value) {
        return Value.of(value);
    }

    /**
     * Returns an immutable EelValue for the specified ZonedDateTime {@code value}.
     * For efficiency EelValues may be pooled and reused.
     * @param value     backing ZonedDateTime
     * @return          An EelValue backing by a ZonedDateTime
     */
    @Nonnull
    static EelValue of(@Nonnull ZonedDateTime value) {
        return Value.of(value);
    }
}
