package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

/**
 * Allows UDFs access to EEL values
 */
public non-sealed interface EelValue extends ValueAccessor {
    /**
     * Factory method for creating EelValue objects backed a string
     * @param value     backing string
     * @return          An EelValue backing by a string
     */
    @Nonnull
    static EelValue of(@Nonnull String value) {
        return Value.of(value);
    }

    /**
     * Factory method for creating EelValue objects backed a number
     * @param value     backing number
     * @return          An EelValue backing by a number
     */
    @Nonnull
    static EelValue of(@Nonnull Number value) {
        return Value.of(value);
    }

    /**
     * Factory method for creating EelValue objects backed a boolean
     * @param value     backing boolean
     * @return          An EelValue backing by a boolean
     */
    @Nonnull
    static EelValue of(boolean value) {
        return Value.of(value);
    }

    /**
     * Factory method for creating EelValue objects backed a ZonedDateTime
     * @param value     backing ZonedDateTime
     * @return          An EelValue backing by a ZonedDateTime
     */
    @Nonnull
    static EelValue of(@Nonnull ZonedDateTime value) {
        return Value.of(value);
    }
}
