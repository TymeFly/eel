package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Getter functions for a text fragment
 */
public interface TextModel {
    /**
     * Returns the literal text to be rendered
     * @return the literal text to be rendered
     */
    @Nonnull
    String text();

    /**
     * Returns the style the text should be rendered
     * @return the style the text should be rendered
     */
    @Nonnull
    TextStyle style();

    /**
     * Returns the link associated with this text fragment, or {@literal null} if there is no link
     * @return the link associated with this text fragment, or {@literal null}
     */
    @Nullable
    String target();
}
