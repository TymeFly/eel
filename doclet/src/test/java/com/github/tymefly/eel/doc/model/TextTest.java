package com.github.tymefly.eel.doc.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Text}
 */
public class TextTest {
    private final Text empty = new Text(TextStyle.NONE, "");
    private final Text noLink = new Text(TextStyle.RAW, "Hello World");
    private final Text withLink = new Text(TextStyle.CODE, "linked text", "test.Test1.formatOctal(java.math.BigInteger)");

    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_text() {
        Assert.assertEquals("empty", "", empty.text());
        Assert.assertEquals("noLink", "Hello World", noLink.text());
        Assert.assertEquals("withLink", "linked text", withLink.text());
    }

    /**
     * Unit test {@link Text#style()}
     */
    @Test
    public void test_style() {
        Assert.assertEquals("empty", TextStyle.NONE, empty.style());
        Assert.assertEquals("noLink", TextStyle.RAW, noLink.style());
        Assert.assertEquals("withLink", TextStyle.CODE, withLink.style());
    }

    /**
     * Unit test {@link Text#target()}
     */
    @Test
    public void test_target() {
        Assert.assertNull("empty", empty.target());
        Assert.assertNull("noLink", noLink.target());
        Assert.assertEquals("withLink", "test.Test1.formatOctal(java.math.BigInteger)", withLink.target());
    }


    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_controlChars() {
        Assert.assertEquals("leading spaces: No change",
            " some text ",
            new Text(TextStyle.RAW, " some text ").text());
        Assert.assertEquals("remove tabs",
            "start end",
            new Text(TextStyle.RAW,  "start\t\tend").text());
        Assert.assertEquals("Line Feed",
            "line1 line2 line3 ",
            new Text(TextStyle.RAW, "line1\rline2\nline3\n").text());
    }

    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_escapes() {
        Assert.assertEquals("Resolve Escapes disabled",
            "And: &, Less <, Greater >",
            new Text(TextStyle.RAW, "And: &, Less <, Greater >").text());
        Assert.assertEquals("Resolves Escapes enabled",
            "And: &amp;, Less &lt;, Greater &gt;",
            new Text(TextStyle.LINK, "And: &, Less <, Greater >").text());
    }

    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_clean() {
        Assert.assertEquals("Clean disabled",
            "Some     Spaces",
            new Text(TextStyle.LITERAL, "Some     Spaces").text());
        Assert.assertEquals("Clean enabled",
            "Some Spaces",
            new Text(TextStyle.RAW, "Some     Spaces").text());
    }
}