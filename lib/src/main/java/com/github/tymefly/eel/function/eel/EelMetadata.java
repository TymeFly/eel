package com.github.tymefly.eel.function.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * Functions that supply information about the EEL language.
 * @since 2.0
 */
@PackagedEelFunction
public class EelMetadata {
    /**
     * Returns the EEL version as text in the format {@code major.minor}, where both parts are numeric.
     * This format can be easily converted to a number if required.
     * @param context   the current EEL context
     * @return          the version text
     * @since 2.0
     */
    @Nonnull
    @EelFunction("eel.version")
    public String version(@Nonnull EelContext context) {
        return context.metadata().version();
    }


    /**
     * Returns the date and time the EEL compiler was built.
     * @param context   the current EEL context
     * @return          the EEL build date and time
     * @since 2.0
     */
    @Nonnull
    @EelFunction("eel.buildDate")
    public ZonedDateTime buildDate(@Nonnull EelContext context) {
        return context.metadata().buildDate();
    }
}
