package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.utils.EelType;

/**
 * Getter functions for an EEL Function parameter Model
 */
public interface ParamModel extends ParagraphModel {
    /**
     * Returns the name of the parameter.
     * @return the name of the parameter.
     */
    @Nonnull
    String identifier();

    /**
     * Returns {@literal true} only if Javadoc implemented the parameter; {@literal false} implies that
     * the parameter was defined by Javadoc but was not implemented in the Java code
     * @return {@literal true} only if Javadoc implemented the parameter
     */
    boolean isParameter();

    /**
     * Returns {@literal true} only if the parameter is varArgs
     * @return {@literal true} only if the parameter is varArgs
     */
    boolean isVarArgs();

    /**
     * Returns the EEL type of the parameter. If the Optional is empty then the parameter isn't used by EEL
     * @return the EEL type of the parameter.
     */
    @Nonnull
    Optional<EelType> type();

    /**
     * Returns the 0-based order of the parameter in the parameter list
     * @return the 0-based order of the parameter in the parameter list
     */
    int order();

    /**
     * Returns {@literal true} only if the parameter has a default value
     * @return {@literal true} only if the parameter has a default value
     */
    boolean isDefaulted();

    /**
     * Returns the optional default value for the EEL parameter
     * @return the optional default value for the EEL parameter
     */
    @Nonnull
    Optional<String> defaultDescription();
}
