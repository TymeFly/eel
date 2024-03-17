package com.github.tymefly.eel;

import java.math.MathContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.builder.EelContextBuilder;

/**
 * Context in which EEL expressions are compiled and executed.
 */
public interface EelContext {
    /**
     * A date which can be converted to the logic value {@literal false}.
     * Other dates with a {@literal 0} second offset in the current epoch but with a different ZoneOffset can
     * also be converted to the logic value {@literal false}.
     */
    ZonedDateTime FALSE_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC);


    /**
     * The default precision used in calculations on fractional numbers.
     * @see EelContextBuilder#withPrecision(int)
     */
    int DEFAULT_PRECISION = 16;

    /**
     * Entry point for building EEL Context objects
     * @return a factory object that is used to defined EEL Contexts
     */
    @Nonnull
    static EelContextBuilder factory() {
        return new EelContextImpl.Builder();
    }


    /**
     * Returns an object that can reading Eel language metadata
     * @return an object that can reading Eel language metadata
     */
    @Nonnull
    Metadata metadata();

    /**
     * Returns a unique ID for this context object. No particular significant should be assigned to the string
     * returned by this method as it may change in future versions.
     * @return a unique ID for this context object.
     */
    @Nonnull
    String contextId();

    /**
     * Returns returns the time stamp indicating when this context was created. This is always in the UTC Zone
     * @return returns the time stamp indicating when this context was created.
     */
    @Nonnull
    ZonedDateTime getStartTime();

    /**
     * Returns the MathContext used to evaluate maths expressions
     * @return the MathContext used to evaluate maths expressions
     */
    @Nonnull
    MathContext getMathContext();
}
