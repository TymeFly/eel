package com.github.tymefly.eel.doc.model;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Getter functions for a single element
 */
public interface ElementModel extends ParagraphModel {
    /**
     * Returns {@literal true} only if the block has been marked as hidden by the @hidden tag in the Javadoc.
     * @return {@literal true} only if the block has been marked as hidden.
     */
    boolean isHidden();

    /**
     * Returns all the tags defined for this model for a given {@code type}
     * @param type  the type of the tag to return
     * @return all the tags defined for this model for a given {@code type}
     */
    @Nonnull
    List<TagModel> tags(@Nonnull TagType type);

    /**
     * Returns the optional summary tag model
     * @return the optional summary tag model
     */
    @Nonnull
    Optional<TagModel> summary();

    /**
     * Returns the optional deprecated tag model
     * @return the optional deprecated tag model
     */
    @Nonnull
    Optional<TagModel> deprecated();
}
