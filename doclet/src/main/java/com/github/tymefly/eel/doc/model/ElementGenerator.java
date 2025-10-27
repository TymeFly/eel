package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;

/**
 * Mutator functions used to describe an element in the generated documentation
 * @param <F>   the type of the fluent interface
 */
public interface ElementGenerator<F extends ElementGenerator<F>> extends ParagraphGenerator<F> {
    /**
     * Indicates that this element should not be added to the generated documentation.
     * @return a fluent interface
     */
    @Nonnull
    F hide();

    /**
     * Add a tag to this element
     * @param type  type of the tag
     * @return A mutator class used to further describe the tag
     */
    @Nonnull
    TagGenerator addTag(@Nonnull TagType type);

    /**
     * Add a summary to this element
     * @param summary   The summary that describes this element
     * @return A mutator class used to further describe the summary
     */
    @Nonnull
    TagGenerator addSummary(@Nonnull TextBlockGenerator summary);

    /**
     * Returns {@literal true} only if this element has a summary
     * @return {@literal true} only if this element has a summary
     * @see #addSummary(TextBlockGenerator)
     */
    boolean hasSummary();
}
