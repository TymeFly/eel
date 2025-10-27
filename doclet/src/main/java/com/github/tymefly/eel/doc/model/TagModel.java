package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Getter functions for the Tag Model
 */
public interface TagModel extends ParagraphModel {
    /**
     * Returns the type of the tag
     * @return the type of the tag
     */
    @Nonnull
    TagType tagType();

    /**
     * If this tag model represents a description of a @throws or @see tag, then this method will return signature
     * of the referenced element
     * @return the signature of the referenced element or {@literal null} if there is no reference
     */
    @Nonnull
    Optional<String> reference();

    /**
     * If this tag model represents a link, then this method will return the signature of the destination element
     * @return the signature of the linked element or {@literal null} if there is no link
     */
    @Nullable
    String target();
}
