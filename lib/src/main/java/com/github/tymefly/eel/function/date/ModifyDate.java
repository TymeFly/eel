package com.github.tymefly.eel.function.date;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFunctionException;
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
     * <br>
     * <b>Note:</b> Week is not a supported field.
     * <br>
     * The EEL syntax for this function is <code>date.set( date, specifier... )</code>
     * @param date          date to format
     * @param specifiers    Field specifiers
     * @return the date at a reduced accuracy.
     * @throws EelFunctionException if one of the {@code specifiers} is not valid
     * @see #setZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction(name = "date.set")
    public ZonedDateTime set(@Nonnull ZonedDateTime date, @Nonnull String... specifiers) throws EelFunctionException {
        for (var specifier : specifiers) {
            date = DateHelper.setField(date, specifier);
        }

        return date;
    }


    /**
     * Entry point for the {@code setZone} function that returns a copy of the DATE passed to it but with a
     * different Zone.
     * Unlike {@link #moveZone(ZonedDateTime, String)} , this function will keep the same value for the time fields.
     * This means that the numeric value of the returned date (seconds since 1970-01-01 00:00:00 UTC) will not match
     * the date passed in.
     * <br>
     * The EEL syntax for this function is <code>date.setZone( date, zone )</code>
     * @param date  date to format
     * @param zone  Zone Id
     * @return the date at a reduced accuracy.
     * @throws EelFunctionException if one of the {@code zone} is not valid
     * @see #set(ZonedDateTime, String...)
     * @see #moveZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction(name = "date.setZone")
    public ZonedDateTime setZone(@Nonnull ZonedDateTime date, @Nonnull String zone) throws EelFunctionException {
        ZoneId zoneId = DateHelper.toZone(zone);

        return date.withZoneSameLocal(zoneId);
    }


    /**
     * Entry point for the {@code moveZone} function that returns a copy of the DATE passed to it but with a
     * different Zone.
     * Unlike {@link #setZone(ZonedDateTime, String)}, this function will adjust the time fields so that the numeric
     * value of the returned DATE (seconds since 1970-01-01 00:00:00 UTC) matches the date passed in.
     * <br>
     * The EEL syntax for this function is <code>date.moveZone( date, zone )</code>
     * @param date  date to format
     * @param zone  Zone Id
     * @return the date at a reduced accuracy.
     * @throws EelFunctionException if one of the {@code zone} is not valid
     * @see #setZone(ZonedDateTime, String)
     */
    @Nonnull
    @EelFunction(name = "date.moveZone")
    public ZonedDateTime moveZone(@Nonnull ZonedDateTime date, @Nonnull String zone) throws EelFunctionException {
        ZoneId zoneId = DateHelper.toZone(zone);

        date = date.withZoneSameInstant(zoneId);

        return date;
    }


    /**
     * Entry point for the {@code truncate} function that reduces the accuracy of a DATE.
     * This is done by setting less significant DATE fields to their lowest value. These are:
     * <ul>
     *  <li><i>year</i> - The month is set to January, the day to the 1st and the time to midnight</li>
     *  <li><i>month</i> - The day to the 1st of the month and the time to midnight</li>
     *  <li><i>day</i> - The time to midnight</li>
     *  <li><i>hour</i> - The minutes, seconds and fractions of a second are set to 0</li>
     *  <li><i>minute</i> - The seconds and fractions of a second are set to 0</li>
     *  <li><i>seconds</i> - The fractions of a second are set to 0</li>
     * </ul>
     * <br>
     * <b>Note:</b> Week is not a supported accuracy.
     * <br>
     * The EEL syntax for this function is <code>date.truncate( date, accuracy )</code>
     * @param date      date to format
     * @param accuracy  The required accuracy of the date
     * @return the date at a reduced accuracy.
     * @throws EelFunctionException if the {@code accuracy} is not valid
     */
    @Nonnull
    @EelFunction(name = "date.truncate")
    public ZonedDateTime truncate(@Nonnull ZonedDateTime date, @Nonnull String accuracy) throws EelFunctionException {
        Period period = Period.lookup(accuracy);

        // Suspend Checkstyle rule FallThrough for 20 lines: Fall through will clear high precision fields
        switch (period) {
            case YEAR: date = date.withMonth(1);
            case MONTH: date = date.withDayOfMonth(1);
            case DAY: date = date.withHour(0);
            case HOUR: date = date.withMinute(0);
            case MINUTE: date = date.withSecond(0);
            case SECOND: date = date.withNano(0);
                break;

            default:            // Period.WEEK
                throw new EelFunctionException("Unsupported date period '%s'", accuracy);
        }

        return date;
    }
}
