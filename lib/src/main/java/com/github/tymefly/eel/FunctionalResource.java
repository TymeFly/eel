package com.github.tymefly.eel;

import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * UDF's can accept FunctionalResource objects to access managed resources on a per implementing-class,
 * per-{@link EelContext} basis.
 * As EEL functions are required to be stateless, classes that implement UDFs must use a FunctionalResource object to
 * persist resources across invocations.
 * <br/>
 * The managed resources relate to a specific instance of a {@link EelContext}, so if the Eel expression is recompiled
 * with a new context then a new set of resources will be allocated. If another expression is compiled with an existing
 * context then the resources are reused.
 * <br/>
 * The managed resources also relate to the class that implements the {@link com.github.tymefly.eel.udf.EelFunction}.
 * If the implementing class supports multiple EelFunctions they can all share the same resources. However, UDFs
 * resource are protected from functions that are in different classes.
 */
public interface FunctionalResource {
    /**
     * Returns the named resource.
     * @param name              the named of the resource.
     * @param constructor       function used to create the resource if it doesn't already exist.
     *                              Typically, this will be either constructor or a factory method.
     * @param <T>               the type of the named resource
     * @return the named resource.
     */
    @Nonnull
    <T> T getResource(@Nonnull String name, @Nonnull Function<String, T> constructor);
}
