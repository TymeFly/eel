package com.github.tymefly.eel;

import java.io.File;
import java.math.MathContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.builder.EelContextBuilder;

/**
 * Context in which EEL expressions are compiled and executed.
 * Many of the values returned by this class are configured via {@link EelContextBuilder}.
 */
public sealed interface EelContext permits EelContextImpl {
    /** Date in UTC that corresponds to the EEL numeric value {@literal 0}. */
    ZonedDateTime ZERO_DATE = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC);

    /**
     * A date that can be converted to the logical value {@literal false}.
     * Other dates with a {@literal 0} second offset in the current epoch, but with a different {@link ZoneOffset},
     * can also be converted to the logical value {@literal false}.
     */
    ZonedDateTime FALSE_DATE = ZERO_DATE;

    /**
     * The default precision used in calculations involving fractional numbers.
     * @see EelContextBuilder#withPrecision(int)
     */
    int DEFAULT_PRECISION = 16;

    /**
     * The default maximum number of bytes that EEL can read in a single operation.
     * @see EelContextBuilder#withIoLimit(int)
     */
    int DEFAULT_IO_LIMIT = 32768;

    /**
     * Entry point for building EEL context objects.
     * @return a factory used to define EEL contexts
     */
    @Nonnull
    static EelContextBuilder factory() {
        return new EelContextImpl.Builder();
    }

    /**
     * Returns an object for reading EEL language metadata.
     * @return an object for reading EEL language metadata
     * @since 2.0
     */
    @Nonnull
    Metadata metadata();

    /**
     * Returns a unique identifier for this context.
     * No particular significance should be assigned to the returned value, as it may change in future versions.
     * @return a unique identifier for this context
     */
    @Nonnull
    String contextId();

    /**
     * Returns the {@link MathContext} used to evaluate mathematical expressions.
     * @return the {@link MathContext} used for evaluation
     */
    @Nonnull
    MathContext getMathContext();

    /**
     * Returns the timestamp indicating when this context was created.
     * The value is always in the UTC {@link ZoneOffset}.
     * @return the timestamp at which this context was created
     */
    @Nonnull
    ZonedDateTime getStartTime();

    /**
     * Returns the maximum number of bytes that an EEL function can read.
     * @return the maximum number of bytes that an EEL function can read
     */
    int getIoLimit();

    /**
     * Returns the definition of a week.
     * @return the {@link WeekFields} defining the week
     * @since 3.0
     */
    @Nonnull
    WeekFields getWeek();

    /**
     * Builds a {@link File} that is guaranteed not to access a sensitive part of the local file system.
     * @param path  a pathname value
     * @return      a {@link File} for the supplied path
     * @throws EelFunctionException if the {@code path} accesses a sensitive part of the file system
     * @see EelContextBuilder#withFileFactory(FileFactory)
     * @since 3.2
     */
    @Nonnull
    File getFile(@Nonnull Value path) throws EelFunctionException;

    /**
     * Builds a {@link File} that is guaranteed not to access a sensitive part of the local file system.
     * @param path  a pathname string
     * @return      a {@link File} for the supplied path
     * @throws EelFunctionException if the {@code path} accesses a sensitive part of the file system
     * @see EelContextBuilder#withFileFactory(FileFactory)
     * @since 3.2
     */
    @Nonnull
    File getFile(@Nonnull String path) throws EelFunctionException;
}