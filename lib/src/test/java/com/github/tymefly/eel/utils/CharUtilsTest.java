package com.github.tymefly.eel.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link CharUtils}
 */
public class CharUtilsTest {

    /**
     * Unit test {@link CharUtils#isAsciiPrintable(char)}
     */
    @Test
    public void test_IsAsciiPrintable() {
        Assert.assertFalse("Null", CharUtils.isAsciiPrintable((char) 0x00));
        Assert.assertFalse("ESC", CharUtils.isAsciiPrintable((char) 0x1b));
        Assert.assertFalse("Unit Separator", CharUtils.isAsciiPrintable((char) 0x1f));

        Assert.assertTrue("Space", CharUtils.isAsciiPrintable(' '));
        Assert.assertTrue("'0'", CharUtils.isAsciiPrintable('0'));
        Assert.assertTrue("'A'", CharUtils.isAsciiPrintable('A'));
        Assert.assertTrue("'a'", CharUtils.isAsciiPrintable('a'));
        Assert.assertTrue("'|'", CharUtils.isAsciiPrintable('|'));
        Assert.assertTrue("'~'", CharUtils.isAsciiPrintable('~'));

        Assert.assertFalse("Delete", CharUtils.isAsciiPrintable((char) 0x7f));
        Assert.assertFalse("0x80", CharUtils.isAsciiPrintable((char) 0x80));
        Assert.assertFalse("0x100", CharUtils.isAsciiPrintable((char) 0x100));
    }
}