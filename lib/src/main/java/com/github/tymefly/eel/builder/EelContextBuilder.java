package com.github.tymefly.eel.builder;

import java.time.Duration;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * A fluent interface used to build {@link EelContext} objects.
 */
public interface EelContextBuilder extends EelContextSettingBuilder<EelContextBuilder> {
    /**
     * The default maximum length of an EEL expression.
     * @see #withMaxExpressionSize(int)
     */
    int DEFAULT_MAX_EXPRESSION_LENGTH = 1024;

    /**
     * The default maximum duration an EEL expression is allowed to evaluate.
     * @see #withTimeout(Duration)
     */
    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    /**
     * Indicates that no timeout is applied to EEL expression evaluation.
     * @see #withTimeout(Duration)
     */
    Duration NO_TIMEOUT = Duration.ofSeconds(0);

    /**
     * Creates a new {@link EelContext} based on the current builder configuration.
     * @return the constructed {@link EelContext} instance
     * @see EelContext
     */
    @Nonnull
    EelContext build();
}