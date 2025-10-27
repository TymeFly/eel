package com.github.tymefly.eel.doc.model;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.utils.EelType;

/**
 * Getter functions for an EEL Function Model
 */
public interface FunctionModel extends ElementModel {
    /**
     * Returns the EEL name for this function
     * @return the EEL name for this function
     */
    @Nonnull
    String name();

    /**
     * Returns an object describing the group this function belongs to.
     * @return an object describing the group this function belongs to.
     */
    @Nonnull
    GroupModel group();

    /**
     * Returns the type returned by the function
     * @return the type returned by the function
     */
    @Nonnull
    Optional<EelType> type();

    /**
     * Returns the unique ID of this function. This could be different from the function name as it's
     * possible for the developer to annotate two or more functions with the same name.
     * @return the unique ID of this function
     */
    @Nonnull
    String uniqueId();

    /**
     * Returns the parameters passed to the EEL function in the order they are declared
     * @return the parameters passed to the EEL function in the order they are declared
     */
    @Nonnull
    List<ParamModel> parameters();

    /**
     * Returns the EEL signature for this function
     * @return the EEL signature for this function
     */
    @Nonnull
    String eelSignature();
}
