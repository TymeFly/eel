package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Helper methods for EEL date functions
 */
class DateHelper {
    private static final Pattern OFFSET_PATTERN = Pattern.compile("([+-]?\\d+) *([a-zA-Z]+)");

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
     * Adjust a time-date stamp
     * @param date  Date to adjust
     * @param spec  The offset from {@code date}. The format of the string is a signed number followed by the Period.
     *              Positive offsets are in the future; negative offsets are in the past
     * @return      an adjusted date
     * @throws DateTimeException if the {@code spec} is invalid
     */
    @Nonnull
    static ZonedDateTime applyOffset(@Nonnull ZonedDateTime date, @Nonnull String spec) throws DateTimeException {
        Matcher matcher = OFFSET_PATTERN.matcher(spec);

        if (!matcher.matches()) {
            throw new DateTimeException("Invalid Date offset '" + spec + "'");
        }

        long offset = Long.parseLong(matcher.group(1));
        Period period = Period.lookup(matcher.group(2));

        date = switch (period) {
            case YEAR -> date.plusYears(offset);
            case MONTH -> date.plusMonths(offset);
            case WEEK -> date.plus(offset, ChronoUnit.WEEKS);
            case DAY -> date.plusDays(offset);
            case HOUR -> date.plusHours(offset);
            case MINUTE -> date.plusMinutes(offset);
            case SECOND -> date.plusSeconds(offset);
        };

        return date;
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
    static ZonedDateTime setField(@Nonnull ZonedDateTime date, @Nonnull String spec) throws DateTimeException {
        Matcher matcher = OFFSET_PATTERN.matcher(spec);

        if (matcher.matches()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                Period period = Period.lookup(matcher.group(2));

                date = switch (period) {
                    case YEAR -> date.withYear(value);
                    case MONTH -> date.withMonth(value);
                    case WEEK -> null;
                    case DAY -> date.withDayOfMonth(value);
                    case HOUR -> date.withHour(value);
                    case MINUTE -> date.withMinute(value);
                    case SECOND -> date.withSecond(value);
                };
            } catch (RuntimeException e) {
                date = null;
            }
        } else {
            date = null;
        }

        if (date == null) {
            throw new DateTimeException("Invalid Date field '" + spec + "'");
        }

        return date;
    }
}
