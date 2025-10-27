package com.github.tymefly.eel.doc.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelType}
 */
public class EelTypeTest {

    /**
     * Unit test {@link EelType#toString}
     */
    @Test
    public void test_toString() {
        Assert.assertEquals("TEXT", "Text", EelType.TEXT.toString());
        Assert.assertEquals("NUMBER", "Number", EelType.NUMBER.toString());
        Assert.assertEquals("LOGIC", "Logic", EelType.LOGIC.toString());
        Assert.assertEquals("DATE", "Date", EelType.DATE.toString());
        Assert.assertEquals("VALUE", "Value", EelType.VALUE.toString());
    }
}