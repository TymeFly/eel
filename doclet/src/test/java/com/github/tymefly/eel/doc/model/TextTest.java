package com.github.tymefly.eel.doc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals("", empty.text(), "empty");
        assertEquals("Hello World", noLink.text(), "noLink");
        assertEquals("linked text", withLink.text(), "withLink");
    }

    /**
     * Unit test {@link Text#style()}
     */
    @Test
    public void test_style() {
        assertEquals(TextStyle.NONE, empty.style(), "empty");
        assertEquals(TextStyle.RAW, noLink.style(), "noLink");
        assertEquals(TextStyle.CODE, withLink.style(), "withLink");
    }

    /**
     * Unit test {@link Text#target()}
     */
    @Test
    public void test_target() {
        assertNull(empty.target(), "empty");
        assertNull(noLink.target(), "noLink");
        assertEquals("test.Test1.formatOctal(java.math.BigInteger)", withLink.target(), "withLink");
    }


    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_controlChars() {
        assertEquals(
            " some text ",
            new Text(TextStyle.RAW, " some text ").text(),
            "leading spaces: No change");
        assertEquals(
            "start end",
            new Text(TextStyle.RAW,  "start\t\tend").text(),
            "remove tabs");
        assertEquals(
            "line1 line2 line3 ",
            new Text(TextStyle.RAW, "line1\rline2\nline3\n").text(),
            "Line Feed");
    }

    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_escapes() {
        assertEquals(
            "And: &, Less <, Greater >",
            new Text(TextStyle.RAW, "And: &, Less <, Greater >").text(),
            "Resolve Escapes disabled");
        assertEquals(
            "And: &amp;, Less &lt;, Greater &gt;",
            new Text(TextStyle.LINK, "And: &, Less <, Greater >").text(),
            "Resolves Escapes enabled");
    }

    /**
     * Unit test {@link Text#text()}
     */
    @Test
    public void test_cleanText_clean() {
        assertEquals(
            "Some     Spaces",
            new Text(TextStyle.LITERAL, "Some     Spaces").text(),
            "Clean disabled");
        assertEquals(
            "Some Spaces",
            new Text(TextStyle.RAW, "Some     Spaces").text(),
            "Clean enabled");
    }
}