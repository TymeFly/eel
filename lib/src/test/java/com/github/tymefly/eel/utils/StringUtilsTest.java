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
        Assert.assertEquals("Empty String", "", StringUtils.left("", 3));
        Assert.assertEquals("Count is negative", "", StringUtils.left("abcdef", -3));
        Assert.assertEquals("Count is complete string", "abcdef", StringUtils.left("abcdef", 6));
        Assert.assertEquals("Count is beyond string", "abcdef", StringUtils.left("abcdef", 7));
    }

    /**
     * Unit test {@link StringUtils#mid(String, int, int)}
     */
    @Test
    public void test_mid() {
        Assert.assertEquals("Happy Path", "bcd", StringUtils.mid("abcdef", 1, 3));
        Assert.assertEquals("single char", "b", StringUtils.mid("abcdef", 1, 1));
        Assert.assertEquals("Empty String", "", StringUtils.mid("", 2, 5));
        Assert.assertEquals("Start is negative", "abcd", StringUtils.mid("abcdef", -2, 4));
        Assert.assertEquals("Complete string", "abcdef", StringUtils.mid("abcdef", 0, 6));
        Assert.assertEquals("Beyond string", "abcdef", StringUtils.mid("abcdef", 0, 7));
        Assert.assertEquals("Beyond string", "bcdef", StringUtils.mid("abcdef", 1, 7));
    }

    /**
     * Unit test {@link StringUtils#right(String, int)}
     */
    @Test
    public void test_right() {
        Assert.assertEquals("Happy Path", "def", StringUtils.right("abcdef", 3));
        Assert.assertEquals("Empty String", "", StringUtils.right("", 3));
        Assert.assertEquals("Count is negative", "", StringUtils.right("abcdef", -3));
        Assert.assertEquals("Count is complete string", "abcdef", StringUtils.right("abcdef", 6));
        Assert.assertEquals("Count is beyond string", "abcdef", StringUtils.right("abcdef", 7));
    }
}