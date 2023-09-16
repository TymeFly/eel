package com.github.tymefly.eel.function.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Convert}
 */
public class ConvertTest {
    /**
     * Unit test {@link Convert#toChar(int)}
     */
    @Test
    public void test_toChar() {
        Assert.assertEquals("Convert uppercase", "A", new Convert().toChar(65));
        Assert.assertEquals("Convert lowercase", "b", new Convert().toChar(98));
        Assert.assertEquals("Convert digit", "0", new Convert().toChar(48));
        Assert.assertEquals("Convert special", "!", new Convert().toChar(33));
        Assert.assertEquals("Convert control", "\n", new Convert().toChar(10));
        Assert.assertEquals("Convert Latin-1 Supplement", "\u00a9", new Convert().toChar(169));     // (c) symbol > 128
        Assert.assertEquals("Convert currency", "\u20ac", new Convert().toChar(8364));              // euro symbol > 256
    }

    /**
     * Unit test {@link Convert#codepoint(char)}
     */
    @Test
    public void test_codepoint() {
        Assert.assertEquals("Convert uppercase", 65, new Convert().codepoint('A'));
        Assert.assertEquals("Convert lowercase", 98, new Convert().codepoint('b'));
        Assert.assertEquals("Convert digit", 48, new Convert().codepoint('0'));
        Assert.assertEquals("Convert special", 33, new Convert().codepoint('!'));
        Assert.assertEquals("Convert control", 10, new Convert().codepoint('\n'));
        Assert.assertEquals("Convert Latin-1 Supplement", 169, new Convert().codepoint('\u00a9'));      // (c) symbol > 128
        Assert.assertEquals("Convert currency", 8364, new Convert().codepoint('\u20ac'));               // euro symbol > 256
    }
}