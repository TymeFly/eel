package com.github.tymefly.eel.function.text;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("", new RandomText().random(0, "a"), "0 length");
        assertEquals("a", new RandomText().random(1, "a"), "1 length");
        assertEquals("aa", new RandomText().random(2, "a"), "2 length");
        assertEquals("aaa", new RandomText().random(3, "a"), "3 length");
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

        assertTrue(values.size() > 95, "Unexpected number of clashes: " + values.size());
    }

    private void randomHelper(@Nonnull String actual) {
        assertEquals(10, actual.length(), "Unexpected length");

        for (var test : actual.toCharArray()) {
            assertTrue(CHARACTERS.contains(test), "Unexpected character '" + test + "' in " + actual);
        }
    }


    /**
     * Unit test {@link RandomText#parseCharacterSet(String)}
     */
    @Test
    public void test_parseCharacterSet() {
        assertThrows(IllegalArgumentException.class,
            () -> new RandomText().parseCharacterSet(""),
            "Empty chars");
        assertThrows(IllegalArgumentException.class,
            () -> new RandomText().parseCharacterSet("a-c-g"),
            "Ambiguous Range");

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
        parseCharacterSetHelper("+--", "+,-");
    }

    private void parseCharacterSetHelper(@Nonnull String expression, @Nonnull String expected) {
        String actual = new RandomText().parseCharacterSet(expression);

        assertEquals(expected.length(), actual.length(), expression + ": Unexpected Length: " + actual);

        for (var test : expected.toCharArray()) {
            assertTrue(actual.indexOf(test) != -1, expression + ": " + actual + " is missing expected character '" + test + "'");
        }
    }

    /**
     * Unit test {@link RandomText#minDistinctChars(int, String)}
     */
    @Test
    public void test_minDistinctChars() {
        // Minimal unique chars
        assertEquals(0, new RandomText().minDistinctChars(0, "a"), "0 length, 1 char");
        assertEquals(1, new RandomText().minDistinctChars(1, "a"), "1 length, 1 char");
        assertEquals(1, new RandomText().minDistinctChars(2, "a"), "2 length, 1 char");
        assertEquals(1, new RandomText().minDistinctChars(3, "a"), "3 length, 1 char");

        // Minimal length of generated string
        assertEquals(0, new RandomText().minDistinctChars(1, ""), "1 length, 0 char");
        assertEquals(1, new RandomText().minDistinctChars(1, "a"), "1 length, 1 char");
        assertEquals(1, new RandomText().minDistinctChars(1, "ab"), "1 length, 2 char");
        assertEquals(1, new RandomText().minDistinctChars(1, "abc"), "1 length, 3 char");

        // typical values
        assertEquals(7, new RandomText().minDistinctChars(10, DEFAULT_CHARACTERS), "default length and chars");
        assertEquals(15, new RandomText().minDistinctChars(20, DEFAULT_CHARACTERS), "20 length, default chars");
        assertEquals(12, new RandomText().minDistinctChars(20, "0123456789abcdef"), "20 length, hex characters");
    }
}