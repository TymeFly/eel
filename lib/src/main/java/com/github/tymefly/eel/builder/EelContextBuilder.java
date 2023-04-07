package com.github.tymefly.eel.builder;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * A fluent interface that is used to build {@link EelContext} objects
 */
public interface EelContextBuilder extends EelContextSettingBuilder<EelContextBuilder> {
    /**
     * Returns a new EEL Context object
     * @return a new EEL Context object
     */
    @Nonnull
    EelContext build();
}
