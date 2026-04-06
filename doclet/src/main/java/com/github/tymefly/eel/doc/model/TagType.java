package com.github.tymefly.eel.doc.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.config.Config;

/**
 * The supported types used by the Tag model.
 * These are listed in the order they are displayed in the generated documentation
 */
public enum TagType {
    SUMMARY("Summary", Cardinality.SINGLETON, false),
    DEPRECATED("Deprecated", Cardinality.SINGLETON, false),

    RETURN("Returns", Cardinality.SINGLETON, true),
    THROWS("Throws", Cardinality.UNBOUNDED, true),
    SEE("See", Cardinality.UNBOUNDED, true, Helper.ENABLED, () -> Config.getInstance().allReferences()),
    SINCE("Since", Cardinality.SINGLETON, true),
    VERSION("Version", Cardinality.SINGLETON, true),
    AUTHOR("Author", Cardinality.UNBOUNDED, true, () -> Config.getInstance().author(), Helper.ENABLED);

    /** The cardinality of a tag is the maximum number of times that the tag can be displayed for each block */
    enum Cardinality {
        SINGLETON(1),
        UNBOUNDED(Integer.MAX_VALUE);

        private final int maxValue;

        Cardinality(int maxValue) {
            this.maxValue = maxValue;
        }

        int getMaxValue() {
            return maxValue;
        }
    }

    private static class Helper {
        private static final Supplier<Boolean> ENABLED = () -> true;
    }

    private static final List<TagType> SECTIONS = Arrays.stream(values())
        .filter(tag -> tag.isSection)
        .toList();

    private final String tagName;
    private final Cardinality cardinality;
    private final boolean isSection;
    private final Supplier<Boolean> enabled;
    private final Supplier<Boolean> showAllReference;


    TagType(@Nonnull String tagName, @Nonnull Cardinality cardinality, boolean isSection) {
        this(tagName, cardinality, isSection, Helper.ENABLED, Helper.ENABLED);
    }


    TagType(@Nonnull String tagName,
            @Nonnull Cardinality cardinality,
            boolean isSection,
            @Nonnull Supplier<Boolean> enabled,
            @Nonnull Supplier<Boolean> showAllReference) {
        this.tagName = tagName;
        this.cardinality = cardinality;
        this.isSection = isSection;
        this.enabled = enabled;
        this.showAllReference = showAllReference;
    }

    /**
     * Returns all the TagTypes that are displayed their own named sections
     * @return all the TagTypes that are displayed their own named sections
     */
    @Nonnull
    public static List<TagType> sections() {
        return List.copyOf(SECTIONS);
    }

    /**
     * Returns the number of times this tag can be documented in a single block
     * @return the number of times this tag can be documented in a single block
     */
    @Nonnull
    Cardinality cardinality() {
        return cardinality;
    }

    /**
     * Returns {@literal true} only if this tag should be documented
     * @return {@literal true} only if this tag should be documented
     */
    public boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Returns {@literal true} only if all references, including broken references, are to be documented
     * @return {@literal true} only if all references, including broken references, are to be documented
     */
    public boolean showAllReference() {
        return showAllReference.get();
    }


    @Override
    @Nonnull
    public String toString() {
        return tagName;
    }
}
