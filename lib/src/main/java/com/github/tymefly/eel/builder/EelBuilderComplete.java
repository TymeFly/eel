package com.github.tymefly.eel.builder;

import java.io.InputStream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;

/**
 * Part of the Fluent interface for building {@link Eel} objects. This interface contains just
 * the methods used to complete the Expression
 */
public interface EelBuilderComplete {

    /**
     * Compile an Expression as defined in the {@code expression} string
     * @param expression    The expression to compile
     * @return              A compiled expression
     */
    @Nonnull
    Eel compile(@Nonnull String expression);

    /**
     * Compile an Expression as defined in the {@code expression} stream
     * @param expression    The expression to compile
     * @return              A compiled expression
     */
    @Nonnull
    Eel compile(@Nonnull InputStream expression);
}
