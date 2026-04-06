package com.github.tymefly.eel.builder;

import java.io.File;
import java.time.DayOfWeek;
import java.time.Duration;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.FileFactory;


/**
 * Part of the fluent interface for building EEL context objects.
 * @param <T>  the type of the fluent interface
 */
public interface EelContextSettingBuilder<T> {

    /**
     * Sets the maximum length of an EEL expression. If EEL attempts to parse an expression longer
     * than this limit, an {@link com.github.tymefly.eel.EelSourceException} is thrown.
     * The purpose of this setting is to prevent clients from mounting a denial of service (DoS)
     * attack by defining excessively long expressions that consume system resources, for example,
     * by piping a stream of data that never ends.
     * By default, this is {@link EelContextBuilder#DEFAULT_MAX_EXPRESSION_LENGTH}.
     * @param maxLength  the maximum allowed length of an EEL expression
     * @return           a fluent interface
     * @see EelContextBuilder#DEFAULT_MAX_EXPRESSION_LENGTH
     */
    @Nonnull
    T withMaxExpressionSize(int maxLength);

    /**
     * Sets a timeout for evaluating an EEL expression. If this duration is exceeded, an
     * {@link com.github.tymefly.eel.exception.EelTimeoutException} is thrown.
     * The purpose of this setting is to prevent clients from mounting a denial of service (DoS)
     * attack by defining expressions that never terminate or fail to complete within a reasonable
     * time, for example, by calling a UDF that does not terminate or depends on an unresponsive
     * external resource.
     * By default, this is {@link EelContextBuilder#DEFAULT_TIMEOUT}. Expressions using only
     * standard functions should typically complete in under one second, so this setting rarely
     * needs adjustment.
     * @param timeout    the maximum duration allowed to evaluate an expression
     * @return           a fluent interface
     * @see EelContextBuilder#DEFAULT_TIMEOUT
     * @see EelContextBuilder#NO_TIMEOUT
     */
    @Nonnull
    T withTimeout(@Nonnull Duration timeout);

    /**
     * Sets the precision used in calculations involving fractional numbers.
     * By default, this is {@value com.github.tymefly.eel.EelContext#DEFAULT_PRECISION}.
     * @param precision  the precision used for mathematical operations
     * @return           a fluent interface
     * @see com.github.tymefly.eel.EelContext#DEFAULT_PRECISION
     */
    @Nonnull
    T withPrecision(int precision);

    /**
     * Sets the maximum number of bytes that an EEL function can read.
     * By default, this is {@value com.github.tymefly.eel.EelContext#DEFAULT_IO_LIMIT} bytes.
     * @param bytes      the maximum number of bytes that can be read in a single operation
     * @return           a fluent interface
     * @since 3.0
     * @see com.github.tymefly.eel.EelContext#DEFAULT_IO_LIMIT
     */
    @Nonnull
    T withIoLimit(int bytes);

    /**
     * Sets the first day of the week used in date-based calculations.
     * By default, this is {@link DayOfWeek#MONDAY} to align with ISO-8601.
     * @param startOfWeek  the first day of the calendar week
     * @return             a fluent interface
     * @since 3.0
     */
    T withStartOfWeek(@Nonnull DayOfWeek startOfWeek);

    /**
     * Sets the minimum number of days in the first week of the year, from 1 to 7.
     * By default, this is {@literal 4} to align with ISO-8601.
     * @param minimalDaysInFirstWeek  the minimum number of days in the first week of the year
     * @return                       a fluent interface
     * @since 3.0
     */
    @Nonnull
    T withMinimalDaysInFirstWeek(int minimalDaysInFirstWeek);

    /**
     * Allows methods annotated with {@link com.github.tymefly.eel.udf.EelFunction} in the specified
     * class callable by EEL.
     * @param udfClass  the class that provides UDF functions
     * @return          a fluent interface
     * @see com.github.tymefly.eel.udf.EelFunction
     */
    @Nonnull
    T withUdfClass(@Nonnull Class<?> udfClass);

    /**
     * Searches the specified {@code location} for classes annotated with
     * {@link com.github.tymefly.eel.udf.PackagedEelFunction}. For each class found, methods
     * annotated with {@link com.github.tymefly.eel.udf.EelFunction} are made callable by EEL.
     * This method does not search sub-packages.
     * @param location  the package containing classes with callable EEL functions
     * @return          a fluent interface
     * @see com.github.tymefly.eel.udf.PackagedEelFunction
     * @see com.github.tymefly.eel.udf.EelFunction
     */
    @Nonnull
    T withUdfPackage(@Nonnull Package location);

    /**
     * Sets a custom factory function for converting paths into {@link File} objects.
     * Clients may use this to add custom file validation behaviour; however, a custom
     * factory cannot override the built-in validations.
     * @param factory   the custom factory used to convert paths into {@link File} objects
     * @return          a fluent interface
     * @since 3.2
     */
    @Nonnull
    T withFileFactory(@Nonnull FileFactory factory);
}