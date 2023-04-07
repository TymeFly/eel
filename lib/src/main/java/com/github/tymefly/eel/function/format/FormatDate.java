package com.github.tymefly.eel.function.format;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.function.date.DateFactory;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL functions that converts DATE to TEXT with a custom format
 */
@PackagedEelFunction
public class FormatDate {
    private final DateFactory dateFactory;

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
    }


    /**
     * Entry point for the {@code format.date} function that returns any given DATE in a customised formatted
     * <br>
     * The EEL syntax for this function is <code>format.date( date, format )</code>
     * @param format    Format of the returned string.
     * @param date      date to format
     * @return the formatted date
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction(name = "format.date")
    public String formatDate(@Nonnull String format, @Nonnull ZonedDateTime date) {
        String result = DateTimeFormatter.ofPattern(format)
            .format(date);

        return result;
    }


    /**
     * Entry point for the {@code format.utc} function that returns a DATE in the UTC zone with optional offsets
     * in a customised formatted. This function is the equivalent of
     * {@code format.date( date.utc(), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.utc( format )</code> - format the current time in the UTC Zone</li>
     *  <li><code>format.utc( format, offsets... )</code> - format the time in the UTC Zone after applying offsets</li>
     * </ul>
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#utc(String...)
     * @see #formatDate(String, ZonedDateTime)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction(name = "format.utc")
    public String formatUtc(@Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.utc(offsets);

        return formatDate(format, date);
    }


    /**
     * Entry point for the {@code format.local} function that returns a DATE in the local time zone with optional
     * offsets in a customised formatted. This function is the equivalent of
     * {@code format.date( date.local(), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.local( format )</code> - format the current time in the UTC Zone</li>
     *  <li><code>format.local( format, offsets... )</code>
     *          - format the time in the local Zone after applying offsets</li>
     * </ul>
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#local(String...) (String...)
     * @see #formatDate(String, ZonedDateTime)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction(name = "format.local")
    public String formatLocal(@Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.local(offsets);

        return formatDate(format, date);
    }


    /**
     * Entry point for the {@code format.local} function that returns a DATE the specified zone with optional
     * offsets in a customised formatted. This function is the equivalent of
     * {@code format.date( date.at( zone ), format ) }
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>format.at( zone, format )</code> - format the current time in the specified Zone</li>
     *  <li><code>format.at( zone, format, offsets... )</code>
     *          - format the time in the specified Zone after applying offsets</li>
     * </ul>
     * @param zone      A time Zone
     * @param format    Format of the returned string.
     * @param offsets   optional offsets.
     * @return the formatted date
     * @see DateFactory#at(String, String...) (String...)
     * @see #formatDate(String, ZonedDateTime)
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  Java DateTimeFormatter</a>
     */
    @Nonnull
    @EelFunction(name = "format.at")
    public String formatAt(@Nonnull String zone, @Nonnull String format, @Nonnull String... offsets) {
        ZonedDateTime date = dateFactory.at(zone, offsets);

        return formatDate(format, date);
    }
}