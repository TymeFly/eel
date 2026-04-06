package com.github.tymefly.eel.builder;

import java.io.InputStream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;

/**
 * Part of the fluent interface for building {@link Eel} objects. This interface contains
 * the methods required to complete the expression.
 */
public interface EelBuilderComplete {

    /**
     * Compiles an expression defined in the {@code expression} string.
     * @param expression  the expression to compile
     * @return            the compiled {@link Eel} expression
     * @see Eel
     */
    @Nonnull
    Eel compile(@Nonnull String expression);

    /**
     * Compiles an expression defined in the {@code expression} stream.
     * @param expression  the input stream containing the expression to compile
     * @return            the compiled {@link Eel} expression
     * @see Eel
     */
    @Nonnull
    Eel compile(@Nonnull InputStream expression);
}
