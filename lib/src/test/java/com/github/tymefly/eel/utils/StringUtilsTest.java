package com.github.tymefly.eel.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link StringUtils}
 */
public class StringUtilsTest {

    /**
     * Unit test {@link StringUtils#toTitleCase(String)}
     */
    @Test
    public void test_toTitleCase() {
        Assert.assertEquals("empty String", "", StringUtils.toTitleCase(""));
        Assert.assertEquals("lower first", "X", StringUtils.toTitleCase("x"));
        Assert.assertEquals("upper first", "X", StringUtils.toTitleCase("X"));
        Assert.assertEquals("All lower", "Xy", StringUtils.toTitleCase("xy"));
        Assert.assertEquals("All upper", "Xy", StringUtils.toTitleCase("XY"));
        Assert.assertEquals("mixed", "Abcd", StringUtils.toTitleCase("aBcD"));
        Assert.assertEquals("multiple words", "Ab Cd", StringUtils.toTitleCase("aB cD"));
        Assert.assertEquals("multiple spaces", " Ab  Cd ", StringUtils.toTitleCase(" aB  cD "));
        Assert.assertEquals("multiple tabs", "\tAb\t\tCd\t", StringUtils.toTitleCase("\taB\t\tcD\t"));
        Assert.assertEquals("Numbers", "123 456 7890", StringUtils.toTitleCase("123 456 7890"));
        Assert.assertEquals("ASCII Specials", "!? >~", StringUtils.toTitleCase("!? >~"));
        Assert.assertEquals("Other Specials",
            "\u00a9\u00df \u2022\u2070 \u00c0\u00f1",
            StringUtils.toTitleCase("\u00a9\u00df \u2022\u2070 \u00e0\u00d1")); // Map 00e0 => 00c0 and 00d1 => 00f1
        Assert.assertEquals("Multi-character support",
            "\ud83d\udc00 \ud83d\ude00",                                        // Rat and a happy face
            StringUtils.toTitleCase("\ud83d\udc00 \ud83d\ude00"));
    }

    /**
     * Unit test {@link StringUtils#upperFirst(String)}
     */
    @Test
    public void test_upperFirst() {
        Assert.assertEquals("empty String", "", StringUtils.upperFirst(""));
        Assert.assertEquals("lower first", "X", StringUtils.upperFirst("x"));
        Assert.assertEquals("upper first", "X", StringUtils.upperFirst("X"));
        Assert.assertEquals("All lower", "Xy", StringUtils.upperFirst("xy"));
        Assert.assertEquals("All upper", "XY", StringUtils.upperFirst("XY"));
        Assert.assertEquals("mixed", "ABcD", StringUtils.upperFirst("aBcD"));
    }

    /**
     * Unit test {@link StringUtils#lowerFirst(String)}
     */
    @Test
    public void test_lowerFirst() {
        Assert.assertEquals("empty String", "", StringUtils.lowerFirst(""));
        Assert.assertEquals("lower first", "x", StringUtils.lowerFirst("x"));
        Assert.assertEquals("upper first", "x", StringUtils.lowerFirst("X"));
        Assert.assertEquals("All lower", "xy", StringUtils.lowerFirst("xy"));
        Assert.assertEquals("All upper", "xY", StringUtils.lowerFirst("XY"));
        Assert.assertEquals("mixed", "aBcD", StringUtils.lowerFirst("aBcD"));
    }

    /**
     * Unit test {@link StringUtils#toggleFirst(String)}
     */
    @Test
    public void test_toggleFirst() {
        Assert.assertEquals("empty String", "", StringUtils.toggleFirst(""));
        Assert.assertEquals("lower first", "X", StringUtils.toggleFirst("x"));
        Assert.assertEquals("upper first", "x", StringUtils.toggleFirst("X"));
        Assert.assertEquals("All lower", "Xy", StringUtils.toggleFirst("xy"));
        Assert.assertEquals("All upper", "xY", StringUtils.toggleFirst("XY"));
        Assert.assertEquals("mixed", "ABcD", StringUtils.toggleFirst("aBcD"));
    }

