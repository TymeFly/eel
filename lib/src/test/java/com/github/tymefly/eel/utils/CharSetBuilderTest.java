package com.github.tymefly.eel.utils;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link CharSetBuilder}
 */
public class CharSetBuilderTest {
    /**
     * Unit test {@link CharSetBuilder}
     */
    @Test
    public void test_ConstructAndAdd() {
        Set<Character> source = Set.of('A', 'B');

        assertEquals(Set.of('A', 'B', 'C'),
            new CharSetBuilder(source).with('C').immutable(),
            "Expected single char");
    }

    /**
     * Unit test {@link CharSetBuilder}
     */
    @Test
    public void test_empty() {
        assertEquals(Collections.emptySet(),
            new CharSetBuilder().immutable(),
            "Expected empty set");
    }

    /**
     * Unit test {@link CharSetBuilder#with(char)}
     */
    @Test
    public void test_with() {
        assertEquals(Set.of('A'),
            new CharSetBuilder().with('A').immutable(),
            "Expected single char");
    }

    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_range() {
        assertEquals(Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'),
            new CharSetBuilder().range('0', '9').immutable(),
            "Expected all digits");
    }

    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_range_single() {
        assertEquals(Set.of('0'),
            new CharSetBuilder().range('0', '0').immutable(),
            "Expected single digit");
    }

    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_range_invalid() {
        assertThrows(IllegalArgumentException.class,
            () -> new CharSetBuilder().range('1', '0'));
    }


    /**
     * Unit test {@link CharSetBuilder#withAll(Set)}
     */
    @Test
    public void test_including() {
        Set<Character> subSet = Set.of('?', ':', '@');
        Set<Character> actual = new CharSetBuilder().withAll(subSet).mutable();

        assertEquals(subSet, actual, "Expected test chars");
        assertNotSame(subSet, actual, "Invalid object returned");
    }


    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_multipleCalls() {
        Set<Character> actual = new CharSetBuilder()
            .with('A')
            .range('0', '9')
            .withAll(Set.of(':'))
            .with('b')
            .range('A', 'F')
            .with('c')
            .withAll(Set.of('?', '@'))
            .immutable();

        assertEquals(Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
               'A', 'B', 'C', 'D', 'E', 'F',
               'b', 'c',
               '?', ':', '@'),
            actual,
            "Expected all digits");
    }


    /**
     * Unit test {@link CharSetBuilder#mutable()}
     */
    @Test
    public void test_mutable() {
        Set<Character> actual = new CharSetBuilder().mutable();

        actual.add('@');

        assertEquals(Set.of('@'), actual, "Expected single char");
    }

    /**
     * Unit test {@link CharSetBuilder#immutable()}
     */
    @Test
    public void test_immutable() {
        Set<Character> actual = new CharSetBuilder().immutable();

        assertThrows(UnsupportedOperationException.class, () -> actual.add('@'));
    }

    /**
     * Unit test {@link CharSetBuilder#asString()}
     */
    @Test
    public void test_asString() {
        String actual = new CharSetBuilder().range('A', 'Z').asString();

        assertEquals(26, actual.length(), "Unexpected Length: " + actual);

        for (var test = 'A'; test <= 'Z'; test++) {
            assertTrue(actual.indexOf(test) != -1, actual + " is missing expected character '" + test + "'");
        }
    }
}