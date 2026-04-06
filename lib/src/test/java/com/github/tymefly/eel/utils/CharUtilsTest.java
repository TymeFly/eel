package com.github.tymefly.eel.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link CharUtils}
 */
public class CharUtilsTest {

    /**
     * Unit test {@link CharUtils#isAsciiPrintable(char)}
     */
    @Test
    public void test_IsAsciiPrintable() {
        assertFalse(CharUtils.isAsciiPrintable((char) 0x00), "Null");
        assertFalse(CharUtils.isAsciiPrintable((char) 0x1b), "ESC");
        assertFalse(CharUtils.isAsciiPrintable((char) 0x1f), "Unit Separator");

        assertTrue(CharUtils.isAsciiPrintable(' '), "Space");
        assertTrue(CharUtils.isAsciiPrintable('0'), "'0'");
        assertTrue(CharUtils.isAsciiPrintable('A'), "'A'");
        assertTrue(CharUtils.isAsciiPrintable('a'), "'a'");
        assertTrue(CharUtils.isAsciiPrintable('|'), "'|'");
        assertTrue(CharUtils.isAsciiPrintable('~'), "'~'");

        assertFalse(CharUtils.isAsciiPrintable((char) 0x7f), "Delete");
        assertFalse(CharUtils.isAsciiPrintable((char) 0x80), "0x80");
        assertFalse(CharUtils.isAsciiPrintable((char) 0x100), "0x100");
    }

    /**
     * Unit test {@link CharUtils#hexValue(char)}
     */
    @Test
    public void test_hexValue_HappyPath() {
        assertEquals(0, CharUtils.hexValue('0'), "Test 0");
        assertEquals(1, CharUtils.hexValue('1'), "Test 1");
        assertEquals(2, CharUtils.hexValue('2'), "Test 2");
        assertEquals(3, CharUtils.hexValue('3'), "Test 3");
        assertEquals(4, CharUtils.hexValue('4'), "Test 4");
        assertEquals(5, CharUtils.hexValue('5'), "Test 5");
        assertEquals(6, CharUtils.hexValue('6'), "Test 6");
        assertEquals(7, CharUtils.hexValue('7'), "Test 7");
        assertEquals(8, CharUtils.hexValue('8'), "Test 8");
        assertEquals(9, CharUtils.hexValue('9'), "Test 9");

        assertEquals(10, CharUtils.hexValue('a'), "Test a");
        assertEquals(11, CharUtils.hexValue('b'), "Test b");
        assertEquals(12, CharUtils.hexValue('c'), "Test c");
        assertEquals(13, CharUtils.hexValue('d'), "Test d");
        assertEquals(14, CharUtils.hexValue('e'), "Test e");
        assertEquals(15, CharUtils.hexValue('f'), "Test f");

        assertEquals(10, CharUtils.hexValue('A'), "Test A");
        assertEquals(11, CharUtils.hexValue('B'), "Test B");
        assertEquals(12, CharUtils.hexValue('C'), "Test C");
        assertEquals(13, CharUtils.hexValue('D'), "Test D");
        assertEquals(14, CharUtils.hexValue('E'), "Test E");
        assertEquals(15, CharUtils.hexValue('F'), "Test F");
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

        assertEquals(21, valid, "Unexpected valid count");
        assertEquals(65515, invalid, "Unexpected invalid count");
    }
}