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
     * Returns the DATE at which the given {@code context} was created in the UTC time zone.
     * This provides a timestamp that does not change across repeated invocations of an expression or related
     * expressions that use the same context.
     * @param context   the EEL context.
     * @param zone      the time zone name.
     * @param offsets   offsets to apply.
     * @return          a timestamp indicating when the {@code context} was created.
     * @throws DateTimeException if the {@code zone} is not valid.
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
     * Returns the current date in the UTC zone with optional offsets applied.
     * @param context   the current EEL context.
     * @param offsets   offsets to apply.
     * @return          the current time in the UTC zone.
     */
    @Nonnull
    @EelFunction("date.utc")
    public ZonedDateTime utc(@Nonnull EelContext context, @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now(UTC);

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the current date in the local time zone with optional offsets applied.
     * @param context   the current EEL context.
     * @param offsets   offsets to apply.
     * @return          the current time in the local time zone.
     */
    @Nonnull
    @EelFunction("date.local")
    public ZonedDateTime local(@Nonnull EelContext context, @Nonnull String... offsets) {
        ZonedDateTime result = ZonedDateTime.now();

        result = plusOffsets(context, result, offsets);

        return result;
    }


    /**
     * Returns the current date in the specified {@code zone} with optional offsets applied.
     * @param context   the current EEL context.
     * @param zone      the time zone.
     * @param offsets   offsets to apply.
     * @return          the current time in the specified zone.
     * @throws DateTimeException if the {@code zone} is not valid.
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
