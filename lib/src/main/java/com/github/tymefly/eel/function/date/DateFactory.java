package com.github.tymefly.eel.function.date;


import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Provides various functions that return new dates. These may be based on the current date,
 * the date the context was created, or parsed text.
 * All functions accept an optional sequence of offsets.
 * @since 1.0
 */
@PackagedEelFunction
public class DateFactory {
    private static final ZoneId UTC = ZoneId.of("UTC");


    /**
     * Parses text representing a date/time using the specified {@link java.time.format.DateTimeFormatter}
     * pattern.
     * <p>
     * The {@code pattern} must follow the syntax defined by {@link java.time.format.DateTimeFormatter}.
     * The {@code date} text must conform exactly to this pattern.
     * <p>
     * Any temporal fields not defined by the {@code pattern} are assigned default values as follows:
     * <ul>
     *   <li>Era defaults to AD (CE)</li>
     *   <li>Time zone defaults to UTC if not specified by the pattern</li>
     *   <li>Year defaults to the current year at the time of parsing</li>
     *   <li>All other fields default to their lowest valid values (e.g., month = 1, day = 1, hour = 0)</li>
     * </ul>
     * Fields present in the {@code pattern} must be present in the {@code date} text and are parsed strictly.
     * @param context   the current EEL context
     * @param pattern   the {@link java.time.format.DateTimeFormatter} pattern describing the expected structure
     *                  of the {@code date} text
     * @param date      the date/time text to parse, which must exactly match the specified {@code pattern}
     * @param offsets   optional offsets to apply to the timestamp, applied sequentially
     * @return          the parsed date/time value with any missing fields defaulted as described above
     * @throws DateTimeException if the {@code pattern} is invalid or the {@code date} doesn't match the {@code pattern}
     * @see <a
     *  href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html">
     *  DateTimeFormatter documentation</a>
     * @since 3.2
     */
    @Nonnull
    @EelFunction("date.parse")
    public ZonedDateTime parse(@Nonnull EelContext context,
                               @Nonnull String pattern,
                               @Nonnull String date,
                               @DefaultArgument(value = "", description = "No offsets") @Nonnull String... offsets)
            throws DateTimeException {
        TemporalAccessor parsed = new DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .parseDefaulting(ChronoField.ERA, 1)                                // default to AD
            .parseDefaulting(ChronoField.YEAR_OF_ERA, Year.now().getValue())    // default to current year
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .parse(date);
        ZoneId zone = parsed.query(TemporalQueries.zone());
        ZonedDateTime result;

        if (zone != null) {                                         // Zone supplied in input
            result = ZonedDateTime.from(parsed);
        } else {                                                    // Default zone to UTC
            result = LocalDateTime.from(parsed)
                .atZone(ZoneOffset.UTC);
        }

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the date/time at which the given {@code context} was created, adjusted to the specified time zone.
     * <p>
     * The returned value represents a fixed timestamp associated with the creation of the {@code context}.
     * It does not change across repeated evaluations using the same context.
     * <p>
     * The timestamp is converted to the specified {@code zone}. Any supplied {@code offsets}
     * are then applied, in order, to the resulting value.
     * @param context   the current EEL context
     * @param zone      a time zone ID (as defined by {@link java.time.ZoneId}) to which the
     *                  timestamp is converted
     * @param offsets   optional offsets to apply to the timestamp, applied sequentially
     * @return the context creation timestamp adjusted to the specified {@code zone} and offsets
     * @throws DateTimeException if the {@code zone} is not a valid time zone ID
     * @see ZoneId#of(String)
     * @since 1.0
     */
    @Nonnull
    @EelFunction("date.start")
    public ZonedDateTime start(@Nonnull EelContext context,
                               @DefaultArgument("UTC") @Nonnull String zone,
                               @DefaultArgument(value = "", description = "No offsets")
                                   @Nonnull String... offsets) throws DateTimeException {
        ZoneId zoneId = DateHelper.toZone(zone);
        ZonedDateTime result = context.getStartTime()
            .withZoneSameInstant(zoneId);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the current date/time in the UTC time zone, with optional offsets applied.
     * The returned value is evaluated at the time of invocation.
     * @param context   the current EEL context
     * @param offsets   the offsets to apply to the current timestamp, applied sequentially
     * @return          the current date/time in the UTC time zone with the specified offsets applied
     * @since 1.0
     */
    @Nonnull
    @EelFunction("date.utc")
    public ZonedDateTime utc(@Nonnull EelContext context,
                             @DefaultArgument(value = "", description = "No offsets") @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now(UTC);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the current date/time in the local time zone with optional offsets applied.
     * The returned value is evaluated at the time of invocation.
     * @param context   the current EEL context
     * @param offsets   the offsets to apply to the current timestamp, applied sequentially
     * @return          the current date/time in the local time zone with the specified offsets applied
     * @since 1.0
     */
    @Nonnull
    @EelFunction("date.local")
    public ZonedDateTime local(@Nonnull EelContext context,
                               @DefaultArgument(value = "", description = "No offsets") @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now();

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the current date/time in the specified {@code zone} with optional offsets applied.
     * The returned value is evaluated at the time of invocation.
     * @param context   the current EEL context
     * @param zone      the time zone
     * @param offsets   the offsets to apply to the current timestamp (for example, years, months,
     *                  days, or hours), applied sequentially
     * @return          the current date/time in the specified {@code zone} with the offsets applied
     * @throws DateTimeException if the {@code zone} is not a valid time zone ID
     * @see ZoneId#of(String)
     * @since 1.0
     */
    @Nonnull
    @EelFunction("date.at")
    public ZonedDateTime at(@Nonnull EelContext context,
                            @Nonnull String zone,
                            @DefaultArgument(value = "", description = "No offsets")
                                   @Nonnull String... offsets) throws DateTimeException {
        ZoneId zoneId = DateHelper.toZone(zone);
        ZonedDateTime result = ZonedDateTime.now(zoneId);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    @Nonnull
    private static ZonedDateTime plusOffsets(@Nonnull EelContext context,
                                             @Nonnull ZonedDateTime result,
                                             @Nonnull String... offsets) {
        for (var offset : offsets) {
            result = DateHelper.plus(context, result, offset);
        }

        return result;
    }
}
