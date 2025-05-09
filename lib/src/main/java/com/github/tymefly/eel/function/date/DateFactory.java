package com.github.tymefly.eel.function.date;


import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * A collection of functions that create new Dates
 */
@PackagedEelFunction
public class DateFactory {
    private static final ZoneId UTC = ZoneId.of("UTC");


    /**
     * Entry point for the {@code start} function, which returns the DATE the EelContext was created in the
     * UTC time Zone.
     * The purpose of this function is to provide a time stamp that doesn't change across repeated invocations of an
     * expression or several related expressions.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>date.start()</code> - the start time in the UTC Zone</li>
     *  <li><code>date.start( zone )</code> - the start time in the specified Zone</li>
     *  <li><code>date.start( zone, offsets )</code> - the start time in the specified Zone after applying the
     *                          offsets</li>
     * </ul>
     * @param context   The EEL Context
     * @param zone      The optional zone name. This defaults to UTC
     * @param offsets   optional offsets.
     * @return a timestamp at which the {@code context} was created
     * @throws DateTimeException if the {@code zone} is not valid
     */
    @Nonnull
    @EelFunction("date.start")
    public ZonedDateTime start(@Nonnull EelContext context,
                               @DefaultArgument("UTC") @Nonnull String zone,
                               @DefaultArgument("") @Nonnull String... offsets) throws DateTimeException {
        ZoneId zoneId = DateHelper.toZone(zone);
        ZonedDateTime result = context.getStartTime()
            .withZoneSameInstant(zoneId);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Entry point for the {@code time.utc} function, which returns a DATE in the UTC zone with optional offsets
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>date.utc()</code> - the current time in the UTC Zone</li>
     *  <li><code>date.utc( offsets... )</code> - the time in the UTC Zone after applying the offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param offsets   optional offsets.
     * @return the time in the UTC zone
     */
    @Nonnull
    @EelFunction("date.utc")
    public ZonedDateTime utc(@Nonnull EelContext context, @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now(UTC);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Entry point for the {@code time.local} function, which returns a DATE in the local time zone with
     * optional offsets
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>date.local()</code> - the current time in the Local Zone</li>
     *  <li><code>date.local( offsets... )</code> - the time in the Local Zone after applying the offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param offsets   optional offsets.
     * @return the time in the Local Time zone
     */
    @Nonnull
    @EelFunction("date.local")
    public ZonedDateTime local(@Nonnull EelContext context, @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now();

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Entry point for the {@code date.at} function, which returns the current DATE in the specified zone
     * with optional offsets
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>date.at( zone )</code> - the current time in the specified Zone</li>
     *  <li><code>date.at( zone, offsets... )</code> - the time in the specified Zone after applying the offsets</li>
     * </ul>
     * @param context   The current EEL Context
     * @param zone      A time Zone
     * @param offsets   optional offsets.
     * @return the time in the UTC zone
     * @throws DateTimeException if the {@code zone} is not valid
     * @see ZoneId#of(String)
     */
    @Nonnull
    @EelFunction("date.at")
    public ZonedDateTime at(@Nonnull EelContext context,
                            @Nonnull String zone,
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
