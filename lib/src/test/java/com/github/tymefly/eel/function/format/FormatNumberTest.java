package com.github.tymefly.eel.function.format;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link FormatNumber}
 */
public class FormatNumberTest {
    private FormatNumber formatter;

    @BeforeEach
    public void setUp() {
        formatter = new FormatNumber();
    }

    /**
     * Unit test {@link FormatNumber#formatBinary(BigInteger)}
     */
    @Test
    public void test_formatBinary() {
        assertEquals("101001011010", formatter.formatBinary(BigInteger.valueOf(0xa5a)), "#1 Binary");
        assertEquals("-101001011010", formatter.formatBinary(BigInteger.valueOf(-0xa5a)), "#2 Binary");
    }

    /**
     * Unit test {@link FormatNumber#formatOctal(BigInteger)}
     */
    @Test
    public void test_formatOctal() {
        assertEquals("11145401322", formatter.formatOctal(BigInteger.valueOf(1234567890)), "#1 Oct");
        assertEquals("-7267464261", formatter.formatOctal(BigInteger.valueOf(-987654321)), "#2 Oct");
    }

    /**
     * Unit test {@link FormatNumber#formatHex(BigInteger)}
     */
    @Test
    public void test_formatHex() {
        assertEquals("499602d2", formatter.formatHex(BigInteger.valueOf(1234567890)), "#1 Hex");
        assertEquals("-3ade68b1", formatter.formatHex(BigInteger.valueOf(-987654321)), "#2 Hex");
    }

    /**
     * Unit test {@link FormatNumber#formatNumber(BigInteger, int)}
     */
    @Test
    public void test_formatNumber() {
        assertEquals("1234567890", formatter.formatNumber(BigInteger.valueOf(1234567890), 10), "#1 Decimal");
        assertEquals("11145401322", formatter.formatNumber(BigInteger.valueOf(1234567890), 8), "#1 Octal");
        assertEquals("499602d2", formatter.formatNumber(BigInteger.valueOf(1234567890), 16), "#1 Hex");
        assertEquals("1001001100101100000001011010010", formatter.formatNumber(BigInteger.valueOf(1234567890), 2), "#1 Binary");

        assertEquals("-987654321", formatter.formatNumber(BigInteger.valueOf(-987654321), 10), "#2 Decimal");
        assertEquals("-7267464261", formatter.formatNumber(BigInteger.valueOf(-987654321), 8), "#2 Octal");
        assertEquals("-3ade68b1", formatter.formatNumber(BigInteger.valueOf(-987654321), 16), "#2 Hex");
        assertEquals("-111010110111100110100010110001", formatter.formatNumber(BigInteger.valueOf(-987654321), 2), "#2 Binary");
    }

    /**
     * Unit test {@link FormatNumber#formatNumber(BigInteger, int)}
     */
    @Test
    public void test_formatNumber_radixRange() {
        assertEquals("10011010010", formatter.formatNumber(BigInteger.valueOf(1234), Character.MIN_RADIX), "Min Radix");
        assertEquals("ya", formatter.formatNumber(BigInteger.valueOf(1234), Character.MAX_RADIX), "Max Radix");

        assertThrows(IllegalArgumentException.class,
            () -> formatter.formatNumber(BigInteger.valueOf(1234), Character.MIN_RADIX - 1),
            "Below Mix Radix");
        assertThrows(IllegalArgumentException.class,
            () -> formatter.formatNumber(BigInteger.valueOf(1234), Character.MAX_RADIX + 1),
            "Above Max Radix");
    }
}