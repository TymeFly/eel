package com.github.tymefly.eel.doc.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link EelType}
 */
public class EelTypeTest {

    /**
     * Unit test {@link EelType#toString}
     */
    @Test
    public void test_toString() {
        assertEquals("Text", EelType.TEXT.toString(), "TEXT");
        assertEquals("Number", EelType.NUMBER.toString(), "NUMBER");
        assertEquals("Logic", EelType.LOGIC.toString(), "LOGIC");
        assertEquals("Date", EelType.DATE.toString(), "DATE");
        assertEquals("Value", EelType.VALUE.toString(), "VALUE");
    }
}