package com.github.tymefly.eel.function.date;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL functions that apply offsets to a dates
 */
@PackagedEelFunction
public class Offset {
    /**
     * Entry point for the {@code date.plus} function.
     * <br>
     * The EEL syntax for this function is <code>date.plus( date, offsets... )</code>
     * @param context   The current EEL Context
     * @param date      date to modify
     * @param offsets   offsets to apply
     * @return the {@code date} with all the offsets applied
     */
    @Nonnull
    @EelFunction("date.plus")
    public ZonedDateTime plus(@Nonnull EelContext context, @Nonnull ZonedDateTime date, @Nonnull String... offsets) {
        for (var offset : offsets) {
            date = DateHelper.plus(context, date, offset);
        }

        return date;
    }

    /**
     * Entry point for the {@code date.minus} function.
     * <br>
     * The EEL syntax for this function is <code>date.minus( date, offsets... )</code>
     * @param context   The current EEL Context
     * @param date      date to modify
     * @param offsets   offsets to apply
     * @return the {@code date} with all the offsets applied
     */
    @Nonnull
    @EelFunction("date.minus")
    public ZonedDateTime minus(@Nonnull EelContext context, @Nonnull ZonedDateTime date, @Nonnull String... offsets) {
        for (var offset : offsets) {
            date = DateHelper.minus(context, date, offset);
        }

        return date;
    }
}