package com.github.tymefly.eel.doc.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit test for {@link TextUtils}
 */
public class TextUtilsTest {

    /**
     * Unit test {@link TextUtils#capitalise(String)} 
     */
    @Test
    public void test_capitalise() {
        assertEquals("", TextUtils.capitalise(""), "Empty String");
        assertEquals("", TextUtils.capitalise("   \t   "), "White space");
        assertEquals("Abcdef", TextUtils.capitalise("abcdef"), "lower case");
        assertEquals("ABCDEF", TextUtils.capitalise("ABCDEF"), "upper case");
        assertEquals("One two. Three", TextUtils.capitalise("one two. Three"), "multiple words");
        assertEquals("X", TextUtils.capitalise("x"), "single char");
        assertEquals("One two. Three  ", TextUtils.capitalise("  one two. Three  "), "leading and trailing spaces");
    }
}