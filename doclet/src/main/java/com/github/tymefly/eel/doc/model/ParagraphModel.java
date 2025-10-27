package com.github.tymefly.eel.doc.model;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base interface that defines the contract used to read models that contain text
 */
public interface ParagraphModel {
    /**
     * Returns the ordered collection of text models containing a description of an element
     * @return the ordered collection of text models containing a description of an element
     */
    @Nonnull
    List<TextModel> text();
}
