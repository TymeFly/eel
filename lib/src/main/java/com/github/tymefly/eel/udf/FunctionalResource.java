package com.github.tymefly.eel.udf;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * UDFs can accept {@link FunctionalResource} objects to access managed resources on a per-implementing-class
 * and per-{@link EelContext} basis. Because EEL functions must be stateless, classes that implement UDFs should use
 * a {@link FunctionalResource} to persist resources (typically data) across invocations.
 * <br/>
 * Managed resources relate to a specific instance of {@link EelContext}, so if the EEL expression is recompiled
 * with a new context, a new set of resources is allocated. If another expression is compiled with an existing
 * context, the resources are reused.
 * <br/>
 * Managed resources also relate to the class implementing the {@link com.github.tymefly.eel.udf.EelFunction}.
 * If the class implements multiple {@link EelFunction} methods, they share the same resources. However, UDF
 * resources are hidden from {@link EelFunction} methods in different classes.
 * @since 2.0
 */
public interface FunctionalResource {

    /**
     * Returns the named resource.
     * @param name     the name of the resource
     * @param factory  function used to create the resource if it does not already exist. Typically, a constructor
     *                 or a factory method
     * @param <T>      the type of the named resource
     * @return         the named resource
     */
    @Nonnull
    <T> T getResource(@Nonnull String name, @Nonnull Function<String, T> factory);
}