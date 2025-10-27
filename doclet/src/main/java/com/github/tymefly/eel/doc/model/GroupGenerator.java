package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;

/**
 * Mutator functions for the Group model
 */
public interface GroupGenerator extends ElementGenerator<GroupGenerator> {
    /**
     * Flags this group as having a text description.
     * @return  a fluent interface
     */
    @Nonnull
    GroupGenerator withDescription();

    /**
     * Returns {@literal true} only if this group has a description
     * @return {@literal true} only if this group has a description
     */
    boolean hasDescription();
}
