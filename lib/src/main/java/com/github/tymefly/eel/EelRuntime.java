package com.github.tymefly.eel;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelException;
import com.github.tymefly.eel.exception.EelInternalException;
import com.github.tymefly.eel.exception.EelInterruptedException;
import com.github.tymefly.eel.exception.EelRuntimeException;
import com.github.tymefly.eel.exception.EelTimeoutException;

/**
 * Wrap an Executor in the EEL Runtime
 */
class EelRuntime {
    private final EelContextImpl context;

    EelRuntime(@Nonnull EelContextImpl context) {
        this.context = context;
    }


    @Nonnull
    Expression wrap(@Nonnull Term wrapped) {
        Duration timeout = context.getTimeout();
        boolean skipTimeOut = (timeout.isZero() || wrapped.isConstant());
        Expression expression = (skipTimeOut ? withoutTimeout(wrapped) : withTimeout(wrapped, timeout));

        return expression;
    }


    @Nonnull
    private Expression withoutTimeout(@Nonnull Term wrapped) {
        return s -> {
            try {
                return execute(wrapped, s);
            } catch (EelException e) {
                throw e;
            } catch (Exception e) {
                throw new EelRuntimeException("EEL execution failed", e);
            }
        };
    }

    @Nonnull
    private Expression withTimeout(@Nonnull Term wrapped, @Nonnull Duration timeout) {
        return s -> {
            Instant start = Instant.now();
            CompletableFuture<Result> future = CompletableFuture.supplyAsync(() -> execute(wrapped, s))
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

            try {
                return future.get();
            } catch (InterruptedException e) {
                throw new EelInterruptedException("EEL execution was interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();

                if (cause instanceof TimeoutException) {
                    long duration = ChronoUnit.SECONDS.between(start, Instant.now());

                    throw new EelTimeoutException("EEL Timeout after %d second(s)", duration);
                } else if (cause instanceof EelException eelException) {
                    throw eelException;
                } else {
                    throw new EelRuntimeException("EEL execution failed", cause);
                }
            }
        };
    }

    @Nonnull
    private static Result execute(@Nonnull Term wrapped, @Nonnull SymbolsTable symbols) {
        Value value = wrapped.evaluate(symbols);

        if (value instanceof ValueArgument executor) {
            value = executor.evaluate(symbols);
        }

        if (value instanceof Constant constant) {
            return constant;
        } else {                // Should not happen
            throw new EelInternalException("Unexpected type evaluated: " + value.getClass().getName());
        }
    }
}
