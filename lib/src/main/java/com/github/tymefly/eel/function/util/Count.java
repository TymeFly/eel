package com.github.tymefly.eel.function.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns the next value in a running counter.
 * The first value returned is {@literal 1}.
 * The count is context based; if the same context is shared across multiple expressions or if there are multiple
 * invocations of the same expression then the value will continue to increment.
 * If you need to reset the count back to its initial value then recompile the expression with a new {@link EelContext}.
 * <br>
 * The EEL syntax for this function is <code>count()</code>
 */
@PackagedEelFunction
public class Count {
    private static final Map<String, AtomicLong> COUNTERS = new HashMap<>();

    /**
     * Entry point for the {@code count} function
     * @param context   context for the current expression
     * @return          The next value in an incremental counter
     */
    @EelFunction(name = "count")
    public long count(@Nonnull EelContext context) {
        long count = COUNTERS.computeIfAbsent(context.contextId(), k -> new AtomicLong())
            .incrementAndGet();

        return count;
    }
}
