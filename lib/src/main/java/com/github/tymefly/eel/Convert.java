package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * Implements the EEL type conversion rules.
 */
class Convert {
    private static final Set<String> FALSE_TEXT = Set.of("false", "0", "");
    private static final Set<String> TRUE_TEXT = Set.of("true", "1");

    private static final int YEAR_WIDTH = 4;
    private static final int MONTH_WIDTH = 2;
    private static final int DAY_WIDTH = 2;
    private static final int HOUR_WIDTH = 2;
    private static final int MINUTE_WIDTH = 2;
    private static final int SECOND_WIDTH = 2;

    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final DateTimeFormatter DATE_TO_STRING = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    private static final DateTimeFormatter STRING_TO_DATE = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, YEAR_WIDTH)
        .optionalStart()
            .optionalStart().appendLiteral('-').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendValue(ChronoField.MONTH_OF_YEAR, MONTH_WIDTH)
            .optionalStart()
                .optionalStart().appendLiteral('-').optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .appendValue(ChronoField.DAY_OF_MONTH, DAY_WIDTH)
                .optionalStart()
                    .optionalStart().appendLiteral('T').optionalEnd()
                    .optionalStart().appendLiteral(' ').optionalEnd()
                    .appendValue(ChronoField.HOUR_OF_DAY, HOUR_WIDTH)
                    .optionalStart()
                        .optionalStart().appendLiteral(':').optionalEnd()
                        .optionalStart().appendLiteral(' ').optionalEnd()
                        .appendValue(ChronoField.MINUTE_OF_HOUR, MINUTE_WIDTH)
                        .optionalStart()
                            .optionalStart().appendLiteral(':').optionalEnd()
                            .optionalStart().appendLiteral(' ').optionalEnd()
                            .appendValue(ChronoField.SECOND_OF_MINUTE, SECOND_WIDTH)
                        .optionalEnd()
                    .optionalEnd()
                .optionalEnd()
            .optionalEnd()
        .optionalEnd()
        .optionalStart().appendLiteral(' ').optionalEnd()
        .optionalStart().appendPattern("X").optionalEnd()
        .toFormatter();

    private Convert() {
    }

    @Nonnull
    static String toText(@Nonnull BigDecimal value) {
        return value.toPlainString();
    }

    @Nonnull
    static String toText(@Nonnull ZonedDateTime value) {
        return DATE_TO_STRING.format(value);
    }



    static boolean toLogic(@Nonnull String value) {
        boolean result;

        value = value.trim().toLowerCase();

        if (FALSE_TEXT.contains(value)) {
            result = false;
        } else if (TRUE_TEXT.contains(value)) {
            result = true;
        } else {
            result = invalid(value, "Logic");
        }

        return result;
    }

    static boolean toLogic(@Nonnull BigDecimal number) {
        return (BigDecimal.ZERO.compareTo(number) < 0);
    }

    static boolean toLogic(@Nonnull ZonedDateTime date) {
        return (date.toEpochSecond() > 0);
    }



    @Nonnull
    static BigDecimal toNumber(@Nonnull String value) {
        BigDecimal number;

        try {
            number = NumberParser.parse(value.trim());
        } catch (NumberFormatException e) {
            number = invalid(value, "Number");
        }

        return number;
    }

    @Nonnull
    static BigDecimal toNumber(@Nonnull ZonedDateTime time) {
        return BigDecimal.valueOf(time.toEpochSecond());
    }


    @Nonnull
    static ZonedDateTime toDate(@Nonnull String text) {
        TemporalAccessor accessor = STRING_TO_DATE.parse(text.trim());
        int year = accessor.get(ChronoField.YEAR);
        int month = extractChrono(accessor, ChronoField.MONTH_OF_YEAR, 1);
        int day = extractChrono(accessor, ChronoField.DAY_OF_MONTH, 1);
        int hour = extractChrono(accessor, ChronoField.HOUR_OF_DAY, 0);
        int minute = extractChrono(accessor, ChronoField.MINUTE_OF_HOUR, 0);
        int second = extractChrono(accessor, ChronoField.SECOND_OF_MINUTE, 0);
        ZoneId zone = accessor.query(TemporalQueries.zone());
        ZonedDateTime result = ZonedDateTime.of(year,
            month,
            day,
            hour,
            minute,
            second,
            0,
            (zone == null ? UTC : zone));

        return result;
    }

    @Nonnull
    static ZonedDateTime toDate(@Nonnull BigDecimal number) {
        Instant instant = Instant.ofEpochSecond(number.longValue());
        ZonedDateTime result = ZonedDateTime.ofInstant(instant, UTC);

        return result;
    }


    private static int extractChrono(@Nonnull TemporalAccessor accessor,
                                     @Nonnull ChronoField field,
                                     int defaultValue) {
        return accessor.isSupported(field) ? accessor.get(field) : defaultValue;
    }


    private static <T> T invalid(@Nonnull Object value, @Nonnull String targetType) {
        throw new EelConvertException("Can not convert '%s' from %s to %s",
            value, value.getClass().getSimpleName(), targetType);
    }
}
