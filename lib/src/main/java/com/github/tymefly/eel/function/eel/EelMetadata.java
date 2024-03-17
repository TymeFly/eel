package com.github.tymefly.eel.function.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions provide build time information about EEL
 */
@PackagedEelFunction
public class EelMetadata {
    /**
     * Entry point for the {@code eel.version} function, which returns the EEL version. This is in the format
     * {@code majorVersion.minorVersion}, where both values are numeric
     * <br>
     * The EEL syntax for this function is <code>eel.version()</code>
     * @param context   The Eel Context
     * @return the Eel version
     */
    @Nonnull
    @EelFunction(name = "eel.version")
    public String version(@Nonnull EelContext context) {
        return context.metadata().version();
    }


    /**
     * Entry point for the {@code eel.buildDate} function, which returns the date and time the EEL compiler was built
     * <br>
     * The EEL syntax for this function is <code>eel.buildDate()</code>
     * @param context   The Eel Context
     * @return the Eel build date
     */
    @Nonnull
    @EelFunction(name = "eel.buildDate")
    public ZonedDateTime buildDate(@Nonnull EelContext context) {
        return context.metadata().buildDate();
    }
}
