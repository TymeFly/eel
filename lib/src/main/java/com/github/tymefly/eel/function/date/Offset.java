package com.github.tymefly.eel.function.date;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that apply offsets to dates. The dates may be based on the current date,
 * the date the context was created, or values passed as arguments.
 * @since 1.0
 */
@PackagedEelFunction
public class Offset {
    /**
     * Returns a copy of the given {@code date} with all specified {@code offsets} added.
     * @param context   the current EEL context
     * @param date      the date to modify
     * @param offsets   the offsets to apply to the {@code date}, applied sequentially
     * @return          a copy of the {@code date} with all offsets applied
     * @since 1.0
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
     * @param context   the current EEL context
     * @param date      the date to modify
     * @param offsets   the offsets to apply to the {@code date}, applied sequentially
     * @return          a copy of the {@code date} with all offsets applied
     * @since 1.0
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