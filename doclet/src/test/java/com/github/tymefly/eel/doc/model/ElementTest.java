package com.github.tymefly.eel.doc.model;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Element}
 */
public class ElementTest {
    private static class RealElement extends Element<RealElement> {
    }

    /**
     * Unit test {@link Element#hide()} and {@link Element#isHidden}
     */
    @Test
    public void test_hide() {
        assertTrue(new RealElement().hide().isHidden(), "hidden");
        assertFalse(new RealElement().isHidden(), "not hidden");
    }

    /**
     * Unit test {@link Element#addTag(TagType)} and {@link Element#tags(TagType)}
     */
    @Test
    public void test_addTag() {
        Element<?> element = new RealElement();
        Tag tag1 = element.addTag(TagType.RETURN);
        Tag tag2 = element.addTag(TagType.AUTHOR);
        Tag tag3 = element.addTag(TagType.AUTHOR);
        Tag tag4 = element.addTag(TagType.RETURN);         // Ignored - only one RETURN element is allowed

        assertEquals(List.of(), element.tags(TagType.SINCE), "SINCE");
        assertEquals(List.of(tag1), element.tags(TagType.RETURN), "RETURN");
        assertEquals(List.of(tag2, tag3), element.tags(TagType.AUTHOR), "AUTHOR");
    }

    /**
     * Unit test {@link Element#addSummary(TextBlockGenerator)}, {@link Element#hasSummary()}
     * and {@link Element#summary()}
     */
    @Test
    public void test_addSummary() {
        Element<?> element = new RealElement();
        TextBlockGenerator textBlock = new TextBlock()
            .withText("Hello")
            .withCode().withText("World");

        assertFalse(element.hasSummary(), "no summary");
        assertEquals(Optional.empty(), element.summary(), "missing summary");

        element.addSummary(textBlock);

        Optional<TagModel> summary = element.summary();

        assertTrue(element.hasSummary(), "has summary");
        assertNotEquals(Optional.empty(), summary, "summary text");

        TagModel summaryTag = summary.get();

        assertEquals(TagType.SUMMARY, summaryTag.tagType(), "Unexpected tag type");
        assertEquals(Optional.empty(), summaryTag.reference(), "Unexpected tag reference");
        assertNull(summaryTag.target(), "Unexpected tag target");
    }

    /**
     * Unit test {@link Element#deprecated}
     */
    @Test
    public void test_deprecated() {
        Element<?> element = new RealElement();

        assertEquals(Optional.empty(), element.deprecated(), "nothing Deprecated");

        Tag tag1 = element.addTag(TagType.RETURN);
        Tag tag2 = element.addTag(TagType.AUTHOR);
        Tag tag3 = element.addTag(TagType.DEPRECATED);
        Tag tag4 = element.addTag(TagType.DEPRECATED);                  // Only one tag is allowed

        assertEquals(Optional.of(tag3), element.deprecated(), "Deprecated tag");
    }
}
