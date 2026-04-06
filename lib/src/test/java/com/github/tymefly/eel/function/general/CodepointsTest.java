package com.github.tymefly.eel.function.general;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Codepoints}
 */
public class CodepointsTest {
    /**
     * Unit test {@link Codepoints#toChar(int)}
     */
    @Test
    public void test_toChar() {
        assertEquals("A", new Codepoints().toChar(65), "Convert uppercase");
        assertEquals("b", new Codepoints().toChar(98), "Convert lowercase");
        assertEquals("0", new Codepoints().toChar(48), "Convert digit");
        assertEquals("!", new Codepoints().toChar(33), "Convert special");
        assertEquals("\n", new Codepoints().toChar(10), "Convert control");
        assertEquals("\u00a9", new Codepoints().toChar(169), "Convert Latin-1 Supplement");     // (c) symbol > 128
        assertEquals("\u20ac", new Codepoints().toChar(8364), "Convert currency");              // euro symbol > 256

        assertEquals("\u0000", new Codepoints().toChar(0), "zero");
        assertThrows(IllegalArgumentException.class, () -> new Codepoints().toChar(-1), "negative");
    }

    /**
     * Unit test {@link Codepoints#codepoint(char)}
     */
    @Test
    public void test_codepoint() {
        assertEquals(65, new Codepoints().codepoint('A'), "Convert uppercase");
        assertEquals(98, new Codepoints().codepoint('b'), "Convert lowercase");
        assertEquals(48, new Codepoints().codepoint('0'), "Convert digit");
        assertEquals(33, new Codepoints().codepoint('!'), "Convert special");
        assertEquals(10, new Codepoints().codepoint('\n'), "Convert control");
        assertEquals(169, new Codepoints().codepoint('\u00a9'), "Convert Latin-1 Supplement");      // (c) symbol > 128
        assertEquals(8364, new Codepoints().codepoint('\u20ac'), "Convert currency");               // euro symbol > 256
    }
}