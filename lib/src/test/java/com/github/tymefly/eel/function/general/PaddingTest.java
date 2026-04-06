package com.github.tymefly.eel.function.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Padding}
 */
public class PaddingTest {
    private Padding padding;


    @BeforeEach
    public void setUp() {
        padding = new Padding();
    }

    /**
     * Unit test {@link Padding#padLeft(String, int, char)}
     */
    @Test
    public void test_padLeft() {
        assertEquals("    Hello World", padding.padLeft("Hello World", 15, ' '), "Spaces");
        assertEquals("****Hello World", padding.padLeft("Hello World", 15, '*'), "Stars");
        assertEquals("Hello World", padding.padLeft("Hello World", "Hello World".length(), ' '), "Match length");
        assertEquals("Hello World", padding.padLeft("Hello World", 5, ' '), "less than length");
        assertEquals("Hello World", padding.padLeft("Hello World", 0, ' '), "zero length");
        assertThrows(IllegalArgumentException.class, () -> padding.padLeft("Hello World", -1, ' '), "negative length");
    }

    /**
     * Unit test {@link Padding#padRight(String, int, char)}
     */
    @Test
    public void test_padRight() {
        assertEquals("Hello World    ", padding.padRight("Hello World", 15, ' '), "Spaces");
        assertEquals("Hello World****", padding.padRight("Hello World", 15, '*'), "Stars");
        assertEquals("Hello World", padding.padRight("Hello World", "Hello World".length(), ' '), "Match length");
        assertEquals("Hello World", padding.padRight("Hello World", 5, ' '), "less than length");
        assertEquals("Hello World", padding.padRight("Hello World", 0, ' '), "zero length");
        assertThrows(IllegalArgumentException.class, () -> padding.padLeft("Hello World", -1, ' '), "negative length");
    }
}