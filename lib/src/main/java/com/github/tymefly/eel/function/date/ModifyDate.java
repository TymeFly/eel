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
     * Entry point for the {@code set} function that return a copy of DATE passed in, but individual fields set
     * to specified values.
     * The EEL syntax for this function is <code>date.set( date, specifier... )</code>
     * @param context       The current EEL Context
     * @param date          date to format
     * @param spec          Field specifiers
     * @return the date with specific fields changed
     * @throws DateTimeException if one of the {@code specifiers} is not valid
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
     * Entry point for the {@code setZone} function, which returns a copy of the DATE passed to it but with a
     * different Zone.
     * Unlike {@link #moveZone(ZonedDateTime, String)} , this function will keep the same value for the time fields.
     * This means that the numeric value of the returned date (seconds since 1970-01-01 00:00:00 UTC) will not match
     * the date passed in.
     * <br>
     * The EEL syntax for this function is <code>date.setZone( date, zone )</code>
     * @param date  date to format
     * @param zone  Zone Id
     * @return the date with a different zone
     * @throws DateTimeException if the {@code zone} is not valid
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
     * Entry point for the {@code moveZone} function, which returns a copy of the DATE passed to it but with a
     * different Zone.
     * Unlike {@link #setZone(ZonedDateTime, String)}, this function will adjust the time fields so that the numeric
     * value of the returned DATE (seconds since 1970-01-01 00:00:00 UTC) matches the date passed in.
     * <br>
     * The EEL syntax for this function is <code>date.moveZone( date, zone )</code>
     * @param date  date to format
     * @param zone  Zone Id
     * @return the same instant given by the {@code date} but in a different zone
     * @throws DateTimeException if the {@code zone} is not valid
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
