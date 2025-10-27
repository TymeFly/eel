package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * A collection of functions that will modify the Date
 */
@PackagedEelFunction
public class ModifyDate {
    /**
     * Returns a copy of the given {@code date} with individual fields updated according to the specified {@code spec}.
     * @param context   the current EEL context.
     * @param date      the date to update.
     * @param spec      the field specifiers.
     * @return          the date with specific fields changed.
     * @throws DateTimeException if one of the {@code specifiers} is not valid.
     * @see #setZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction("date.set")
    public ZonedDateTime set(@Nonnull EelContext context,
                             @Nonnull ZonedDateTime date,
                             @Nonnull String... spec) throws DateTimeException {
        for (var specifier : spec) {
            date = DateHelper.setField(context, date, specifier);
        }

        return date;
    }


    /**
     * Returns a copy of the given {@code date} with a different time zone.
     * Unlike {@link #moveZone(ZonedDateTime, String)}, this function preserves the time fields.
     * The numeric value of the returned date (seconds since 1970-01-01 00:00:00 UTC) may therefore differ from
     * the original date.
     * @param date  the date to update.
     * @param zone  the zone ID.
     * @return      the date with a different time zone.
     * @throws DateTimeException if the {@code zone} is not valid.
     * @see #set(EelContext, ZonedDateTime, String...)
     * @see #moveZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction("date.setZone")
    public ZonedDateTime setZone(@Nonnull ZonedDateTime date, @Nonnull String zone) throws DateTimeException {
        ZoneId zoneId = DateHelper.toZone(zone);

        return date.withZoneSameLocal(zoneId);
    }


    /**
     * Returns a copy of the given {@code date} with a different time zone.
     * Unlike {@link #setZone(ZonedDateTime, String)}, this function adjusts the time fields so that the numeric
     * value of the returned date (seconds since 1970-01-01 00:00:00 UTC) matches the original date.
     * @param date  the date to update.
     * @param zone  the zone ID.
     * @return      the same instant as the given {@code date} but in a different time zone.
     * @throws DateTimeException if the {@code zone} is not valid.
     * @see #setZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction("date.moveZone")
    public ZonedDateTime moveZone(@Nonnull ZonedDateTime date, @Nonnull String zone) throws DateTimeException {
        ZoneId zoneId = DateHelper.toZone(zone);

        date = date.withZoneSameInstant(zoneId);

        return date;
    }
}
