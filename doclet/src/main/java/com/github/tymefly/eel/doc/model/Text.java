package com.github.tymefly.eel.doc.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Model a single styled text fragment.
 */
@Immutable
class Text implements TextModel {
    private final TextStyle style;
    private final String text;
    private final String target;

    Text(@Nonnull TextStyle style, @Nonnull String text) {
        this(style, text, null);
    }

    Text(@Nonnull TextStyle style, @Nonnull String text, @Nullable String target) {
        text = cleanText(style, text);

        this.style = style;
        this.text = text;
        this.target = target;
    }


    @Override
    @Nonnull
    public String text() {
        return text;
    }

    @Override
    @Nonnull
    public TextStyle style() {
        return style;
    }

    @Override
    @Nullable
    public String target() {
        return target;
    }


    @Nonnull
    private String cleanText(@Nonnull TextStyle style, @Nonnull String text) {
        if (!text.isEmpty()) {
            text = text.replaceAll("[\r\n]", " ")
                .replaceAll("\t+", " ");

            if (style.escape()) {
                text = text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
            }

            if (style.clean()) {
                text = text.replaceAll(" +", " ");
            }
        }

        return text;
    }
}
