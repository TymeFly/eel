package com.github.tymefly.eel;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link NumberParser}
 */
public class NumberParserTest {
    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_empty() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse(""));

        Assert.assertEquals("Unexpected message", "For input \"\"", actual.getMessage());
    }

                //*** Decimal ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal() {
        validate("1234567890", new BigDecimal("1234567890"));
        validate(".1", new BigDecimal(".1"));
        validate("+200", new BigDecimal("200"));
        validate("-3.00", new BigDecimal("-3.00"));
        validate("4.44", new BigDecimal("4.44"));
        validate("5e2", new BigDecimal("500"));
        validate("6E-3", new BigDecimal("0.006"));
        validate("7.3e10", new BigDecimal("73000000000"));
        validate("8.", new BigDecimal("8"));
        validate("-9", new BigDecimal("-9"));
        validate("+9.9", new BigDecimal("9.9"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_withUnderscores() {
        validate(".1_00", new BigDecimal(".1"));
        validate("+2_000", new BigDecimal("2000"));
        validate("-0_3.00", new BigDecimal("-3.00"));
        validate("4.4_4", new BigDecimal("4.44"));
        validate("5_0e2_0", new BigDecimal("5.0E+21"));
        validate("6_0E-3_0", new BigDecimal("6.0E-29"));
        validate("7_00.3e1_000", new BigDecimal("7.003E+1002"));
        validate("-9_00", new BigDecimal("-900"));
        validate("+9_99.9", new BigDecimal("+999.9"));
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_unexpectedChars() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("123a"));

        Assert.assertEquals("Unexpected message", "For input string: \"123a\"", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_source() {
        validate("10 and a bit", new BigDecimal("10"), " and a bit");
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_number_missingExponent() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1.2e "));

        Assert.assertEquals("Unexpected message", "Expected exponent", actual.getMessage());
    }

                //*** Binary ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary() {
        validate("0b10", new BigDecimal("2"));
        validate("0b0101", new BigDecimal("5"));
        validate("0B1010", new BigDecimal("10"));
        validate("+0B101", new BigDecimal("5"));
        validate("-0B101", new BigDecimal("-5"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_withUnderscores() {
        validate("0b01_01", new BigDecimal("5"));
        validate("0B10_10", new BigDecimal("10"));
        validate("+0B1_01", new BigDecimal("5"));
        validate("-0B1_01", new BigDecimal("-5"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_unexpectedChars() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0b012"));

        Assert.assertEquals("Unexpected message", "For input string: \"0b012\"", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_missingDigits() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0b"));

        Assert.assertEquals("Unexpected message", "Expected digits", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_source() {
        validate("0B10+5", new BigDecimal("2"), "+5");
    }


                //*** Octal ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal() {
        validate("0c12345670", new BigDecimal("2739128"));
        validate("0c773", new BigDecimal("507"));
        validate("0C532", new BigDecimal("346"));
        validate("+0C123", new BigDecimal("83"));
        validate("-0C123", new BigDecimal("-83"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_withUnderscores() {
        validate("0c77_3", new BigDecimal("507"));
        validate("0C5_32", new BigDecimal("346"));
        validate("+0C1_23", new BigDecimal("83"));
        validate("-0C1_23", new BigDecimal("-83"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_unexpectedChars() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0c6789"));

        Assert.assertEquals("Unexpected message", "For input string: \"0c6789\"", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_missingDigits() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0C"));

        Assert.assertEquals("Unexpected message", "Expected digits", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_source() {
        validate("0c123Hello", new BigDecimal("83"), "Hello");
    }

                //*** Hex ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex() {
        validate("0x123456789abcdef", new BigDecimal("81985529216486895"));
        validate("0xfe", new BigDecimal("254"));
        validate("0Xa5", new BigDecimal("165"));
        validate("+0x1fe", new BigDecimal("510"));
        validate("-0x1fe", new BigDecimal("-510"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_withUnderscores() {
        validate("0xff_fe", new BigDecimal("65534"));
        validate("0Xa5_5a", new BigDecimal("42330"));
        validate("+0x1_fe", new BigDecimal("510"));
        validate("-0x1_fe", new BigDecimal("-510"));
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_unexpectedChars() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0X89abcdefg"));

        Assert.assertEquals("Unexpected message", "For input string: \"0X89abcdefg\"", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_missingDigits() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0x"));

        Assert.assertEquals("Unexpected message", "Expected digits", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_source() {
        validate("0xbabe!", new BigDecimal("47806"), "!");
    }


                //*** Broken underscores ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_double() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1__2"));

        Assert.assertEquals("Unexpected message", "Unexpected char '_' (0x5f)", actual.getMessage());
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_lastChar() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse(".1_"));

        Assert.assertEquals("Unexpected message", "Unexpected char '_' (0x5f)", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforePoint() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1_."));

        Assert.assertEquals("Unexpected message", "Unexpected char '_' (0x5f)", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_afterPoint() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1._2"));

        Assert.assertEquals("Unexpected message", "Unexpected char '_' (0x5f)", actual.getMessage());
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforeExponent() {
        NumberFormatException actual = Assert.assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1_e3"));

        Assert.assertEquals("Unexpected message", "Unexpected char '_' (0x5f)", actual.getMessage());
    }



    private void validate(@Nonnull String literal, @Nonnull BigDecimal expectedValue) {
        BigDecimal value = NumberParser.parse(literal);

        Assert.assertTrue("Unexpected value for " + literal + ": expected " + expectedValue +
                " but was " + value.toPlainString(),
            expectedValue.compareTo(value) == 0);
    }

    private void validate(@Nonnull String literal, @Nonnull BigDecimal expectedValue, @Nonnull String trailing) {
        Source source = Source.build(literal, literal.length());
        BigDecimal value = NumberParser.parse(source);

        Assert.assertTrue("Unexpected value for " + literal + ": expected " + expectedValue +
                " but was " + value.toPlainString(),
            expectedValue.compareTo(value) == 0);

        int index = 0;
        for (var next : trailing.toCharArray()) {
            Assert.assertEquals(index + ") unexpected trailing character", next, source.current());
            source.read();
            index++;
        }

        Assert.assertEquals("Extra trailing character" + source.current(), source.current(), Input.END);
    }
}