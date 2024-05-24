package com.github.tymefly.eel.function.format;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link FormatNumber}
 */
public class FormatNumberTest {
    private FormatNumber formatter;

    @Before
    public void setUp() {
        formatter = new FormatNumber();
    }

    /**
     * Unit test {@link FormatNumber#formatBinary(BigInteger)}
     */
    @Test
    public void test_formatBinary() {
        Assert.assertEquals("#1 Binary",
            "101001011010",
            formatter.formatBinary(BigInteger.valueOf(0xa5a)));
        Assert.assertEquals("#2 Binary",
            "-101001011010",
            formatter.formatBinary(BigInteger.valueOf(-0xa5a)));
    }

    /**
     * Unit test {@link FormatNumber#formatOctal(BigInteger)}
     */
    @Test
    public void test_formatOctal() {
        Assert.assertEquals("#1 Oct", "11145401322", formatter.formatOctal(BigInteger.valueOf(1234567890)));
        Assert.assertEquals("#2 Oct", "-7267464261", formatter.formatOctal(BigInteger.valueOf(-987654321)));
    }

    /**
     * Unit test {@link FormatNumber#formatHex(BigInteger)}
     */
    @Test
    public void test_formatHex() {
        Assert.assertEquals("#1 Hex", "499602d2", formatter.formatHex(BigInteger.valueOf(1234567890)));
        Assert.assertEquals("#2 Hex", "-3ade68b1", formatter.formatHex(BigInteger.valueOf(-987654321)));
    }

    /**
     * Unit test {@link FormatNumber#formatNumber(BigInteger, int)}
     */
    @Test
    public void test_formatNumber() {
        Assert.assertEquals("#1 Decimal", "1234567890", formatter.formatNumber(BigInteger.valueOf(1234567890), 10));
        Assert.assertEquals("#1 Octal", "11145401322", formatter.formatNumber(BigInteger.valueOf(1234567890), 8));
        Assert.assertEquals("#1 Hex", "499602d2", formatter.formatNumber(BigInteger.valueOf(1234567890), 16));
        Assert.assertEquals("#1 Binary", "1001001100101100000001011010010", formatter.formatNumber(BigInteger.valueOf(1234567890), 2));

        Assert.assertEquals("#2 Decimal", "-987654321", formatter.formatNumber(BigInteger.valueOf(-987654321), 10));
        Assert.assertEquals("#2 Octal", "-7267464261", formatter.formatNumber(BigInteger.valueOf(-987654321), 8));
        Assert.assertEquals("#2 Hex", "-3ade68b1", formatter.formatNumber(BigInteger.valueOf(-987654321), 16));
        Assert.assertEquals("#2 Binary", "-111010110111100110100010110001", formatter.formatNumber(BigInteger.valueOf(-987654321), 2));
    }

    /**
     * Unit test {@link FormatNumber#formatNumber(BigInteger, int)}
     */
    @Test
    public void test_formatNumber_radixRange() {
        Assert.assertEquals("Min Radix", "10011010010", formatter.formatNumber(BigInteger.valueOf(1234), Character.MIN_RADIX));
        Assert.assertEquals("Max Radix", "ya", formatter.formatNumber(BigInteger.valueOf(1234), Character.MAX_RADIX));

        Assert.assertThrows("Below Mix Radix",
            IllegalArgumentException.class,
            () -> formatter.formatNumber(BigInteger.valueOf(1234), Character.MIN_RADIX - 1));
        Assert.assertThrows("Above Max Radix",
            IllegalArgumentException.class,
            () -> formatter.formatNumber(BigInteger.valueOf(1234), Character.MAX_RADIX + 1));
    }
}