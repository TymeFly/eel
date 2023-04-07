package com.github.tymefly.eel.utils;

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link CharSetBuilder}
 */
public class CharSetBuilderTest {
    /**
     * Unit test {@link CharSetBuilder}
     */
    @Test
    public void test_empty() {
        Assert.assertEquals("Expected empty set",
            Collections.emptySet(),
            new CharSetBuilder().immutable());
    }

    /**
     * Unit test {@link CharSetBuilder#with(char)}
     */
    @Test
    public void test_with() {
        Assert.assertEquals("Expected single char",
            Set.of('A'),
            new CharSetBuilder().with('A').immutable());
    }

    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_range() {
        Assert.assertEquals("Expected all digits",
            Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'),
            new CharSetBuilder().range('0', '9').immutable());
    }

    /**
     * Unit test {@link CharSetBuilder#range(char, char)}
     */
    @Test
    public void test_range_invalid() {
        Assert.assertThrows(IllegalArgumentException.class,
            () -> new CharSetBuilder().range('1', '0'));
    }


    /**
     * Unit test {@link CharSetBuilder#withAll(Set)}
     */
    @Test
    public void test_including() {
        Set<Character> subSet = Set.of('?', ':', '@');
        Set<Character> actual = new CharSetBuilder().withAll(subSet).mutable();

        Assert.assertEquals("Expected test chars", subSet, actual);
        Assert.assertNotSame("Invalid object returned", subSet, actual);
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

        Assert.assertEquals("Expected all digits",
            Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                   'A', 'B', 'C', 'D', 'E', 'F',
                   'b', 'c',
                   '?', ':', '@'),
            actual);
    }


    /**
     * Unit test {@link CharSetBuilder#mutable()}
     */
    @Test
    public void test_mutable() {
        Set<Character> actual = new CharSetBuilder().mutable();

        actual.add('@');

        Assert.assertEquals("Expected single char",
            Set.of('@'),
            actual);
    }

    /**
     * Unit test {@link CharSetBuilder#immutable()}
     */
    @Test
    public void test_immutable() {
        Set<Character> actual = new CharSetBuilder().immutable();

        Assert.assertThrows(UnsupportedOperationException.class, () -> actual.add('@'));
    }

}