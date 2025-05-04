package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Supported DATE periods
 */
public enum Period {
    YEAR("year", "y", ChronoUnit.YEARS),
    MONTH("month", "M", ChronoUnit.MONTHS),
    WEEK("week", "w", ChronoUnit.WEEKS),
    DAY("day", "d", ChronoUnit.DAYS),
    HOUR("hour", "h", ChronoUnit.HOURS),
    MINUTE("minute", "m", ChronoUnit.MINUTES),
    SECOND("second", "s", ChronoUnit.SECONDS),
    MILLI("milli", "I", ChronoUnit.MILLIS),
    MICRO("micro", "U", ChronoUnit.MICROS),
    NANO("nano", "N", ChronoUnit.NANOS),

    MILLI_OF_SECONDS("milliOfSecond", "millisOfSecond", "i", ChronoUnit.MILLIS),
    MICRO_OF_SECONDS("microOfSecond", "microsOfSecond", "u", ChronoUnit.MICROS),
    NANO_OF_SECONDS("nanoOfSecond", "nanosOfSecond", "n", ChronoUnit.NANOS);

    private static final Map<String, Period> LOOK_UP;

    private final String longForm;
    private final String pluralForm;
    private final String shortForm;
    private final ChronoUnit chronoUnit;


    static {
        Map<String, Period> lookup = new HashMap<>();

        for (var period : values()) {
            lookup.put(period.longForm, period);
            lookup.put(period.pluralForm, period);
            lookup.put(period.shortForm, period);
        }

        LOOK_UP = Collections.unmodifiableMap(lookup);
    }


    Period(@Nonnull String longForm, @Nonnull String shortForm, @Nonnull ChronoUnit chronoUnit) {
        this(longForm, longForm + "s", shortForm, chronoUnit);
    }

    Period(@Nonnull String longForm,
           @Nonnull String pluralForm,
           @Nonnull String shortForm,
           @Nonnull ChronoUnit chronoUnit) {
        this.longForm = longForm;
        this.pluralForm = pluralForm;
        this.shortForm = shortForm;
        this.chronoUnit = chronoUnit;
    }


    /**
     * Look up a period by identifier
     * @param key   either the lowercase name of the period or its single character abbreviation.
     * @return      The period represented by the {@code key}
     * @throws DateTimeException is the {@code key} is not valid
     */
    @Nonnull
    public static Period lookup(@Nonnull String key) throws DateTimeException {
        Period period = LOOK_UP.get(key);

        if (period == null) {
            throw new DateTimeException("Invalid date period '" + key + "'");
        }

        return period;
    }

    @Nonnull
    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    @Nonnull
    String shortForm() {
        return shortForm;
    }
}
