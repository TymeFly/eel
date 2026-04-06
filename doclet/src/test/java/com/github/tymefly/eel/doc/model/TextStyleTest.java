package com.github.tymefly.eel.doc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link TextStyle}
 */
public class TextStyleTest {

    /**
     * Unit test {@link TextStyle#clean()}
     */
    @Test
    public void test_clean() {
        assertTrue(TextStyle.NONE.clean(), "NONE");
        assertTrue(TextStyle.RAW.clean(), "RAW");
        assertFalse(TextStyle.LITERAL.clean(), "LITERAL");
        assertFalse(TextStyle.CODE.clean(), "CODE");
        assertFalse(TextStyle.LINK.clean(), "LINK");
        assertFalse(TextStyle.PLAIN_LINK.clean(), "PLAIN_LINK");
        assertFalse(TextStyle.ERROR.clean(), "ERROR");
    }

    /**
     * Unit test {@link TextStyle#escape()}
     */
    @Test
    public void test_escape() {
        assertFalse(TextStyle.NONE.escape(), "NONE");
        assertFalse(TextStyle.RAW.escape(), "RAW");
        assertFalse(TextStyle.LITERAL.escape(), "LITERAL");
        assertFalse(TextStyle.CODE.escape(), "CODE");
        assertTrue(TextStyle.LINK.escape(), "LINK");
        assertTrue(TextStyle.PLAIN_LINK.escape(), "PLAIN_LINK");
        assertFalse(TextStyle.ERROR.escape(), "ERROR");
    }
}