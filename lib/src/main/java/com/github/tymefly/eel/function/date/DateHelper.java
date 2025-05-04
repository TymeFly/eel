package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.EelContext;

/**
 * Helper methods for EEL date functions
 */
class DateHelper {
    @FunctionalInterface
    private interface Handler {
        /**
         * A date processing function that is performed after processing an offset pattern
         * @param context       current EEL context
         * @param date          date to be manipulated
         * @param value         value as given in the spec modifier
         * @param period        Period as given in the spec modifier
         * @return              The modified date
         * @throws RuntimeException if the action could not be taken
         */
        @Nonnull
        ZonedDateTime modify(@Nonnull EelContext context,
                             @Nonnull ZonedDateTime date,
                             int value,
                             @Nonnull Period period) throws RuntimeException;
    }

    private static final int MICRO_IN_NANO = 1_000;
    private static final int MILLIS_IN_MICRO = 1_000;
    private static final int MILLIS_IN_NANO = 1_000_000;

    // RegEx that accepts either
    //   1. an '@' sign followed by some letters
    //   2. a number followed by some letters. The number can have an optional sign and may contain '_' between digits
    private static final Pattern OFFSET_PATTERN = Pattern.compile("([+-]?\\d(?:\\d|_\\d)*|@) *([a-zA-Z]+)");


    private DateHelper() {
    }


    /**
     * Convert a zone name to a ZoneId
     * @param zone  The case-insensitive name of a Zone
     * @return      The associated ZoneId
     * @throws DateTimeException if the {@code zone} is not valid
     */
    @Nonnull
    static ZoneId toZone(@Nonnull String zone) throws DateTimeException {
        return ZoneId.of(zone);
    }


    /**
     * Add an offset to a time-date stamp
     * @param date  Date to adjust
     * @param spec  The offset from {@code date}. The format of the string is a signed number followed by the Period.
     *              Positive offsets are in the future; negative offsets are in the past
     * @return      an adjusted date
     * @throws DateTimeException if the {@code spec} is invalid
     */
    @Nonnull
    static ZonedDateTime plus(@Nonnull EelContext context,
                              @Nonnull ZonedDateTime date,
                              @Nonnull String spec) throws DateTimeException {
        return process(context, date, spec, DateHelper::plus);
    }


    /**
     * Subtract an offset to a time-date stamp
     * @param date  Date to adjust
     * @param spec  The offset from {@code date}. The format of the string is a signed number followed by the Period.
     *              Positive offsets are in the past; negative offsets are in the future
     * @return      an adjusted date
     * @throws DateTimeException if the {@code spec} is invalid
     */
    @Nonnull
    static ZonedDateTime minus(@Nonnull EelContext context,
                               @Nonnull ZonedDateTime date,
                               @Nonnull String spec) throws DateTimeException {
        return process(context, date, spec, DateHelper::minus);
    }


    /**
     * Returns a copy of the {@code date} with a specific field set to a given value
     * @param date  Date to adjust
     * @param spec  The field and value to set. The format of the string is a signed number followed by the Period.
     *              Positive offsets are in the future; negative offsets are in the past
     * @return a copy of the {@code date} with a specific field set to a given value
     * @throws DateTimeException if the {@code spec} is invalid
     */
    @Nonnull
    static ZonedDateTime setField(@Nonnull EelContext context,
                                  @Nonnull ZonedDateTime date,
                                  @Nonnull String spec) throws DateTimeException {
        return process(context, date, spec, DateHelper::set);
    }


    @Nonnull
    private static ZonedDateTime process(@Nonnull EelContext context,
                                         @Nonnull ZonedDateTime date,
                                         @Nonnull String spec,
                                         @Nonnull Handler handler) throws DateTimeException {
        Matcher matcher = OFFSET_PATTERN.matcher(spec);

        if (!matcher.matches()) {
            date = null;
        } else {
            String value = matcher.group(1);
            String unit = matcher.group(2);
            Period period = Period.lookup(unit);

            if ("@".equals(value)) {
                date = snap(context, date, period);
            } else {
                date = process(context, date, handler, value, period);
            }
        }

        if (date == null) {
            throw new DateTimeException("Invalid date modifier '" + spec + "'");
        }

        return date;
    }

