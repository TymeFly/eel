package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

/**
 * Provides information about the EEL build
 * @since 2.0.0
 */
public interface Metadata {

    /**
     * Returns the EEL version
     * @return the EEL version
     * @since 2.0.0
     */
    @Nonnull
    String version();

    /**
     * Returns the EEL build date
     * @return the EEL build date
     * @since 2.0.0
     */
    @Nonnull
    ZonedDateTime buildDate();
}
