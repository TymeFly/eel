package com.github.tymefly.eel.function.date;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that manipulate dates
 */
@PackagedEelFunction
public class DateOperations {
    /**
     * Entry point for the {@code duration} function, which returns the difference between two dates in
     * a specific unit of time. If the duration can not be exactly expressed in the specified unit then the
     * fractional part is removed.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *     <li><code>duration(from, to)</code> -
     *              returns the duration in seconds</li>
     *     <li><code>duration(from, to, period)</code> -
     *              returns the duration in the specified period</li>
     * </ul>
     * @param from      the start time, inclusive
     * @param to        the end time, inclusive
     * @param period    the unit of time the results is returned in
     * @return the difference between two time stamps
     */
    @EelFunction(name = "duration")
    public long duration(@Nonnull ZonedDateTime from,
                         @Nonnull ZonedDateTime to,
                         @DefaultArgument("s") String period) {
        return Period.lookup(period)
            .getChronoUnit()
            .between(from, to);
    }
}
