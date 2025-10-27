package com.github.tymefly.eel.doc.model;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue("hidden", new RealElement().hide().isHidden());
        Assert.assertFalse("not hidden", new RealElement().isHidden());
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

        Assert.assertEquals("SINCE", List.of(), element.tags(TagType.SINCE));
        Assert.assertEquals("RETURN", List.of(tag1), element.tags(TagType.RETURN));
        Assert.assertEquals("AUTHOR", List.of(tag2, tag3), element.tags(TagType.AUTHOR));
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

        Assert.assertFalse("no summary", element.hasSummary());
        Assert.assertEquals("missing summary", Optional.empty(), element.summary());

        element.addSummary(textBlock);

        Optional<TagModel> summary = element.summary();

        Assert.assertTrue("has summary", element.hasSummary());
        Assert.assertNotEquals("summary text", Optional.empty(), summary);

        TagModel summaryTag = summary.get();

        Assert.assertEquals("Unexpected tag type", TagType.SUMMARY, summaryTag.tagType());
        Assert.assertEquals("Unexpected tag reference", Optional.empty(), summaryTag.reference());
        Assert.assertNull("Unexpected tag target", summaryTag.target());
    }


    /**
     * Unit test {@link Element#deprecated}
     */
    @Test
    public void test_deprecated() {
        Element<?> element = new RealElement();

        Assert.assertEquals("nothing Deprecated", Optional.empty(), element.deprecated());

        Tag tag1 = element.addTag(TagType.RETURN);
        Tag tag2 = element.addTag(TagType.AUTHOR);
        Tag tag3 = element.addTag(TagType.DEPRECATED);
        Tag tag4 = element.addTag(TagType.DEPRECATED);                  // Only one tag is allowed

        Assert.assertEquals("Deprecated tag", Optional.of(tag3), element.deprecated());
    }
}