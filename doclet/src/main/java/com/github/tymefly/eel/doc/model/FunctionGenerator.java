package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.source.Parameter;

/**
 * Mutator functions for the Function model
 */
public interface FunctionGenerator extends ElementGenerator<FunctionGenerator> {
    /**
     * Add a new parameter to this EEL function.
     * If identifier has already been defined then return the existing parameter
     * @param identifier    Name of the parameter as seen in the JavaDoc
     * @param parameter     The parameter description. This could be {@literal null} if a parameter is
     *                      documented but not defined in the Java code
     * @return A mutator class used to further describe the parameter
     */
    @Nonnull
    ParamGenerator addParameter(@Nonnull String identifier, @Nullable Parameter parameter);
}
