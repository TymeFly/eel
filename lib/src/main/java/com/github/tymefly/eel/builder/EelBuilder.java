package com.github.tymefly.eel.builder;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * Fluent interface for building expressions.
 */
public interface EelBuilder extends FluentEelBuilder {
    /**
     * Adds an {@link EelContext} to this expression.
     * After this method has been called, no further modifications to the {@link EelContext} are permitted.
     * @param context   the pre-generated {@link EelContext} to associate with this expression
     * @return          a fluent interface for completing the builder configuration
     * @see EelContext#factory()
     */
    @Nonnull
    EelBuilderComplete withContext(@Nonnull EelContext context);
}
