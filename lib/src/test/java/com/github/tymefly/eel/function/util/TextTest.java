package com.github.tymefly.eel.function.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Text}
 */
public class TextTest {
    /**
     * Unit test {@link Text#upper(String)}
     */
    @Test
    public void test_Upper() {
        Assert.assertEquals("Empty String", "", new Text().upper(""));
        Assert.assertEquals("already lower", "ABC", new Text().upper("abc"));
        Assert.assertEquals("upper case", "ABC", new Text().upper("ABC"));
        Assert.assertEquals("mixed types", "ABC ABC 123 @+2", new Text().upper("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#lower(String)}
     */
    @Test
    public void test_Lower() {
        Assert.assertEquals("Empty String", "", new Text().lower(""));
        Assert.assertEquals("already lower", "abc", new Text().lower("abc"));
        Assert.assertEquals("upper case", "abc", new Text().lower("ABC"));
        Assert.assertEquals("mixed types", "abc abc 123 @+2", new Text().lower("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#title(String)}
     */
    @Test
    public void test_title() {
        Assert.assertEquals("Empty String", "", new Text().title(""));
        Assert.assertEquals("lower case", "Abc", new Text().title("abc"));
        Assert.assertEquals("upper case", "Abc", new Text().title("ABC"));
        Assert.assertEquals("mixed types", "Abc Abc 123 @+2", new Text().title("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#left(String, int)}
     */
    @Test
    public void test_left() {
        Assert.assertEquals("Happy Path", "abc", new Text().left("abcdef", 3));
        Assert.assertEquals("full string", "abcdef", new Text().left("abcdef", 6));
        Assert.assertEquals("overflow", "abcdef", new Text().left("abcdef", 7));
        Assert.assertEquals("empty", "", new Text().left("abcdef", 0));
        Assert.assertEquals("negative size", "", new Text().left("abcdef", -2));
    }

    /**
     * Unit test {@link Text#right(String, int)}
     */
    @Test
    public void test_right() {
        Assert.assertEquals("Happy Path", "def", new Text().right("abcdef", 3));
        Assert.assertEquals("full string", "abcdef", new Text().right("abcdef", 6));
        Assert.assertEquals("overflow", "abcdef", new Text().right("abcdef", 7));
        Assert.assertEquals("empty", "", new Text().right("abcdef", 0));
        Assert.assertEquals("negative size", "", new Text().right("abcdef", -2));
    }

    /**
     * Unit test {@link Text#mid(String, int, int)}
     */
    @Test
    public void test_mid() {
        Assert.assertEquals("Happy Path", "bc", new Text().mid("abcdef", 1, 2));
        Assert.assertEquals("no chars", "", new Text().mid("abcdef", 0, 0));
        Assert.assertEquals("single char", "a", new Text().mid("abcdef", 0, 1));
        Assert.assertEquals("full string", "abcdef", new Text().mid("abcdef", 0, 6));
        Assert.assertEquals("overflow", "abcdef", new Text().mid("abcdef", -1, 6));
        Assert.assertEquals("negative size", "", new Text().mid("abcdef", 4, -2));
    }

    /**
     * Unit test {@link Text#beforeFirst(String, String)}
     */
    @Test
    public void test_beforeFirst() {
        Assert.assertEquals("Empty Text", "", new Text().beforeFirst("", "~"));
        Assert.assertEquals("No delimiter", "abcdef", new Text().beforeFirst("abcdef", "~"));
        Assert.assertEquals("One delimiter", "abc", new Text().beforeFirst("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ab", new Text().beforeFirst("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "", new Text().beforeFirst("~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "abcdef", new Text().beforeFirst("abcdef~", "~"));

        Assert.assertEquals("Long delimiter", "ab", new Text().beforeFirst("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "", new Text().beforeFirst("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#beforeLast(String, String)}
     */
    @Test
    public void test_beforeLast() {
        Assert.assertEquals("Empty Text", "", new Text().beforeLast("", "~"));
        Assert.assertEquals("No delimiter", "abcdef", new Text().beforeLast("abcdef", "~"));
        Assert.assertEquals("One delimiter", "abc", new Text().beforeLast("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ab~cd", new Text().beforeLast("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "", new Text().beforeLast("~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "abcdef", new Text().beforeLast("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "ab<=>cd", new Text().beforeLast("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "ab~cd~ef", new Text().beforeLast("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#afterFirst(String, String)}
     */
    @Test
    public void test_afterFirst() {
        Assert.assertEquals("Empty Text", "", new Text().afterFirst("", "~"));
        Assert.assertEquals("No delimiter", "abcdef", new Text().afterFirst("abcdef", "~"));
        Assert.assertEquals("One delimiter", "def", new Text().afterFirst("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "cd~ef", new Text().afterFirst("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "abcdef", new Text().afterFirst("~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "", new Text().afterFirst("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "cd<=>ef", new Text().afterFirst("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "ab~cd~ef", new Text().afterFirst("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#afterLast(String, String)}
     */
    @Test
    public void test_afterLast() {
        Assert.assertEquals("Empty Text", "", new Text().afterLast("", "~"));
        Assert.assertEquals("No delimiter", "abcdef", new Text().afterLast("abcdef", "~"));
        Assert.assertEquals("One delimiter", "def", new Text().afterLast("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ef", new Text().afterLast("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "abcdef", new Text().afterLast("~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "", new Text().afterLast("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "ef", new Text().afterLast("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "", new Text().afterLast("ab~cd~ef", ""));
    }


    /**
     * Unit test {@link Text#extract(String, String)}
     */
    @Test
    public void test_extract() {
        Assert.assertEquals("Not Found", "", new Text().extract("Hello World", ".*~(.*)~.*"));
        Assert.assertEquals("Found", "capture", new Text().extract("Hello ~capture~ World", ".*~(.*)~.*"));
        Assert.assertEquals("multiple groups",
            "FirstSecondThird",
            new Text().extract("prefix /First/Second/Third/ postfix", ".*/(.*)/(.*)/(.*)/.*"));
    }


    /**
     * Unit test {@link Text#matches(String, String)}
     */
    @Test
    public void test_matches() {
        Assert.assertFalse("Not Found", new Text().matches("Hello World", ".*~(.*)~.*"));
        Assert.assertTrue("Found", new Text().matches("Hello ~find me~ World", ".*~(.*)~.*"));
    }

    /**
     * Unit test {@link Text#replace(String, String, String)}
     */
    @Test
    public void test_replace() {
        Assert.assertEquals("Empty String", "", new Text().replace("", " ", "#"));
        Assert.assertEquals("single char replacement", "Hello#World#!", new Text().replace("Hello World !", " ", "#"));
        Assert.assertEquals("two char replacement", "Hello__World__!", new Text().replace("Hello World !", " ", "__"));
        Assert.assertEquals("Word removal", " World !", new Text().replace("Hello World !", "Hello", ""));
    }

    /**
     * Unit test {@link Text#replaceEx(String, String, String)}
     */
    @Test
    public void test_replaceEx() {
        Assert.assertEquals("Empty String", "", new Text().replaceEx("", " ", "#"));
        Assert.assertEquals("single char replacement", "Hello#World#!", new Text().replaceEx("Hello World !", " ", "#"));
        Assert.assertEquals("two char replacement", "Hello__World__!", new Text().replaceEx("Hello World !", " ", "__"));
        Assert.assertEquals("Word removal", " World !", new Text().replaceEx("Hello World !", "Hello", ""));
        Assert.assertEquals("replace uppers", "ello orld !", new Text().replaceEx("Hello World !", "[A-Z]", ""));
    }

    /**
     * Unit test {@link Text#trim(String)}
     */
    @Test
    public void test_trim() {
        Assert.assertEquals("Empty String", "", new Text().trim(""));
        Assert.assertEquals("No White Space", "abc", new Text().trim("abc"));
        Assert.assertEquals("Internal Space", "ab cd", new Text().trim("ab cd"));
        Assert.assertEquals("Internal Tab", "ab\tcd", new Text().trim("ab\tcd"));
        Assert.assertEquals("Leading Space", "a", new Text().trim(" a"));
        Assert.assertEquals("Trailing Space", "a", new Text().trim("a "));
        Assert.assertEquals("Leading Tab", "a", new Text().trim("\ta"));
        Assert.assertEquals("Trailing Tab", "a", new Text().trim("a\t"));
        Assert.assertEquals("Trailing and trailing spaces", "ab \tcd", new Text().trim(" \t ab \tcd\t \t"));
    }

    /**
     * Unit test {@link Text#isEmpty(String)}
     */
    @Test
    public void test_isEmpty() {
        Assert.assertTrue("Empty String", new Text().isEmpty(""));
        Assert.assertFalse("Whitespace String", new Text().isEmpty(" \t "));
        Assert.assertFalse("Non-empty String", new Text().isEmpty("x"));
    }

    /**
     * Unit test {@link Text#isBlank(String)}
     */
    @Test
    public void test_isBlank() {
        Assert.assertTrue("Empty String", new Text().isBlank(""));
        Assert.assertTrue("Whitespace String", new Text().isBlank(" \t "));
        Assert.assertFalse("Non-empty String", new Text().isBlank("x"));
    }


    /**
     * Unit test {@link Text#len(String)}
     */
    @Test
    public void test_len() {
        Assert.assertEquals("Empty String", 0, new Text().len(""));
        Assert.assertEquals("Single character", 1, new Text().len("a"));
        Assert.assertEquals("Whitespace", 3, new Text().len(" \t "));
        Assert.assertEquals("Multiple characters", 4, new Text().len("abcd"));
    }

    /**
     * Unit test {@link Text#indexOf(String, String)}
     */
    @Test
    public void test_IndexOf() {
        Assert.assertEquals("Empty String", -1, new Text().indexOf("", "cd"));
        Assert.assertEquals("Empty search", 0, new Text().indexOf("abcdefabcdef", ""));
        Assert.assertEquals("Found", 2, new Text().indexOf("abcdefabcdef", "cd"));
        Assert.assertEquals("Not found", -1, new Text().indexOf("abcdefabcdef", "dc"));
    }

    /**
     * Unit test {@link Text#lastIndexOf(String, String)}
     */
    @Test
    public void test_LastIndexOf() {
        Assert.assertEquals("Empty String", -1, new Text().lastIndexOf("", "cd"));
        Assert.assertEquals("Empty search", 12, new Text().lastIndexOf("abcdefabcdef", ""));
        Assert.assertEquals("Found", 8, new Text().lastIndexOf("abcdefabcdef", "cd"));
        Assert.assertEquals("Not found", -1, new Text().lastIndexOf("abcdefabcdef", "dc"));
    }
}