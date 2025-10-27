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
     * Returns a copy of the given {@code date} with all specified {@code offsets} added.
     * @param context   the current EEL context.
     * @param date      the date to modify.
     * @param offsets   the offsets to apply.
     * @return          the {@code date} with all offsets applied.
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
     * Returns a copy of the given {@code date} with all specified {@code offsets} subtracted.
     * @param context   the current EEL context.
     * @param date      the date to modify.
     * @param offsets   the offsets to apply.
     * @return          the {@code date} with all offsets applied.
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