package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

/**
 * Provides information about the EEL build.
 * @since 2.0
 */
public interface Metadata {

    /**
     * Returns the version of EEL.
     * @return the EEL version
     * @since 2.0
     */
    @Nonnull
    String version();

    /**
     * Returns the date on which EEL was built.
     * @return the EEL build date
     * @since 2.0
     */
    @Nonnull
    ZonedDateTime buildDate();
}
