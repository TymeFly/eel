package com.github.tymefly.eel.doc.model;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Getter functions for a group of EEL functions
 */
public interface GroupModel extends ElementModel {
    /**
     * Returns the name of the group
     * @return the name of the group
     */
    @Nonnull
    String name();

    /**
     * Returns the functions in this group. Functions flagged with the @hidden tag are filtered out
     * @return the functions in this group.
     */
    @Nonnull
    Collection<FunctionModel> getFunctions();

    /**
     * Returns the name of the generated file for this group
     * @return the name of the generated file for this group
     */
    @Nonnull
    String fileName();

    /**
     * Returns {@literal true} only if this group has a description
     * @return {@literal true} only if this group has a description
     */
    boolean hasDescription();
}
