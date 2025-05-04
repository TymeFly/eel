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
     * Entry point for the {@code format.date} function, which returns any given DATE in a customised formatted
     * <br>
     * The EEL syntax for this function is <code>format.date( date, format, offsets... )</code>
     * @param context   The current EEL Context
     * @param format    Format of the returned string.
     * @param date      date to format
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
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
     * Entry point for the {@code format.start} function, which returns a DATE with optional offsets
     * in a customised formatted. This function is the equivalent of {@code format.date( date.start(), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.start( format )</code> - format the current time in the UTC Zone</li>
     *  <li><code>format.start( format, zone )</code> - format the current time in the specified Zone</li>
     *  <li><code>format.start( format, zone, offsets... )</code> - format the time in the specified Zone after
     *               applying offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param format    Format of the returned string.
     * @param zone      time zone
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#start(EelContext, String, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     * @since 2.1.0
     */
    @Nonnull
    @EelFunction("format.start")
    public String formatStart(@Nonnull EelContext context,
                              @Nonnull String format,
                              @DefaultArgument("UTC") @Nonnull String zone,
                              @DefaultArgument("") @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.start(context, zone, offsets);

        return formatDate(context, format, date);
    }

    /**
     * Entry point for the {@code format.utc} function, which returns a DATE in the UTC zone with optional offsets
     * in a customised formatted. This function is the equivalent of {@code format.date( date.utc(), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.utc( format )</code> - format the current time in the UTC Zone</li>
     *  <li><code>format.utc( format, offsets... )</code> - format the time in the UTC Zone after applying offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#utc(EelContext, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.utc")
    public String formatUtc(@Nonnull EelContext context, @Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.utc(context, offsets);

        return formatDate(context, format, date);
    }


    /**
     * Entry point for the {@code format.local} function, which returns a DATE in the local time zone with optional
     * offsets in a customised formatted. This function is the equivalent of {@code format.date( date.local(), format )}
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.local( format )</code> - format the current time in the UTC Zone</li>
     *  <li><code>format.local( format, offsets... )</code>
     *          - format the time in the local Zone after applying offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#local(EelContext, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction("format.local")
    public String formatLocal(@Nonnull EelContext context, @Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.local(context, offsets);

        return formatDate(context, format, date);
    }


    /**
     * Entry point for the {@code format.local} function, which returns a DATE the specified zone with optional
     * offsets in a customised formatted. This function is the equivalent of
     * {@code format.date( date.at( zone ), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.at( zone, format )</code> - format the current time in the specified Zone</li>
     *  <li><code>format.at( zone, format, offsets... )</code>
     *          - format the time in the specified Zone after applying offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param zone      A time Zone
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#at(EelContext, String, String...)
     * @see #formatDate(EelContext, String, ZonedDateTime, String...)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
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