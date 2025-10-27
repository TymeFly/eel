package com.github.tymefly.eel.function.general;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.function.date.Period;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * General purpose date functions
 */
@PackagedEelFunction
public class Dates {
    /**
     * Returns the difference between two dates in a specific {@code period} of time.
     * If the duration cannot be exactly expressed in the specified unit, the fractional part is removed.
     * @param from   the start time, inclusive
     * @param to     the end time, inclusive
     * @param period the unit of time in which the result is returned
     * @return       the amount of time between {@code from} and {@code to} in terms of the {@code period}.
     *               The value is positive only if {@code from} precedes {@code to}.
     */
    @EelFunction("duration")
    public long duration(@Nonnull ZonedDateTime from,
                         @Nonnull ZonedDateTime to,
                         @DefaultArgument("seconds") String period) {
        return Period.lookup(period)
            .getChronoUnit()
            .between(from, to);
    }
}
