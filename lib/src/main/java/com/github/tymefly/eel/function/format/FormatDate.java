package com.github.tymefly.eel.function.format;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.function.date.DateFactory;
import com.github.tymefly.eel.function.date.Offset;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL function that converts DATE to TEXT with a custom format
 */
@PackagedEelFunction
public class FormatDate {
    private final DateFactory dateFactory;
    private final Offset dateOffsets;


    /** Application constructor */
    public FormatDate() {
        this(new DateFactory());
    }

    /**
     * Unit test constructor
     * @param dateFactory   DateFactory
     */
    @VisibleForTesting
    FormatDate(@Nonnull DateFactory dateFactory) {
        this.dateFactory = dateFactory;
        this.dateOffsets = new Offset();
    }


    /**
     * Returns the given {@code date} formatted according to the specified {@code format} and optional offsets.
     * @param context   the current EEL context.
     * @param format    the format of the returned string.
     * @param date      the date to format.
     * @param offsets   optional offsets to apply.
     * @return          the formatted date.
     * @see
     * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *              Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.date")
    public String formatDate(@Nonnull EelContext context,
                             @Nonnull String format,
                             @Nonnull ZonedDateTime date,
                             @Nonnull String... offsets) {
        date = dateOffsets.plus(context, date, offsets);

        return DateTimeFormatter.ofPattern(format)
            .format(date);
    }


    /**
     * Returns a formatted date with optional offsets, formatted according to the specified {@code format}
     * and {@code zone}.
     * This function is equivalent to {@code $format.date(format, date.start(), offsets)}.
     * @param context   the current EEL context.
     * @param format    the format of the returned text.
     * @param zone      the time zone.
     * @param offsets   optional offsets to apply.
     * @return          the formatted date.
     * @see DateFactory#start(EelContext, String, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see
     * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *              Java DateTimeFormatter</a>
     * @since 2.1.0
     */
    @Nonnull
    @EelFunction("format.start")
    public String formatStart(@Nonnull EelContext context,
                              @Nonnull String format,
                              @DefaultArgument("UTC") @Nonnull String zone,
                              @DefaultArgument(value = "") @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.start(context, zone, offsets);

        return formatDate(context, format, date);
    }


    /**
     * Returns a formatted date in the UTC zone with optional offsets, formatted according to the specified
     * {@code format} text.
     * This function is equivalent to {@code $format.date(format, date.utc(), offsets)}.
     * @param context   the current EEL context.
     * @param format    the format of the returned text.
     * @param offsets   optional offsets to apply.
     * @return          the formatted date text.
     * @see DateFactory#utc(EelContext, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see
     * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *              Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.utc")
    public String formatUtc(@Nonnull EelContext context, @Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.utc(context, offsets);

        return formatDate(context, format, date);
    }


    /**
     * Returns a formatted date in the local time zone with optional offsets, formatted according to the specified
     * {@code format} text.
     * This function is equivalent to {@code $format.date(format, date.local(), offsets)}.
     * @param context   the current EEL context.
     * @param format    the format of the returned text.
     * @param offsets   optional offsets to apply.
     * @return          the formatted date text.
     * @see DateFactory#local(EelContext, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see
     * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *              Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.local")
    public String formatLocal(@Nonnull EelContext context, @Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.local(context, offsets);

        return formatDate(context, format, date);
    }


    /**
     * Returns a formatted date in the specified {@code zone} with optional offsets, formatted according to the
     * specified {@code format} text.
     * This function is equivalent to {@code $format.date(format, date.at(zone), offsets)}.
     * @param context   the current EEL context.
     * @param zone      the time zone.
     * @param format    the format of the returned text.
     * @param offsets   optional offsets to apply.
     * @return          the formatted date text.
     * @see DateFactory#at(EelContext, String, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see
     * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *              Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.at")
    public String formatAt(@Nonnull EelContext context,
                           @Nonnull String zone,
                           @Nonnull String format,
                           @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.at(context, zone, offsets);

        return formatDate(context, format, date);
    }
}