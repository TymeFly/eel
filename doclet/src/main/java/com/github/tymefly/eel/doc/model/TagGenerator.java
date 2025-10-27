package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Mutator functions for the Tag model
 */
public interface TagGenerator extends ParagraphGenerator<TagGenerator> {
    /**
     * Add a reference to the Tag model
     * @param text      Displayed text
     * @param target    Signature of the referenced element
     * @return a fluent interface
     * @see TagModel#reference()
     */
    @Nonnull
    TagGenerator withReference(@Nonnull String text, @Nullable String target);
}
