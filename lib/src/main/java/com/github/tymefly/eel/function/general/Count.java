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
 * Counters are context-based: if the same context is shared across multiple expressions,
 * or if there are multiple invocations of the same expression, the value will continue to increment.
 * To reset the counter to its initial value, recompile the expression with a new {@link EelContext}.
 */
@PackagedEelFunction
public class Count {
    /** Default counter-name */
    public static final String DEFAULT_COUNTER = "";

    /**
     * Returns the next integer value in an incrementing counter. Names are used to differentiate counters.
     * The first time this function is called, the value {@literal 0} is returned.
     * @param manager   resource manager for Counters
     * @param name      The unique name of the counter.
     *                  If the name is empty then the default anonymous counter is used.
     * @return          The next value in an incremental counter
     */
    @EelFunction("count")
    public long count(@Nonnull FunctionalResource manager,
                      @DefaultArgument(value = DEFAULT_COUNTER,
                                       description = "empty text, denoting the default counter") @Nonnull String name) {
        Map<String, AtomicLong> counters =
            manager.getResource(name, n -> Collections.synchronizedMap(new HashMap<>()));
        long count = counters.computeIfAbsent(name, k -> new AtomicLong())
            .getAndIncrement();

        return count;
    }
}
