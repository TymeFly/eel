package com.github.tymefly.eel.doc.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Paragraph}
 */
public class ParagraphTest {
    private static class RealParagraph extends Paragraph<RealParagraph> {
    }


    private Paragraph<?> paragraph;


    @Before
    public void setUp() {
        paragraph = new RealParagraph();
    }

    /**
     * Unit test {@link Paragraph#instance()}
     */
    @Test
    public void test_instance() {
        Assert.assertSame("Unexpected instance", paragraph, paragraph.instance());
    }

    /**
     * Unit test {@link Paragraph#withText(String)}
     */
    @Test
    public void test_withText() {
        paragraph.withText("Hello World");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "Hello World", TextStyle.NONE, null);
    }

    /**
     * Unit test {@link Paragraph#withText(List)}
     */
    @Test
    public void test_withText_multipleLines() {
        Text line1 = new Text(TextStyle.NONE, "First");
        Text line2 = new Text(TextStyle.LITERAL, "Second");
        Text line3 = new Text(TextStyle.CODE, "Third");

        paragraph.withText(List.of(line1, line2, line3));

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 3, actual.size());
        assertText(actual, 0, "First", TextStyle.NONE, null);
        assertText(actual, 1, "Second", TextStyle.LITERAL, null);
        assertText(actual, 2, "Third", TextStyle.CODE, null);
    }

    /**
     * Unit test {@link Paragraph#withCode()}
     */
    @Test
    public void test_withCode() {
        paragraph.withCode()
            .withText("some code");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "some code", TextStyle.CODE, null);
    }

    /**
     * Unit test {@link Paragraph#withErrorHighlight()}
     */
    @Test
    public void test_withErrorHighlight() {
        paragraph.withErrorHighlight()
            .withText("error message");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "error message", TextStyle.ERROR, null);
    }

    /**
     * Unit test {@link Paragraph#withLink(String, String)}
     */
    @Test
    public void test_withLink() {
        paragraph.withLink("link text", "link target");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "link text", TextStyle.LINK, actual.get(0).target());
    }

    /**
     * Unit test {@link Paragraph#withLink(String, String)}
     */
    @Test
    public void test_withLink_empty() {
        paragraph.withLink("", "link target");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 0, actual.size());
    }

    /**
     * Unit test {@link Paragraph#withPlainLink(String, String)}
     */
    @Test
    public void test_withPlainLink() {
        paragraph.withPlainLink("link text", "link target");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "link text", TextStyle.PLAIN_LINK, actual.get(0).target());
    }

    /**
     * Unit test {@link Paragraph#withLiteral()}
     */
    @Test
    public void test_withLiteral() {
        paragraph.withLiteral()
            .withText("literal text");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "literal text", TextStyle.LITERAL, null);
    }

    /**
     * Unit test {@link Paragraph#withHtml(String)}
     */
    @Test
    public void test_withHtml() {
        paragraph.withHtml("<p>");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 1, actual.size());
        assertText(actual, 0, "<p>", TextStyle.RAW, null);
    }


    /**
     * Unit test {@link Paragraph#withIgnoredText()}
     */
    @Test
    public void test_withIgnoredText() {
        paragraph.withIgnoredText()
            .withText("ignored text");

        List<TextModel> actual = paragraph.text();

        Assert.assertEquals("unexpected number of elements", 0, actual.size());
    }

    /**
     * Unit test {@link Paragraph#addIgnoredBlock()}
     */
    @Test
    public void test_addIgnoredBlock() {
        ParagraphGenerator<?> next = paragraph.addIgnoredBlock()
            .withText("ignored text");

        List<TextModel> actual = paragraph.text();

        Assert.assertNotSame("wrong block returned", paragraph, next);
        Assert.assertEquals("paragraph has unexpected number of elements", 0, actual.size());
    }


    /**
     * Unit test {@link Paragraph#addIgnoredBlock()}
     */
    @Test
    public void test_multipleFragments() {
        ParagraphGenerator<?> next = paragraph.withText("start with plain")            // Add a space
            .withCode().withText("code")
            .withHtml("<br>")
            .withText("plain again")
            .withText("")
            .withIgnoredText().withText("ignored text")
            .withLink("link to", "#myElement")
            .withText("more text");

        List<TextModel> actual = paragraph.text();

        Assert.assertSame("Broken fluent interface", paragraph, next);
        Assert.assertEquals("unexpected number of elements", 6, actual.size());
        assertText(actual, 0, "start with plain", TextStyle.NONE, null);
        assertText(actual, 1, " code", TextStyle.CODE, null);
        assertText(actual, 2, "<br>", TextStyle.RAW, null);
        assertText(actual, 3, "plain again", TextStyle.NONE, null);
        assertText(actual, 4, "link to", TextStyle.LINK, "#myElement");
        assertText(actual, 5, " more text", TextStyle.NONE, null);
    }


    private void assertText(@Nonnull List<TextModel> actual,
                            int index,
                            @Nonnull String text,
                            @Nonnull TextStyle style,
                            @Nullable String target) {
        Assert.assertEquals(index + ": unexpected text", text, actual.get(index).text());
        Assert.assertEquals(index + ": unexpected style", style, actual.get(index).style());
        Assert.assertEquals(index + ": unexpected target", target, actual.get(index).target());
    }
}