    /**
     * Unit test {@link StringUtils#toggleAll(String)}
     */
    @Test
    public void test_toggleAll() {
        Assert.assertEquals("empty String", "", StringUtils.toggleAll(""));
        Assert.assertEquals("lower first", "X", StringUtils.toggleAll("x"));
        Assert.assertEquals("upper first", "x", StringUtils.toggleAll("X"));
        Assert.assertEquals("All lower", "XY", StringUtils.toggleAll("xy"));
        Assert.assertEquals("All upper", "xy", StringUtils.toggleAll("XY"));
        Assert.assertEquals("mixed", "AbCd", StringUtils.toggleAll("aBcD"));
    }

    /**
     * Unit test {@link StringUtils#left(String, int)}
     */
    @Test
    public void test_left() {
        Assert.assertEquals("Happy Path", "abc", StringUtils.left("abcdef", 3));
        Assert.assertEquals("Zero length", "", StringUtils.left("abcdef", 0));
        Assert.assertEquals("Empty String", "", StringUtils.left("", 3));
        Assert.assertEquals("Count is complete string", "abcdef", StringUtils.left("abcdef", 6));
        Assert.assertEquals("Count is beyond string", "abcdef", StringUtils.left("abcdef", 7));
        Assert.assertEquals("Count is negative", "a", StringUtils.left("abcdef", -5));
        Assert.assertEquals("Count is negative all string", "", StringUtils.left("abcdef", -6));
        Assert.assertEquals("Count is negative beyond end", "", StringUtils.left("abcdef", -7));
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_positiveStart_positiveLength() {
        Assert.assertEquals("Happy Path", "bcd", StringUtils.mid("abcdef", 1, 3));
        Assert.assertEquals("single char", "b", StringUtils.mid("abcdef", 1, 1));
        Assert.assertEquals("Empty String", "", StringUtils.mid("", 2, 5));
        Assert.assertEquals("Complete string", "abcdef", StringUtils.mid("abcdef", 0, 6));
        Assert.assertEquals("Beyond string", "abcdef", StringUtils.mid("abcdef", 0, 7));
        Assert.assertEquals("Beyond string", "bcdef", StringUtils.mid("abcdef", 1, 7));
        Assert.assertEquals("large end position", "cdef", StringUtils.mid("abcdef", 2, Integer.MAX_VALUE));
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_positiveStart_negativeLength() {
        Assert.assertEquals("Happy Path", "bc", StringUtils.mid("abcdef", 1, -3));
        Assert.assertEquals("single char", "bcde", StringUtils.mid("abcdef", 1, -1));
        Assert.assertEquals("Empty String", "", StringUtils.mid("", 2, -5));
        Assert.assertEquals("Complete string", "", StringUtils.mid("abcdef", 0, -6));
        Assert.assertEquals("Beyond string", "", StringUtils.mid("abcdef", 0, -7));
        Assert.assertEquals("Beyond string", "", StringUtils.mid("abcdef", 1, -7));
        Assert.assertEquals("large end position", "", StringUtils.mid("abcdef", 2, Integer.MIN_VALUE));
    }

    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid_negativeStart_positiveLength() {
        Assert.assertEquals("Happy Path", "cde", StringUtils.mid("abcdef", -4, 3));
        Assert.assertEquals("single char", "f", StringUtils.mid("abcdef", -1, 1));
        Assert.assertEquals("Empty String", "", StringUtils.mid("", -2, 5));
        Assert.assertEquals("Beyond end", "f", StringUtils.mid("abcdef", -1, 7));
        Assert.assertEquals("Beyond start", "", StringUtils.mid("abcdef", -7, 3));      // to be consistent with Bash
        Assert.assertEquals("large end position", "ef", StringUtils.mid("abcdef", -2, Integer.MAX_VALUE));
    }


    /**
     * Unit test {@link StringUtils#mid(String, int, int)}.
     * The test data was double-checked against the same strings in a bash shell; the only difference is that
     * {@code mid} returns empty strings when bash would fail
     */
    @Test
    public void test_mid_negativeStart_negativeLength() {
        Assert.assertEquals("mid(-1, -1)", "", StringUtils.mid("abcdef", -1, -1));
        Assert.assertEquals("mid(-1, -2)", "", StringUtils.mid("abcdef", -1, -2));

        Assert.assertEquals("mid(-2, -1)", "e", StringUtils.mid("abcdef", -2, -1));
        Assert.assertEquals("mid(-2, -2)", "", StringUtils.mid("abcdef", -2, -2));
        Assert.assertEquals("mid(-2, -3)", "", StringUtils.mid("abcdef", -2, -3));

        Assert.assertEquals("mid(-3, -1)", "de", StringUtils.mid("abcdef", -3, -1));
        Assert.assertEquals("mid(-3, -2)", "d", StringUtils.mid("abcdef", -3, -2));
        Assert.assertEquals("mid(-3, -3)", "", StringUtils.mid("abcdef", -3, -3));
        Assert.assertEquals("mid(-3, -4)", "", StringUtils.mid("abcdef", -3, -4));

        Assert.assertEquals("mid(-4, -1)", "cde", StringUtils.mid("abcdef", -4, -1));
        Assert.assertEquals("mid(-4, -2)", "cd", StringUtils.mid("abcdef", -4, -2));
        Assert.assertEquals("mid(-4, -3)", "c", StringUtils.mid("abcdef", -4, -3));
        Assert.assertEquals("mid(-4, -4)", "", StringUtils.mid("abcdef", -4, -4));
        Assert.assertEquals("mid(-4, -5)", "", StringUtils.mid("abcdef", -4, -5));

        Assert.assertEquals("mid(-5, -1)", "bcde", StringUtils.mid("abcdef", -5, -1));
        Assert.assertEquals("mid(-5, -2)", "bcd", StringUtils.mid("abcdef", -5, -2));
        Assert.assertEquals("mid(-5, -3)", "bc", StringUtils.mid("abcdef", -5, -3));
        Assert.assertEquals("mid(-5, -4)", "b", StringUtils.mid("abcdef", -5, -4));
        Assert.assertEquals("mid(-5, -5)", "", StringUtils.mid("abcdef", -5, -5));
        Assert.assertEquals("mid(-5, -6)", "", StringUtils.mid("abcdef", -5, -6));

        Assert.assertEquals("mid(-6, -1)", "abcde", StringUtils.mid("abcdef", -6, -1));
        Assert.assertEquals("mid(-7, -1)", "", StringUtils.mid("abcdef", -7, -1));
        Assert.assertEquals("mid(-7, -1)", "", StringUtils.mid("abcdef", -8, -1));

        Assert.assertEquals("large end position", "", StringUtils.mid("abcdef", -2, Integer.MIN_VALUE));
    }


    /**
     * Unit test {@link StringUtils#right(String, int)}
     */
    @Test
    public void test_right() {
        Assert.assertEquals("Happy Path", "def", StringUtils.right("abcdef", 3));
        Assert.assertEquals("Zero length", "", StringUtils.right("abcdef", 0));
        Assert.assertEquals("Empty String", "", StringUtils.right("", 3));
        Assert.assertEquals("Count is complete string", "abcdef", StringUtils.right("abcdef", 6));
        Assert.assertEquals("Count is beyond string", "abcdef", StringUtils.right("abcdef", 7));
        Assert.assertEquals("Count is negative", "f", StringUtils.right("abcdef", -5));
        Assert.assertEquals("Count is negative all string", "", StringUtils.right("abcdef", -6));
        Assert.assertEquals("Count is negative beyond end", "", StringUtils.right("abcdef", -7));
    }
}