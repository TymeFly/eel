package com.github.tymefly.eel.builder;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * Fluent interface for building expressions
 */
public interface EelBuilder extends EelBuilderComplete, EelContextSettingBuilder<EelBuilder> {
    /**
     * Add an EEL Context to this Expression
     * @param context   pre-generated EEL Context
     * @return          A fluent interface
     * @see EelContext#factory()
     */
    @Nonnull
    EelBuilderComplete withContext(@Nonnull EelContext context);

}
