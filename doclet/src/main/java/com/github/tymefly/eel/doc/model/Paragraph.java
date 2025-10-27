package com.github.tymefly.eel.doc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A model for a sequence of {@link TextModel} objects. All Models that contain text extend this class
 * @param <F>   type of the fluent interface
 */
abstract class Paragraph<F extends ParagraphGenerator<F>> implements ParagraphModel, ParagraphGenerator<F> {
    private final List<TextModel> text = new ArrayList<>();

    private TextStyle nextStyle = TextStyle.NONE;
    private boolean addSpace = false;

    private final F instance;

    @SuppressWarnings("unchecked")
    Paragraph() {
        instance = (F) this;
    }


    @Nonnull
    F instance() {
        return instance;
    }


    @Override
    @Nonnull
    public F withText(@Nonnull String text) {
        if ((nextStyle != null) && !text.isBlank()) {           // nextStyle == null if the text is ignored
            /* Sometimes text like:
                        abc {@code def} ghi
               is passed through the Doclet without a space between 'def' and 'ghi'. This also happens with
                        my description.
                        Start of the next line
               which can be passed through without a space or new line between 'description.' and "Start"

               In both cases we'll add a space to stop the words running into each other.
             */
            if (addSpace && (nextStyle != TextStyle.RAW)) {
                text = " " + text;
            }

            char last = text.charAt(text.length() - 1);
            addSpace = !Character.isSpaceChar(last) && (nextStyle == TextStyle.NONE);

            TextModel model = new Text(nextStyle, text);

            this.text.add(model);
        }

        this.nextStyle = TextStyle.NONE;

        return instance;
    }

    @Override
    @Nonnull
    public F withText(@Nonnull List<TextModel> text) {
        this.text.addAll(text);
        this.nextStyle = TextStyle.NONE;

        return instance;
    }

    @Override
    @Nonnull
    public F withCode() {
        nextStyle = TextStyle.CODE;

        return instance;
    }

    @Override
    @Nonnull
    public F withErrorHighlight() {
        nextStyle = TextStyle.ERROR;

        return instance;
    }


    @Override
    @Nonnull
    public F withLink(@Nonnull String text, @Nullable String target) {
        return withLink(text, target, TextStyle.LINK);
    }

    @Override
    public F withPlainLink(@Nonnull String text, @Nullable String target) {
        return withLink(text, target, TextStyle.PLAIN_LINK);
    }

    private F withLink(@Nonnull String text, @Nullable String target, @Nonnull TextStyle style) {
        if (!text.isBlank()) {
            TextModel model = new Text(style, text, target);

            this.text.add(model);
        }

        return instance;
    }

    @Override
    @Nonnull
    public F withLiteral() {
        nextStyle = TextStyle.LITERAL;

        return instance;
    }


    @Override
    @Nonnull
    public F withHtml(@Nonnull String text) {
        nextStyle = TextStyle.RAW;

        return withText(text);
    }


    @Override
    @Nonnull
    public F withIgnoredText() {
        nextStyle = null;

        return instance;
    }

    @Override
    @Nonnull
    public ParagraphGenerator<?> addIgnoredBlock() {
        return new TextBlock();           // not referenced by the model, so the data will be discarded
    }


    @Override
    @Nonnull
    public List<TextModel> text() {
        return Collections.unmodifiableList(text);
    }
}
