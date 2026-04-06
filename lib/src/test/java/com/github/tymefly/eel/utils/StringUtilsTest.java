package com.github.tymefly.eel.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link StringUtils}
 */
public class StringUtilsTest {

    /**
     * Unit test {@link StringUtils#toTitleCase(String)}
     */
    @Test
    public void test_toTitleCase() {
        assertEquals("", StringUtils.toTitleCase(""), "empty String");
        assertEquals("X", StringUtils.toTitleCase("x"), "lower first");
        assertEquals("X", StringUtils.toTitleCase("X"), "upper first");
        assertEquals("Xy", StringUtils.toTitleCase("xy"), "All lower");
        assertEquals("Xy", StringUtils.toTitleCase("XY"), "All upper");
        assertEquals("Abcd", StringUtils.toTitleCase("aBcD"), "mixed");
        assertEquals("Ab Cd", StringUtils.toTitleCase("aB cD"), "multiple words");
        assertEquals(" Ab  Cd ", StringUtils.toTitleCase(" aB  cD "), "multiple spaces");
        assertEquals("\tAb\t\tCd\t", StringUtils.toTitleCase("\taB\t\tcD\t"), "multiple tabs");
        assertEquals("123 456 7890", StringUtils.toTitleCase("123 456 7890"), "Numbers");
        assertEquals("!? >~", StringUtils.toTitleCase("!? >~"), "ASCII Specials");
        assertEquals("\u00a9\u00df \u2022\u2070 \u00c0\u00f1",
            StringUtils.toTitleCase("\u00a9\u00df \u2022\u2070 \u00e0\u00d1"),
            "Other Specials");                                                      // Map 00e0 => 00c0 and 00d1 => 00f1
        assertEquals("\ud83d\udc00 \ud83d\ude00",
            StringUtils.toTitleCase("\ud83d\udc00 \ud83d\ude00"),
            "Multi-character support");
    }

    /**
     * Unit test {@link StringUtils#upperFirst(String)}
     */
    @Test
    public void test_upperFirst() {
        assertEquals("", StringUtils.upperFirst(""), "empty String");
        assertEquals("X", StringUtils.upperFirst("x"), "lower first");
        assertEquals("X", StringUtils.upperFirst("X"), "upper first");
        assertEquals("Xy", StringUtils.upperFirst("xy"), "All lower");
        assertEquals("XY", StringUtils.upperFirst("XY"), "All upper");
        assertEquals("ABcD", StringUtils.upperFirst("aBcD"), "mixed");
    }

    /**
     * Unit test {@link StringUtils#lowerFirst(String)}
     */
    @Test
    public void test_lowerFirst() {
        assertEquals("", StringUtils.lowerFirst(""), "empty String");
        assertEquals("x", StringUtils.lowerFirst("x"), "lower first");
        assertEquals("x", StringUtils.lowerFirst("X"), "upper first");
        assertEquals("xy", StringUtils.lowerFirst("xy"), "All lower");
        assertEquals("xY", StringUtils.lowerFirst("XY"), "All upper");
        assertEquals("aBcD", StringUtils.lowerFirst("aBcD"), "mixed");
    }

    /**
     * Unit test {@link StringUtils#toggleFirst(String)}
     */
    @Test
    public void test_toggleFirst() {
        assertEquals("", StringUtils.toggleFirst(""), "empty String");
        assertEquals("X", StringUtils.toggleFirst("x"), "lower first");
        assertEquals("x", StringUtils.toggleFirst("X"), "upper first");
        assertEquals("Xy", StringUtils.toggleFirst("xy"), "All lower");
        assertEquals("xY", StringUtils.toggleFirst("XY"), "All upper");
        assertEquals("ABcD", StringUtils.toggleFirst("aBcD"), "mixed");
    }

    /**
     * Unit test {@link StringUtils#toggleAll(String)}
     */
    @Test
    public void test_toggleAll() {
        assertEquals("", StringUtils.toggleAll(""), "empty String");
        assertEquals("X", StringUtils.toggleAll("x"), "lower first");
        assertEquals("x", StringUtils.toggleAll("X"), "upper first");
        assertEquals("XY", StringUtils.toggleAll("xy"), "All lower");
        assertEquals("xy", StringUtils.toggleAll("XY"), "All upper");
        assertEquals("AbCd", StringUtils.toggleAll("aBcD"), "mixed");
    }