    @Nullable
    private static ZonedDateTime process(@Nonnull EelContext context,
                                         @Nonnull ZonedDateTime date,
                                         @Nonnull Handler handler,
                                         @Nonnull String value,
                                         @Nonnull Period period) {
        try {
            int offset = Integer.parseInt(value.replace("_", ""));

            date = handler.modify(context, date, offset, period);
        } catch (DateTimeException e) {
            throw e;
        } catch (RuntimeException e) {
            date = null;
        }

        return date;
    }


    @Nonnull
    private static ZonedDateTime set(@Nonnull EelContext context,
                                     @Nonnull ZonedDateTime date,
                                     int value,
                                     @Nonnull Period period) {
        date = switch (period) {
            case YEAR -> date.withYear(value);
            case MONTH -> date.withMonth(value);
            case WEEK -> date.with(context.getWeek().weekOfYear(), value);
            case DAY -> date.withDayOfMonth(value);
            case HOUR -> date.withHour(value);
            case MINUTE -> date.withMinute(value);
            case SECOND -> date.withSecond(value);
            case MILLI, MICRO, NANO -> date.withNano(setFraction(date, period, value));
            case MILLI_OF_SECONDS -> date.withNano(value * MILLIS_IN_NANO);
            case MICRO_OF_SECONDS -> date.withNano(value * MICRO_IN_NANO);
            case NANO_OF_SECONDS -> date.withNano(value);
        };

        return date;
    }

    // Millis seconds, Micro seconds and Nano seconds can all be set independently
    private static int setFraction(@Nonnull ZonedDateTime date, @Nonnull Period period, int value) {
        if ((value < 0) || (value >= MICRO_IN_NANO)) {
            throw new DateTimeException("Invalid value for " + period.getChronoUnit() + ": " + value);
        }

        int fraction = date.getNano();
        int nanos = (period == Period.NANO ? value : fraction % MICRO_IN_NANO);
        int micros = (period == Period.MICRO ? value : (fraction / MICRO_IN_NANO) % MILLIS_IN_MICRO);
        int millis = (period == Period.MILLI ? value : fraction / MILLIS_IN_NANO);

        return (millis * MILLIS_IN_NANO) + (micros * MICRO_IN_NANO) + nanos;
    }


    @Nonnull
    @SuppressWarnings("PMD.UnusedFormalParameter")              // required to make function a Handler
    private static ZonedDateTime plus(@Nonnull EelContext context,
                                      @Nonnull ZonedDateTime date,
                                      int value,
                                      @Nonnull Period period) {
        return date.plus(value, period.getChronoUnit());
    }


    @Nonnull
    @SuppressWarnings("PMD.UnusedFormalParameter")              // required to make function a Handler
    private static ZonedDateTime minus(@Nonnull EelContext context,
                                       @Nonnull ZonedDateTime date,
                                       int value,
                                       @Nonnull Period period) {
        return date.minus(value, period.getChronoUnit());
    }


    @Nullable
    @SuppressWarnings("PMD.ImplicitSwitchFallThrough")
    private static ZonedDateTime snap(@Nonnull EelContext context,
                                      @Nonnull ZonedDateTime date,
                                      @Nonnull Period period) {
        int nano;

        // Suspend Checkstyle rule FallThrough for 10 lines: Fall through will clear higher precision fields
        switch (period) {
            case YEAR: date = date.withMonth(1);
            case MONTH: date = date.withDayOfMonth(1);
            case DAY: date = date.withHour(0);
            case HOUR: date = date.withMinute(0);
            case MINUTE: date = date.withSecond(0);
            case SECOND: date = date.withNano(0);
                break;

            case MILLI, MILLI_OF_SECONDS:
                nano = date.getNano();
                nano = nano / MILLIS_IN_NANO;
                nano = nano * MILLIS_IN_NANO;
                date = date.withNano(nano);
                break;

            case MICRO, MICRO_OF_SECONDS:
                nano = date.getNano();
                nano = nano / MICRO_IN_NANO;
                nano = nano * MICRO_IN_NANO;
                date = date.withNano(nano);
                break;

            case NANO, NANO_OF_SECONDS:                             // Do nothing - we are already at nano precision
                break;

            case WEEK: date = date.with(TemporalAdjusters.previousOrSame(context.getWeek().getFirstDayOfWeek()))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
                break;

            default:            // Should not happen
                date = null;
                break;
        }

        return date;
    }
}
