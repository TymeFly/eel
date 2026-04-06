package com.github.tymefly.eel;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link NumberParser}
 */
public class NumberParserTest {
    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_empty() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse(""));

        assertEquals("For input \"\"", actual.getMessage(), "Unexpected message");
    }

                //*** Decimal ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal() {
        validate("1234567890", new BigDecimal("1234567890"), true);
        validate(".1", new BigDecimal(".1"), false);
        validate("+200", new BigDecimal("200"), true);
        validate("-200", new BigDecimal("-200"), true);
        validate("+3.00", new BigDecimal("3.00"), false);
        validate("-3.00", new BigDecimal("-3.00"), false);
        validate("4.44", new BigDecimal("4.44"), false);
        validate("5e2", new BigDecimal("500"), false);
        validate("6E-3", new BigDecimal("0.006"), false);
        validate("7.3e10", new BigDecimal("73000000000"), false);
        validate("8.", new BigDecimal("8"), false);
        validate("-9", new BigDecimal("-9"), true);
        validate("+9.9", new BigDecimal("9.9"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_withUnderscores() {
        validate(".1_00", new BigDecimal(".1"), false);
        validate("+2_000", new BigDecimal("2000"), true);
        validate("-0_3.00", new BigDecimal("-3.00"), false);
        validate("4.4_4", new BigDecimal("4.44"), false);
        validate("5_0e2_0", new BigDecimal("5.0E+21"), false);
        validate("6_0E-3_0", new BigDecimal("6.0E-29"), false);
        validate("7_00.3e1_000", new BigDecimal("7.003E+1002"), false);
        validate("-9_00", new BigDecimal("-900"), true);
        validate("+9_99.9", new BigDecimal("+999.9"), false);
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_unexpectedChars() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("123a"));

        assertEquals("For input string: \"123a\"", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_decimal_source() {
        validate("10 and a bit", new BigDecimal("10"), "10", " and a bit");
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_number_missingExponent() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1.2e "));

        assertEquals("Expected exponent", actual.getMessage(), "Unexpected message");
    }

                //*** Binary ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary() {
        validate("0b10", new BigDecimal("2"), false);
        validate("0b0101", new BigDecimal("5"), false);
        validate("0B1010", new BigDecimal("10"), false);
        validate("+0B101", new BigDecimal("5"), false);
        validate("-0B101", new BigDecimal("-5"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_withUnderscores() {
        validate("0b01_01", new BigDecimal("5"), false);
        validate("0B10_10", new BigDecimal("10"), false);
        validate("+0B1_01", new BigDecimal("5"), false);
        validate("-0B1_01", new BigDecimal("-5"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_unexpectedChars() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0b012"));

        assertEquals("For input string: \"0b012\"", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_missingDigits() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0b"));

        assertEquals("Expected digits", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_binary_source() {
        validate("0B10+5", new BigDecimal("2"), "0B10", "+5");
    }


                //*** Octal ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal() {
        validate("0c12345670", new BigDecimal("2739128"), false);
        validate("0c773", new BigDecimal("507"), false);
        validate("0C532", new BigDecimal("346"), false);
        validate("+0C123", new BigDecimal("83"), false);
        validate("-0C123", new BigDecimal("-83"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_withUnderscores() {
        validate("0c77_3", new BigDecimal("507"), false);
        validate("0C5_32", new BigDecimal("346"), false);
        validate("+0C1_23", new BigDecimal("83"), false);
        validate("-0C1_23", new BigDecimal("-83"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_unexpectedChars() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0c6789"));

        assertEquals("For input string: \"0c6789\"", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_missingDigits() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0C"));

        assertEquals("Expected digits", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_octal_source() {
        validate("0c123Hello", new BigDecimal("83"), "0c123", "Hello");
    }

                //*** Hex ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex() {
        validate("0x123456789abcdef", new BigDecimal("81985529216486895"), false);
        validate("0xfe", new BigDecimal("254"), false);
        validate("0Xa5", new BigDecimal("165"), false);
        validate("+0x1fe", new BigDecimal("510"), false);
        validate("-0x1fe", new BigDecimal("-510"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_withUnderscores() {
        validate("0xff_fe", new BigDecimal("65534"), false);
        validate("0Xa5_5a", new BigDecimal("42330"), false);
        validate("+0x1_fe", new BigDecimal("510"), false);
        validate("-0x1_fe", new BigDecimal("-510"), false);
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_unexpectedChars() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0X89abcdefg"));

        assertEquals("For input string: \"0X89abcdefg\"", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_missingDigits() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("0x"));

        assertEquals("Expected digits", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_hex_source() {
        validate("0xbabe!", new BigDecimal("47806"), "0xbabe", "!");
    }


                //*** Broken underscores ***//

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_double() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1__2"));

        assertEquals("Unexpected char '_' (0x5f)", actual.getMessage(), "Unexpected message");
    }


    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_lastChar() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse(".1_"));

        assertEquals("Unexpected char '_' (0x5f)", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforePoint() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1_."));

        assertEquals("Unexpected char '_' (0x5f)", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_afterPoint() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1._2"));

        assertEquals("Unexpected char '_' (0x5f)", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link NumberParser#parse(Input)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforeExponent() {
        NumberFormatException actual = assertThrows(NumberFormatException.class,
            () -> NumberParser.parse("1_e3"));

        assertEquals("Unexpected char '_' (0x5f)", actual.getMessage(), "Unexpected message");
    }



    private void validate(@Nonnull String literal, @Nonnull BigDecimal expectedValue, boolean expectedDecimal) {
        NumberParser parser = NumberParser.parse(literal);
        BigDecimal value = parser.getValue();
        String text = parser.getText();

        assertTrue(expectedValue.compareTo(value) == 0,
            "Unexpected value for " + literal + ": expected " + expectedValue + " but was " + value.toPlainString());
        assertEquals(text, literal, "Unexpected text for " + literal);
        assertEquals(expectedDecimal, parser.isDecimal(), "Unexpected decimal flag for " + literal);
    }

    private void validate(@Nonnull String literal,
                          @Nonnull BigDecimal expectedValue,
                          @Nonnull String expectedText,
                          @Nonnull String trailing) {
        Source source = Source.build(literal, literal.length());
        NumberParser parser = NumberParser.parse(source);
        BigDecimal value = parser.getValue();
        String text = parser.getText();

        assertTrue(expectedValue.compareTo(value) == 0,
            "Unexpected value for " + literal + ": expected " + expectedValue + " but was " + value.toPlainString());

        assertEquals(text, expectedText, "Unexpected text for " + literal);

        int index = 0;
        for (var next : trailing.toCharArray()) {
            assertEquals(next, source.current(), index + ") unexpected trailing character");
            source.read();
            index++;
        }

        assertEquals(source.current(), Input.END, "Extra trailing character" + source.current());
    }
}