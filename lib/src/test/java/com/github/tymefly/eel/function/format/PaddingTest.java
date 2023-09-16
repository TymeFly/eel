package com.github.tymefly.eel.function.format;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Padding}
 */
public class PaddingTest {
    private Padding padding;


    @Before
    public void setUp() {
        padding = new Padding();
    }

    /**
     * Unit test {@link Padding#padLeft(String, int, char)}
     */
    @Test
    public void test_padLeft() {
        Assert.assertEquals("Spaces", "    Hello World", padding.padLeft("Hello World", 15, ' '));
        Assert.assertEquals("Stars", "****Hello World", padding.padLeft("Hello World", 15, '*'));
        Assert.assertEquals("Match length", "Hello World", padding.padLeft("Hello World", "Hello World".length(), ' '));
        Assert.assertEquals("less than length", "Hello World", padding.padLeft("Hello World", 5, ' '));
        Assert.assertEquals("zero length", "Hello World", padding.padLeft("Hello World", 0, ' '));
        Assert.assertThrows("negative length", IllegalArgumentException.class, () -> padding.padLeft("Hello World", -1, ' '));
    }

    /**
     * Unit test {@link Padding#padRight(String, int, char)}
     */
    @Test
    public void test_padRight() {
        Assert.assertEquals("Spaces", "Hello World    ", padding.padRight("Hello World", 15, ' '));
        Assert.assertEquals("Stars", "Hello World****", padding.padRight("Hello World", 15, '*'));
        Assert.assertEquals("Match length", "Hello World", padding.padRight("Hello World", "Hello World".length(), ' '));
        Assert.assertEquals("less than length", "Hello World", padding.padRight("Hello World", 5, ' '));
        Assert.assertEquals("zero length", "Hello World", padding.padRight("Hello World", 0, ' '));
        Assert.assertThrows("negative length", IllegalArgumentException.class, () -> padding.padLeft("Hello World", -1, ' '));
    }
}