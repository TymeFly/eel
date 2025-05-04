package com.github.tymefly.eel.function.general;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Codepoints}
 */
public class CodepointsTest {
    /**
     * Unit test {@link Codepoints#toChar(int)}
     */
    @Test
    public void test_toChar() {
        Assert.assertEquals("Convert uppercase", "A", new Codepoints().toChar(65));
        Assert.assertEquals("Convert lowercase", "b", new Codepoints().toChar(98));
        Assert.assertEquals("Convert digit", "0", new Codepoints().toChar(48));
        Assert.assertEquals("Convert special", "!", new Codepoints().toChar(33));
        Assert.assertEquals("Convert control", "\n", new Codepoints().toChar(10));
        Assert.assertEquals("Convert Latin-1 Supplement", "\u00a9", new Codepoints().toChar(169));     // (c) symbol > 128
        Assert.assertEquals("Convert currency", "\u20ac", new Codepoints().toChar(8364));              // euro symbol > 256
    }

    /**
     * Unit test {@link Codepoints#codepoint(char)}
     */
    @Test
    public void test_codepoint() {
        Assert.assertEquals("Convert uppercase", 65, new Codepoints().codepoint('A'));
        Assert.assertEquals("Convert lowercase", 98, new Codepoints().codepoint('b'));
        Assert.assertEquals("Convert digit", 48, new Codepoints().codepoint('0'));
        Assert.assertEquals("Convert special", 33, new Codepoints().codepoint('!'));
        Assert.assertEquals("Convert control", 10, new Codepoints().codepoint('\n'));
        Assert.assertEquals("Convert Latin-1 Supplement", 169, new Codepoints().codepoint('\u00a9'));      // (c) symbol > 128
        Assert.assertEquals("Convert currency", 8364, new Codepoints().codepoint('\u20ac'));               // euro symbol > 256
    }
}