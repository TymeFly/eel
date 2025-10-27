package com.github.tymefly.eel.doc.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link TextUtils}
 */
public class TextUtilsTest {

    /**
     * Unit test {@link TextUtils#capitalise(String)} 
     */
    @Test
    public void test_capitalise() {
        Assert.assertEquals("Empty String", "", TextUtils.capitalise(""));
        Assert.assertEquals("White space", "", TextUtils.capitalise("   \t   "));
        Assert.assertEquals("lower case", "Abcdef", TextUtils.capitalise("abcdef"));
        Assert.assertEquals("upper case", "ABCDEF", TextUtils.capitalise("ABCDEF"));
        Assert.assertEquals("multiple words", "One two. Three", TextUtils.capitalise("one two. Three"));
        Assert.assertEquals("single char", "X", TextUtils.capitalise("x"));
        Assert.assertEquals("leading and trailing spaces", "One two. Three  ", TextUtils.capitalise("  one two. Three  "));
    }
}