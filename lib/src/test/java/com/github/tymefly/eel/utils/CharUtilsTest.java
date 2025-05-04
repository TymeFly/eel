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

    /**
     * Unit test {@link CharUtils#hexValue(char)}
     */
    @Test
    public void test_hexValue_HappyPath() {
        Assert.assertEquals("Test 0", 0, CharUtils.hexValue('0'));
        Assert.assertEquals("Test 1", 1, CharUtils.hexValue('1'));
        Assert.assertEquals("Test 2", 2, CharUtils.hexValue('2'));
        Assert.assertEquals("Test 3", 3, CharUtils.hexValue('3'));
        Assert.assertEquals("Test 4", 4, CharUtils.hexValue('4'));
        Assert.assertEquals("Test 5", 5, CharUtils.hexValue('5'));
        Assert.assertEquals("Test 6", 6, CharUtils.hexValue('6'));
        Assert.assertEquals("Test 7", 7, CharUtils.hexValue('7'));
        Assert.assertEquals("Test 8", 8, CharUtils.hexValue('8'));
        Assert.assertEquals("Test 9", 9, CharUtils.hexValue('9'));

        Assert.assertEquals("Test a", 10, CharUtils.hexValue('a'));
        Assert.assertEquals("Test b", 11, CharUtils.hexValue('b'));
        Assert.assertEquals("Test c", 12, CharUtils.hexValue('c'));
        Assert.assertEquals("Test d", 13, CharUtils.hexValue('d'));
        Assert.assertEquals("Test e", 14, CharUtils.hexValue('e'));
        Assert.assertEquals("Test f", 15, CharUtils.hexValue('f'));

        Assert.assertEquals("Test A", 10, CharUtils.hexValue('A'));
        Assert.assertEquals("Test B", 11, CharUtils.hexValue('B'));
        Assert.assertEquals("Test C", 12, CharUtils.hexValue('C'));
        Assert.assertEquals("Test D", 13, CharUtils.hexValue('D'));
        Assert.assertEquals("Test E", 14, CharUtils.hexValue('E'));
        Assert.assertEquals("Test F", 15, CharUtils.hexValue('F'));
    }

    /**
     * Unit test {@link CharUtils#hexValue(char)}
     */
    @Test
    public void test_hexValue_AllValues() {
        char test = 0;
        int valid = 0;
        int invalid = 0;

        do {
            int result = CharUtils.hexValue(test);

            if (result > 0) {
                valid++;
            } else {
                invalid++;
            }
        } while (++test !=0);

        Assert.assertEquals("Unexpected valid count", 21, valid);
        Assert.assertEquals("Unexpected invalid count", 65515, invalid);
    }
}