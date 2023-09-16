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
enum Period {
    YEAR("y", ChronoUnit.YEARS),
    MONTH("M", ChronoUnit.MONTHS),
    WEEK("w", ChronoUnit.WEEKS),
    DAY("d", ChronoUnit.DAYS),
    HOUR("h", ChronoUnit.HOURS),
    MINUTE("m", ChronoUnit.MINUTES),
    SECOND("s", ChronoUnit.SECONDS);

    private static final Map<String, Period> LOOK_UP;

    private final String identifier;
    private final ChronoUnit chronoUnit;


    static {
        Map<String, Period> lookup = new HashMap<>();

        for (var period : Period.values()) {
            String longForm = period.name().toLowerCase();

            lookup.put(period.identifier, period);
            lookup.put(longForm, period);
            lookup.put(longForm + "s", period);
        }

        LOOK_UP = Collections.unmodifiableMap(lookup);
    }


    Period(@Nonnull String identifier, @Nonnull ChronoUnit chronoUnit) {
        this.identifier = identifier;
        this.chronoUnit = chronoUnit;
    }


    /**
     * Look up a period by identifier
     * @param key   either the lowercase name of the period or its single character abbreviation.
     * @return      The period represented by the {@code key}
     * @throws DateTimeException is the {@code key} is not valid
     */
    @Nonnull
    static Period lookup(@Nonnull String key) throws DateTimeException {
        Period period = LOOK_UP.get(key);

        if (period == null) {
            throw new DateTimeException("Invalid date period '" + key + "'");
        }

        return period;
    }

    @Nonnull
    ChronoUnit getChronoUnit() {
        return chronoUnit;
    }
}
