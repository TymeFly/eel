package com.github.tymefly.eel;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
        Duration timeout = context.getTimeout();
        Executor result = (timeout.isZero() ? withoutTimeout(wrapped) : withTimeout(wrapped, timeout));

        return result;
    }


    @Nonnull
    private Executor withoutTimeout(@Nonnull Executor wrapped) {
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
    private Executor withTimeout(@Nonnull Executor wrapped, @Nonnull Duration timeout) {
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
                } else if (cause instanceof EelException eelException) {
                    // It is more important to the client that the stack trace reports where EEL was called rather than
                    // the EEL internals, which is largely a timer thread calling anonymous lambda functions

                    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                    stack = Arrays.copyOfRange(stack, 1, stack.length);     // remove call to getStackTrace()

                    eelException.setStackTrace(stack);

                    throw eelException;
                } else {
                    throw new EelRuntimeException("EEL execution failed", cause);
                }
            }
        };
    }
}
