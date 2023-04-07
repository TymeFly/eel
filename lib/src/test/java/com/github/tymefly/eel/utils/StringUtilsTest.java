package com.github.tymefly.eel.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link StringUtils}
 */
public class StringUtilsTest {

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