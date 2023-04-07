package com.github.tymefly.eel.function.date;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that adds one or more offsets to a DATE
 * <br>
 * The EEL syntax for this function is <code>date.offset( date, offsets... )</code>
 */
@PackagedEelFunction
public class Offset {
    /**
     * Entry point for the {@code date.offset} function
     * @param date      date to modify
     * @param offsets   offsets to apply
     * @return the {@code date} with all the offsets applied
     * @see DateHelper#applyOffset(ZonedDateTime, String) 
     */
    @Nonnull
    @EelFunction(name = "date.offset")
    public ZonedDateTime offset(@Nonnull ZonedDateTime date, @Nonnull String... offsets) {
        for (var offset : offsets) {
            date = DateHelper.applyOffset(date, offset);
        }

        return date;
    }
}