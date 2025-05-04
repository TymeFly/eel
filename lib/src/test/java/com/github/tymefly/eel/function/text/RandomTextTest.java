package com.github.tymefly.eel.function.text;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link RandomText}
 */
public class RandomTextTest {
    private static final String DEFAULT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Set<Character> CHARACTERS = DEFAULT_CHARACTERS.codePoints()
        .mapToObj(c -> (char) c)
        .collect(Collectors.toSet());


    /**
     * Unit test {@link RandomText#random(int, String)}
     */
    @Test
    public void test_Random_lengths() {
        Assert.assertEquals("0 length", "", new RandomText().random(0, "a"));
        Assert.assertEquals("1 length", "a", new RandomText().random(1, "a"));
        Assert.assertEquals("2 length", "aa", new RandomText().random(2, "a"));
        Assert.assertEquals("3 length", "aaa", new RandomText().random(3, "a"));
    }


    @Test
    public void test_Random() {
        int loop = 100;
        Set<String> values = new HashSet<>();

        while (loop-- != 0) {
            String actual = new RandomText().random(10, DEFAULT_CHARACTERS);

            randomHelper(actual);
            values.add(actual);
       }

        Assert.assertTrue("Unexpected number of clashes: " + values.size(),
            values.size() > 95);
    }

    private void randomHelper(@Nonnull String actual) {
        Assert.assertEquals("Unexpected length", 10, actual.length());

        for (var test : actual.toCharArray()) {
            Assert.assertTrue("Unexpected character '" + test + "' in " + actual,
                CHARACTERS.contains(test));
        }
    }


    /**
     * Unit test {@link RandomText#parseCharacterSet(String)}
     */
    @Test
    public void test_parseCharacterSet() {
        Assert.assertThrows("Empty chars",
            IllegalArgumentException.class,
            () -> new RandomText().parseCharacterSet(""));
        Assert.assertThrows("Ambiguous Range",
            IllegalArgumentException.class,
            () -> new RandomText().parseCharacterSet("a-c-g"));

        parseCharacterSetHelper("a", "a");
        parseCharacterSetHelper("ab", "ab");
        parseCharacterSetHelper("-ab", "-ab");
        parseCharacterSetHelper("ab-", "-ab");
        parseCharacterSetHelper("a-c", "abc");
        parseCharacterSetHelper("0-9", "0123456789");
        parseCharacterSetHelper("ab0-9", "ab0123456789");
        parseCharacterSetHelper("a-c0-9", "abc0123456789");
        parseCharacterSetHelper("-a-c0-9", "-abc0123456789");
        parseCharacterSetHelper("a-c0-9-", "-abc0123456789");
    }

    private void parseCharacterSetHelper(@Nonnull String expression, @Nonnull String expected) {
        String actual = new RandomText().parseCharacterSet(expression);

        Assert.assertEquals(expression + ": Unexpected Length: " + actual, expected.length(), actual.length());

        for (var test : expected.toCharArray()) {
            Assert.assertTrue(expression + ": " + actual + " is missing expected character '" + test + "'",
                actual.indexOf(test) != -1);
        }
    }

    /**
     * Unit test {@link RandomText#minDistinctChars(int, String)}
     */
    @Test
    public void test_minDistinctChars() {
        // Minimal unique chars
        Assert.assertEquals("0 length, 1 char", 0, new RandomText().minDistinctChars(0, "a"));
        Assert.assertEquals("1 length, 1 char", 1, new RandomText().minDistinctChars(1, "a"));
        Assert.assertEquals("2 length, 1 char", 1, new RandomText().minDistinctChars(2, "a"));
        Assert.assertEquals("3 length, 1 char", 1, new RandomText().minDistinctChars(3, "a"));

        // Minimal length of generated string
        Assert.assertEquals("1 length, 0 char", 0, new RandomText().minDistinctChars(1, ""));
        Assert.assertEquals("1 length, 1 char", 1, new RandomText().minDistinctChars(1, "a"));
        Assert.assertEquals("1 length, 2 char", 1, new RandomText().minDistinctChars(1, "ab"));
        Assert.assertEquals("1 length, 3 char", 1, new RandomText().minDistinctChars(1, "abc"));

        // typical values
        Assert.assertEquals("default length and chars",
            5,
            new RandomText().minDistinctChars(10, DEFAULT_CHARACTERS));
        Assert.assertEquals("20 length, default chars",
            10,
            new RandomText().minDistinctChars(20, DEFAULT_CHARACTERS));
        Assert.assertEquals("20 length, hex characters",
            8,
            new RandomText().minDistinctChars(20, "0123456789abcdef"));
    }
}