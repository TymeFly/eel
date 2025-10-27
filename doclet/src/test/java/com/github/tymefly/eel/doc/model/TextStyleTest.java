package com.github.tymefly.eel.doc.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link TextStyle}
 */
public class TextStyleTest {

    /**
     * Unit test {@link TextStyle#clean()}
     */
    @Test
    public void test_clean() {
        Assert.assertTrue("NONE", TextStyle.NONE.clean());
        Assert.assertTrue("RAW", TextStyle.RAW.clean());
        Assert.assertFalse("LITERAL", TextStyle.LITERAL.clean());
        Assert.assertFalse("CODE", TextStyle.CODE.clean());
        Assert.assertFalse("LINK", TextStyle.LINK.clean());
        Assert.assertFalse("PLAIN_LINK", TextStyle.PLAIN_LINK.clean());
        Assert.assertFalse("ERROR", TextStyle.ERROR.clean());
    }

    /**
     * Unit test {@link TextStyle#escape()}
     */
    @Test
    public void test_escape() {
        Assert.assertFalse("NONE", TextStyle.NONE.escape());
        Assert.assertFalse("RAW", TextStyle.RAW.escape());
        Assert.assertFalse("LITERAL", TextStyle.LITERAL.escape());
        Assert.assertFalse("CODE", TextStyle.CODE.escape());
        Assert.assertTrue("LINK", TextStyle.LINK.escape());
        Assert.assertTrue("PLAIN_LINK", TextStyle.PLAIN_LINK.escape());
        Assert.assertFalse("ERROR", TextStyle.ERROR.escape());
    }
}