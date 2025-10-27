package com.github.tymefly.eel.doc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Describes a single element in the generated documentation
 * @param <F>       Type of the fluent interface
 */
abstract class Element<F extends ElementGenerator<F>> extends Paragraph<F>
        implements ElementModel, ElementGenerator<F> {
    private final Map<TagType, List<Tag>> tags;

    private boolean hidden = false;

    Element() {
        this.tags = new EnumMap<>(TagType.class);
    }


    @Override
    @Nonnull
    public F hide() {
        hidden = true;

        return instance();
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }


    @Override
    @Nonnull
    public Tag addTag(@Nonnull TagType type) {
        List<Tag> sections = this.tags.computeIfAbsent(type, t -> new ArrayList<>());
        Tag section = new Tag(type);

        if (sections.size() < type.cardinality().getMaxValue()) {   // Discard TagModels beyond the type's cardinality
            sections.add(section);
        }

        return section;
    }

    @Override
    @Nonnull
    public List<TagModel> tags(@Nonnull TagType type) {
        return tags.getOrDefault(type, Collections.emptyList())
            .stream()
            .map(t -> (TagModel) t)
            .toList();
    }


    @Override
    @Nonnull
    public Tag addSummary(@Nonnull TextBlockGenerator section) {
        Tag summary = new Tag(TagType.SUMMARY);
        List<TextModel> text = ((TextBlock) section).text();

        summary.withText(text);
        tags.put(TagType.SUMMARY, List.of(summary));

        return summary;
    }


    @Override
    public boolean hasSummary() {
        return summary().isPresent();
    }


    @Override
    @Nonnull
    public Optional<TagModel> summary() {
        List<Tag> elements = this.tags.get(TagType.SUMMARY);
        boolean hasSummary = ((elements != null) && !elements.isEmpty());
        Optional<TagModel> summary = Optional.ofNullable(hasSummary ? elements.get(0) : null);

        return summary;
    }


    @Override
    @Nonnull
    public Optional<TagModel> deprecated() {
        List<Tag> sections = this.tags.get(TagType.DEPRECATED);
        boolean isDeprecated = ((sections != null) && !sections.isEmpty());
        Optional<TagModel> tag = Optional.ofNullable(isDeprecated ? sections.get(0) : null);

        return tag;
    }
}
