package com.github.tymefly.eel.function.general;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.FunctionalResource;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL function that returns the next value in a named counter.
 * By default, the first value returned is {@literal 0}.
 * Counters are context based; if the same context is shared across multiple expressions or if there are multiple
 * invocations of the same expression then the value will continue to increment.
 * To reset the count back to its initial value then recompile the expression with a new {@link EelContext}.
 * <br>
 * The EEL syntax for this function is:
 * <ul>
 *  <li><code>count()</code></li>
 *  <li><code>count( counterName )</code></li>
 * </ul>
 */
@PackagedEelFunction
public class Count {
    /** Default counter-name */
    public static final String DEFAULT_COUNTER = "";

    /**
     * Entry point for the {@code count} function
     * @param manager       resource manager for Counters
     * @param name          Name of the counter. Empty string denotes the default anonymous counter
     * @return              The next value in an incremental counter
     */
    @EelFunction("count")
    public long count(@Nonnull FunctionalResource manager,
                      @DefaultArgument(DEFAULT_COUNTER) @Nonnull String name) {
        Map<String, AtomicLong> counters =
            manager.getResource(name, n -> Collections.synchronizedMap(new HashMap<>()));
        long count = counters.computeIfAbsent(name, k -> new AtomicLong())
            .getAndIncrement();

        return count;
    }
}
