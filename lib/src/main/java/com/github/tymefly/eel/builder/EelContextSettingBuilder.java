package com.github.tymefly.eel.builder;

import java.time.DayOfWeek;
import java.time.Duration;

import javax.annotation.Nonnull;


/**
 * Part of the fluent interface for building EEL Context objects
 * @param <T> type of the fluent interface
 */
public interface EelContextSettingBuilder<T> {
    /** 
     * This is the default maximum length of an EEL Expression
     * @see #withMaxExpressionSize(int)
     */
    int DEFAULT_MAX_EXPRESSION_LENGTH = 1024;

    /**
     * This is the default maximum length of time an EEL Expression can take to evaluate
     * @see #withTimeout(Duration)
     */
    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    /**
     * This is the default maximum length of time an EEL Expression can take to evaluate
     * @see #withTimeout(Duration)
     */
    Duration NO_TIMEOUT = Duration.ofSeconds(0);

    /**
     * Sets the maximum maxLength of an EEL expression. If EEL tries to parse an expression that is longer than
     * this limit then an {@link com.github.tymefly.eel.EelSourceException} will be thrown.
     * The purpose of this setting is to prevent the client mounting a Denial Of Service (DOS) attack by defining an
     * expression that is so long enough that it will consume all the resources. For example, by piping a stream
     * of data that never ends.
     * By default, this is {@link #DEFAULT_MAX_EXPRESSION_LENGTH}
     * @param maxLength maximum maxLength of an EEL expression.
     * @return          a fluent interface
     */
    @Nonnull
    T withMaxExpressionSize(int maxLength);

    /**
     * Sets a timeout for evaluating an EEL expression. If this is exceeded then an
     * {@link com.github.tymefly.eel.exception.EelTimeoutException} will be thrown.
     * The purpose of this setting is to prevent the client mounting a Denial Of Service (DOS) attack by defining an
     * expression that never terminates or fails to terminate in a reasonable time. For example, by calling a UDF
     * that never terminates or depends on an external resource that has locked up.
     * By default, this is {@link #DEFAULT_TIMEOUT}. EEL expression that used only the Standard Functions
     * should complete less than a second, so this setting should rarely need to be changed.
     * @param timeout   maximum time taken to evaluate an expression.
     *                  A duration of {@link #NO_TIMEOUT} will disable timeouts.
     * @return          a fluent interface
     */
    @Nonnull
    T withTimeout(@Nonnull Duration timeout);

    /**
     * Sets the precision used in calculations on fractional numbers.
     * By default, this is {@link com.github.tymefly.eel.EelContext#DEFAULT_PRECISION}
     * @param precision precision used for maths operations
     * @return          a fluent interface
     */
    @Nonnull
    T withPrecision(int precision);

    /**
     * Sets the maximum number of bytes that an EEL function can read.
     * By default, this is {@link com.github.tymefly.eel.EelContext#DEFAULT_IO_LIMIT} bytes
     * @param bytes     maximum number of bytes that can be read by a single operation
     * @return          a fluent interface
     * @since 3.0.0
     */
    @Nonnull
    T withIoLimit(int bytes);

    /**
     * Sets the first day of the week, which is used in date-based calculations.
     * By default, this is {@link DayOfWeek#MONDAY} to match ISO-8601
     * @param startOfWeek   the first day of the calendar week
     * @return              a fluent interface
     * @since 3.0.0
     */
    T withStartOfWeek(@Nonnull DayOfWeek startOfWeek);

    /**
     * Sets the minimal number of days in the first week, from 1 to 7
     * By default, this is {@literal 4} to match ISO-8601
     * @param minimalDaysInFirstWeek    the minimum number of days in the first week of a year
     * @return            a fluent interface
     * @since 3.0.0
     */
    @Nonnull
    T withMinimalDaysInFirstWeek(int minimalDaysInFirstWeek);


    /**
     * Make the methods annotated with {@link com.github.tymefly.eel.udf.EelFunction} in the specified class
     * callable by EEL.
     * @param udfClass    Class that implements UDF function
     * @return            a fluent interface
     */
    @Nonnull
    T withUdfClass(@Nonnull Class<?> udfClass);

    /**
     * Search the specified {@code location} for classes annotated with
     * {@link com.github.tymefly.eel.udf.PackagedEelFunction}. For each class found make the methods annotated with
     * {@link com.github.tymefly.eel.udf.EelFunction} callable by EEL.
     * This method does not search sub-packages.
     * @param location      a package that should contain classes with functions EEL can call
     * @return              a fluent interface
     */
    @Nonnull
    T withUdfPackage(@Nonnull Package location);
}