    /**
     * Unit test {@link StringUtils#left(String, int)}
     */
    @Test
    public void test_left() {
        assertEquals("abc", StringUtils.left("abcdef", 3), "Happy Path");
        assertEquals("", StringUtils.left("abcdef", 0), "Zero length");
        assertEquals("", StringUtils.left("", 3), "Empty String");
        assertEquals("abcdef", StringUtils.left("abcdef", 6), "Count is complete string");
        assertEquals("abcdef", StringUtils.left("abcdef", 7), "Count is beyond string");
        assertEquals("a", StringUtils.left("abcdef", -5), "Count is negative");
        assertEquals("", StringUtils.left("abcdef", -6), "Count is negative all string");
        assertEquals("", StringUtils.left("abcdef", -7), "Count is negative beyond end");
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_positiveStart_positiveLength() {
        assertEquals("bcd", StringUtils.mid("abcdef", 1, 3), "Happy Path");
        assertEquals("b", StringUtils.mid("abcdef", 1, 1), "single char");
        assertEquals("", StringUtils.mid("", 2, 5), "Empty String");
        assertEquals("abcdef", StringUtils.mid("abcdef", 0, 6), "Complete string");
        assertEquals("abcdef", StringUtils.mid("abcdef", 0, 7), "Beyond string");
        assertEquals("bcdef", StringUtils.mid("abcdef", 1, 7), "Beyond string");
        assertEquals("cdef", StringUtils.mid("abcdef", 2, Integer.MAX_VALUE), "large end position");
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_positiveStart_negativeLength() {
        assertEquals("bc", StringUtils.mid("abcdef", 1, -3), "Happy Path");
        assertEquals("bcde", StringUtils.mid("abcdef", 1, -1), "single char");
        assertEquals("", StringUtils.mid("", 2, -5), "Empty String");
        assertEquals("", StringUtils.mid("abcdef", 0, -6), "Complete string");
        assertEquals("", StringUtils.mid("abcdef", 0, -7), "Beyond string");
        assertEquals("", StringUtils.mid("abcdef", 1, -7), "Beyond string");
        assertEquals("", StringUtils.mid("abcdef", 2, Integer.MIN_VALUE), "large end position");
    }

    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_negativeStart_positiveLength() {
        assertEquals("cde", StringUtils.mid("abcdef", -4, 3), "Happy Path");
        assertEquals("f", StringUtils.mid("abcdef", -1, 1), "single char");
        assertEquals("", StringUtils.mid("", -2, 5), "Empty String");
        assertEquals("f", StringUtils.mid("abcdef", -1, 7), "Beyond end");
        assertEquals("", StringUtils.mid("abcdef", -7, 3), "Beyond start");      // to be consistent with Bash
        assertEquals("ef", StringUtils.mid("abcdef", -2, Integer.MAX_VALUE), "large end position");
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}.
     * The test data was double-checked against the same strings in a bash shell; the only difference is that
     * {@code mid} returns empty strings when bash would fail
     */
    @Test
    public void test_mid_negativeStart_negativeLength() {
        assertEquals("", StringUtils.mid("abcdef", -1, -1), "mid(-1, -1)");
        assertEquals("", StringUtils.mid("abcdef", -1, -2), "mid(-1, -2)");

        assertEquals("e", StringUtils.mid("abcdef", -2, -1), "mid(-2, -1)");
        assertEquals("", StringUtils.mid("abcdef", -2, -2), "mid(-2, -2)");
        assertEquals("", StringUtils.mid("abcdef", -2, -3), "mid(-2, -3)");

        assertEquals("de", StringUtils.mid("abcdef", -3, -1), "mid(-3, -1)");
        assertEquals("d", StringUtils.mid("abcdef", -3, -2), "mid(-3, -2)");
        assertEquals("", StringUtils.mid("abcdef", -3, -3), "mid(-3, -3)");
        assertEquals("", StringUtils.mid("abcdef", -3, -4), "mid(-3, -4)");

        assertEquals("cde", StringUtils.mid("abcdef", -4, -1), "mid(-4, -1)");
        assertEquals("cd", StringUtils.mid("abcdef", -4, -2), "mid(-4, -2)");
        assertEquals("c", StringUtils.mid("abcdef", -4, -3), "mid(-4, -3)");
        assertEquals("", StringUtils.mid("abcdef", -4, -4), "mid(-4, -4)");
        assertEquals("", StringUtils.mid("abcdef", -4, -5), "mid(-4, -5)");

        assertEquals("bcde", StringUtils.mid("abcdef", -5, -1), "mid(-5, -1)");
        assertEquals("bcd", StringUtils.mid("abcdef", -5, -2), "mid(-5, -2)");
        assertEquals("bc", StringUtils.mid("abcdef", -5, -3), "mid(-5, -3)");
        assertEquals("b", StringUtils.mid("abcdef", -5, -4), "mid(-5, -4)");
        assertEquals("", StringUtils.mid("abcdef", -5, -5), "mid(-5, -5)");
        assertEquals("", StringUtils.mid("abcdef", -5, -6), "mid(-5, -6)");

        assertEquals("abcde", StringUtils.mid("abcdef", -6, -1), "mid(-6, -1)");
        assertEquals("", StringUtils.mid("abcdef", -7, -1), "mid(-7, -1)");
        assertEquals("", StringUtils.mid("abcdef", -8, -1), "mid(-7, -1)");

        assertEquals("", StringUtils.mid("abcdef", -2, Integer.MIN_VALUE), "large end position");
    }


    /**
     * Unit test {@link StringUtils#right(String, int)}
     */
    @Test
    public void test_right() {
        assertEquals("def", StringUtils.right("abcdef", 3), "Happy Path");
        assertEquals("", StringUtils.right("abcdef", 0), "Zero length");
        assertEquals("", StringUtils.right("", 3), "Empty String");
        assertEquals("abcdef", StringUtils.right("abcdef", 6), "Count is complete string");
        assertEquals("abcdef", StringUtils.right("abcdef", 7), "Count is beyond string");
        assertEquals("f", StringUtils.right("abcdef", -5), "Count is negative");
        assertEquals("", StringUtils.right("abcdef", -6), "Count is negative all string");
        assertEquals("", StringUtils.right("abcdef", -7), "Count is negative beyond end");
    }
}