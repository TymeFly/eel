package com.github.tymefly.eel.doc.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base interface that defines the contract used to mutate models that contain text
 * @param <F>       the fluent interface type
 */
public interface ParagraphGenerator<F extends ParagraphGenerator<F>> {
    /**
     * Append an ordered collection of text models to this paragraph
     * @param text      an ordered collection of text models
     * @return  a fluent interface
     */
    @Nonnull
    F withText(@Nonnull List<TextModel> text);

    /**
     * Add a single block of text to this paragraph. Unless otherwise indicated, the style of this text
     * will be {@link TextStyle#NONE}
     * @param text      The literal text to render
     * @return  a fluent interface
     * @see #withLiteral()
     * @see #withCode()
     * @see #withErrorHighlight()
     */
    @Nonnull
    F withText(@Nonnull String text);

    /**
     * Add a block of HTML text to this paragraph
     * @param text  the HTML text
     * @return  a fluent interface
     */
    @Nonnull
    F withHtml(@Nonnull String text);

    /**
     * Add a Link to this paragraph that should be rendered as a link
     * @param text      the text of the link
     * @param target    the destination for the link
     * @return  a fluent interface
     */
    @Nonnull
    F withLink(@Nonnull String text, @Nullable String target);

    /**
     * Add a Link to this paragraph that should be rendered as plain text
     * @param text      the text of the link
     * @param target    the destination for the link
     * @return  a fluent interface
     */
    F withPlainLink(@Nonnull String text, @Nullable String target);

    /**
     * Indicates that the next TextModel added to this paragraph should be highlighted as a literal
     * @return  a fluent interface
     * @see TextStyle#LITERAL
     */
    @Nonnull
    F withLiteral();

    /**
     * Indicates that the next TextModel added to this paragraph should be highlighted as a code block
     * @return  a fluent interface
     * @see TextStyle#CODE
     */
    @Nonnull
    F withCode();

    /**
     * Indicates that the next TextModel added to this paragraph should be highlighted as an error
     * @return  a fluent interface
     * @see TextStyle#ERROR
     */
    @Nonnull
    F withErrorHighlight();

    /**
     * Indicates that the next TextModel added to this paragraph should be ignored
     * @return  a fluent interface
     */
    @Nonnull
    F withIgnoredText();

    /**
     * Create a new paragraph that will be ignored by the generator.
     * @return  a fluent interface
     */
    @Nonnull
    ParagraphGenerator<?> addIgnoredBlock();
}
