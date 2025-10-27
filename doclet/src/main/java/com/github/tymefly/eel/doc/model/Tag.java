package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The model for a tagged block of text in the generated documentation. The meaning of the model depends on the tag
 */
class Tag extends Paragraph<TagGenerator> implements TagModel, TagGenerator {
    private final TagType tag;
    private String reference = null;
    private String target = null;


    Tag(@Nonnull TagType tag) {
        this.tag = tag;
    }


    @Override
    @Nonnull
    public TagGenerator withReference(@Nonnull String text, @Nullable String target) {
        this.reference = text;
        this.target = target;

        return this;
    }


    @Override
    @Nonnull
    public TagType tagType() {
        return tag;
    }

    @Override
    @Nonnull
    public Optional<String> reference() {
        return Optional.ofNullable(reference);
    }

    @Override
    @Nullable
    public String target() {
        return target;
    }
}
