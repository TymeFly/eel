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
    Executor apply(@Nonnull Executor wrapped) {
        Executor result = handleExceptions(wrapped);

        if (!context.getTimeout().isZero()) {
            result = withTimeout(result);
        }

        return result;
    }


    @Nonnull
    private Executor handleExceptions(@Nonnull Executor wrapped) {
        return s -> {
            try {
                return wrapped.execute(s);
            } catch (EelException e) {
                throw e;
            } catch (Exception e) {
                throw new EelRuntimeException("EEL execution failed", e);
            }
        };
    }


    @Nonnull
    private Executor withTimeout(@Nonnull Executor wrapped) {
        Duration timeout = context.getTimeout();

        return s -> {
            Instant start = Instant.now();
            CompletableFuture<Value> future = CompletableFuture.supplyAsync(() -> wrapped.execute(s))
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
                } else {
                    throw (EelException) cause;         // handleExceptions() ensures this case will work
                }
            }
        };
    }
}
