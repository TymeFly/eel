package com.github.tymefly.eel;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.Tokenizer.Terminal;
import com.github.tymefly.eel.exception.EelSyntaxException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Tokenizer}
 */
public class TokenizerTest {
    @Test
    public void test_empty() {
        Terminal actual = buildTokenizer("").interpolate(Token.END_OF_PROGRAM);

        assertToken(1, actual, Token.END_OF_PROGRAM, "", 0);
    }


    @Test
    public void test_whitespace() {
        Terminal actual = buildTokenizer(" \t \t  ").interpolate(Token.END_OF_PROGRAM);

        assertToken(1, actual, Token.END_OF_PROGRAM, "", 7);
    }

    @Test
    public void test_comments() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("## ignore me ## 123 ## ignore me");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "123", new BigDecimal("123"), true, 17);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 21);
    }

    /**
     * Unit Test {@link Tokenizer#text(Token)} 
     */
    @Test
    public void test_interpolate() {
        assertToken(1, buildTokenizer("").text(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 0);
        assertToken(1, buildTokenizer("some text").text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "some text", 1);
        assertToken(1, buildTokenizer("  some text  ").text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "  some text  ", 1);
        assertToken(1, buildTokenizer("xxx $(").text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "xxx ", 1);
        assertToken(1, buildTokenizer("___${").text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "___", 1);
        assertToken(1, buildTokenizer("$1.23").text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "$1.23", 1);
        assertToken(1, buildTokenizer("$_").text(Token.END_OF_PROGRAM), Token.FUNCTION_INTERPOLATION, "$", 1);
        assertToken(1, buildTokenizer("${abc}").text(Token.END_OF_PROGRAM), Token.VALUE_INTERPOLATION, "${", 1);
        assertToken(1, buildTokenizer("$(abc)").text(Token.END_OF_PROGRAM), Token.EXPRESSION_INTERPOLATION, "$(", 1);
        assertToken(1, buildTokenizer("$func()").text(Token.END_OF_PROGRAM), Token.FUNCTION_INTERPOLATION, "$", 1);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_quotes() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("' \"");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 4);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)}
     */
    @Test
    public void test_strings() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("'Hello World' \"foo\" ");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 1);
        assertToken(++index, tokenizer.text(Token.SINGLE_QUOTE), Token.TEXT_LITERAL, "Hello World", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 13);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 15);
        assertToken(++index, tokenizer.text(Token.DOUBLE_QUOTE), Token.TEXT_LITERAL, "foo", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 19);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 21);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_strings_escaped_SingleQuote() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("'~ BackSlash:\\\\ Single Quote:\\' Double Quote:\\\" ~'");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 1);
        assertToken(++index, tokenizer.text(Token.SINGLE_QUOTE), Token.TEXT_LITERAL, "~ BackSlash:\\ Single Quote:' Double Quote:\" ~", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 50);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 51);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_strings_escaped_DoubleQuote() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("\"~ BackSlash:\\\\ Single Quote:\\' Double Quote:\\\" ~\"");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 1);
        assertToken(++index, tokenizer.text(Token.DOUBLE_QUOTE), Token.TEXT_LITERAL, "~ BackSlash:\\ Single Quote:' Double Quote:\" ~", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 50);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 51);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_strings_escaped_Others() {
        int index = 0;
        Tokenizer tokenizer =
            buildTokenizer("\"~ Tab:\\t FormFeed:\\f Newline:\\n CarriageReturn:\\r Backspace:\\b Dollar: \\$xx Other:\\u0061 ~\"");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 1);
        assertToken(++index,
            tokenizer.text(Token.DOUBLE_QUOTE),
            Token.TEXT_LITERAL,
            "~ Tab:\t FormFeed:\f Newline:\n CarriageReturn:\r Backspace:\b Dollar: $xx Other:a ~",
            2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DOUBLE_QUOTE, "\"", 92);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 93);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_strings_escaped_BadUnicode() {
        EelSyntaxException actual;

        actual = Assert.assertThrows(
            "Not hex chars",
            EelSyntaxException.class,
            () -> buildTokenizer("\\uxxxx").text(Token.END_OF_PROGRAM));
        Assert.assertEquals("Unexpected message", "Error at position 3: Unexpected char 'x' (0x78)", actual.getMessage());

        actual = Assert.assertThrows(
            "Missing Digits",
            EelSyntaxException.class,
            () -> buildTokenizer("\\u41 hello").text(Token.END_OF_PROGRAM));
        Assert.assertEquals("Unexpected message", "Error at position 5: Unexpected char ' ' (0x20)", actual.getMessage());

        actual = Assert.assertThrows(
            "Truncated Input",
            EelSyntaxException.class,
            () -> buildTokenizer("\\u").text(Token.END_OF_PROGRAM));
        Assert.assertEquals("Unexpected message", "Error at position 3: Unexpected char (0x00)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_strings_ControlCharacters() {
        EelSyntaxException actual;

        actual = Assert.assertThrows(
            "Embedded Control characters",
            EelSyntaxException.class,
            () -> buildTokenizer(" \u0004    ").text(Token.END_OF_PROGRAM));
        Assert.assertEquals("Error at position 2: Unexpected char (0x04)", actual.getMessage());

        actual = Assert.assertThrows(
            "Unicode Specials Block (Interlinear Annotation Anchor)",
            EelSyntaxException.class,
            () -> buildTokenizer(" \uFFF9    ").text(Token.END_OF_PROGRAM));
        Assert.assertEquals("Error at position 2: Unexpected char (0xfff9)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)}
     */
    @Test
    public void test_embeddedNullTerminator() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(" \u0000 unreadable ");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 2);
    }



    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_decimals() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(".1 200 3.00 4.44 5e2 6E-3 7.3e10 8.");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, ".1", new BigDecimal(".1"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "200", new BigDecimal("200"), true, 4);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "3.00", new BigDecimal("3.00"), false, 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "4.44", new BigDecimal("4.44"), false, 13);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "5e2", new BigDecimal("500"), false, 18);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "6E-3", new BigDecimal("0.006"), false, 22);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "7.3e10", new BigDecimal("73000000000"), false, 27);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "8.", new BigDecimal("8"), false, 34);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 36);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_decimals_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(".1_00  2_000  0_3.00  4.4_4  5_0e2_0  6_0E-3_0  7_00.3e1_000");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, ".1_00", new BigDecimal(".1"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "2_000", new BigDecimal("2000"), true, 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0_3.00", new BigDecimal("3.00"), false, 15);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "4.4_4", new BigDecimal("4.44"), false, 23);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "5_0e2_0", new BigDecimal("5.0E+21"), false, 30);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "6_0E-3_0", new BigDecimal("6.0E-29"), false, 39);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "7_00.3e1_000", new BigDecimal("7.003E+1002"), false, 49);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 61);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_binary() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0b0101 0B1010");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0b0101", new BigDecimal("5"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0B1010", new BigDecimal("10"), false, 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 14);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_binary_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0b01_01 0B10_10");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0b01_01", new BigDecimal("5"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0B10_10", new BigDecimal("10"), false, 9);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 16);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_octal() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0c773 0C532");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0c773", new BigDecimal("507"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0C532", new BigDecimal("346"), false, 7);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 12);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_octal_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0c77_3 0C5_32");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0c77_3", new BigDecimal("507"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0C5_32", new BigDecimal("346"), false, 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 14);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_hex() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0xfe 0Xa5");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0xfe", new BigDecimal("254"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0Xa5", new BigDecimal("165"), false, 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 10);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_hex_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0xff_fe 0Xa5_5a");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0xff_fe", new BigDecimal("65534"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "0Xa5_5a", new BigDecimal("42330"), false, 9);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 16);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_numbers_withUnderscores_invalid_double() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1__2").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message",
            "Error at position 2: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_numbers_withUnderscores_invalid_lastChar() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer(".1_").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message",
            "Error at position 3: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforePoint() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1_.").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message",
            "Error at position 2: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_numbers_withUnderscores_invalid_afterPoint() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1._2").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message",
            "Error at position 3: Unexpected char '_' (0x5f)",
            actual.getMessage());

    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforeExponent() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1_e3").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 2: Unexpected char '_' (0x5f)", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_missingBinaryDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0b").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_missingOctalDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0C").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_missingHexDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0x").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_number_fractionalExponent() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("1.2e-3.4");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "1.2e-3", new BigDecimal("0.0012"), false, 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, ".4", new BigDecimal("0.4"), false, 7);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 9);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_number_missingExponent() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1.2e ").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 5: Expected exponent", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_dollarValue_expression() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("$[123]");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOOK_BACK, "$[", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "123", new BigDecimal("123"), true, 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.RIGHT_BRACKET, "]", 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 7);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_dollarValue_interpolate() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("$123");

        assertToken(++index, tokenizer.text(Token.END_OF_PROGRAM), Token.TEXT_LITERAL, "$123", 1);
        assertToken(++index, tokenizer.text(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 5);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_constants() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("true false");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.TRUE, "true", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.FALSE, "false", 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 11);
    }
    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_conversionOps() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("text number logic date");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.TEXT, "text", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMBER, "number", 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOGIC, "logic", 13);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DATE, "date", 19);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 23);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_bitwise_op() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("~ & | ^");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.TILDE, "~", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.BITWISE_AND, "&", 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.BITWISE_OR, "|", 5);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.CARET, "^", 7);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 8);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_start_interpolation() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("${ $(");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.VALUE_INTERPOLATION, "${", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.EXPRESSION_INTERPOLATION, "$(", 4);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 6);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_math_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(" + - * / // -/ % ** << >>");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.PLUS, "+", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.MINUS, "-", 4);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.MULTIPLY, "*", 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DIVIDE, "/", 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DIVIDE_FLOOR, "//", 10);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.DIVIDE_TRUNCATE, "-/", 13);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.MODULUS, "%", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.EXPONENTIATION, "**", 18);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LEFT_SHIFT, "<<", 21);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.RIGHT_SHIFT, ">>", 24);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 26);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_relational_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("= <> != >= > < <= isBefore isAfter");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.EQUAL, "=", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NOT_EQUAL, "<>", 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NOT_EQUAL, "!=", 6);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.GREATER_THAN_EQUAL, ">=", 9);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.GREATER_THAN, ">", 12);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LESS_THAN, "<", 14);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LESS_THAN_EQUAL, "<=", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IS_BEFORE, "isBefore", 19);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IS_AFTER, "isAfter", 28);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 35);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_logical_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("not and or xor");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOGICAL_NOT, "not", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOGICAL_AND, "and", 5);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOGICAL_OR, "or", 9);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LOGICAL_XOR, "xor", 12);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 15);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_text_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("~>");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.CONCATENATE, "~>", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 3);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_in_operator() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("in");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IN, "in", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 3);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_variableExpansion() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("# ^ ^^ , ,, ~ ~~ :-");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.HASH, "#", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.CARET, "^", 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.ALL_UPPER, "^^", 5);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.COMMA, ",", 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.ALL_LOWER, ",,", 10);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.TILDE, "~", 13);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.ALL_TOGGLE, "~~", 15);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.BLANK_DEFAULT, ":-", 18);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 20);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_miscSymbols() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("() {} [] , ? ; : #");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LEFT_PARENTHESES, "(", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.RIGHT_PARENTHESES, ")", 2);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LEFT_BRACE, "{", 4);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.RIGHT_BRACE, "}", 5);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.LEFT_BRACKET, "[", 7);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.RIGHT_BRACKET, "]", 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.COMMA, ",", 10);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.QUESTION_MARK, "?", 12);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SEMICOLON, ";", 14);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.COLON, ":", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.HASH, "#", 18);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 19);
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_unexpectedPrintable() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("@").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 1: Unexpected char '@' (0x40)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_unexpectedUnprintable() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("\u0002").interpolate(Token.END_OF_PROGRAM));

        Assert.assertEquals("Unexpected message", "Error at position 1: Unexpected char (0x02)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_identifier() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("Key Key2 Key_3 ENV_VAR _PWD system.property with.array[123].element");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "Key", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "Key2", 5);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "Key_3", 10);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "ENV_VAR", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "_PWD", 24);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "system.property", 29);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "with.array[123].element", 45);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 68);
    }


    /**
     * Unit test {@link Tokenizer#interpolate(Token)} 
     */
    @Test
    public void test_mixed() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("$(Hello-'World!'+\t9e-2");

        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.EXPRESSION_INTERPOLATION, "$(", 1);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.IDENTIFIER, "Hello", 3);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.MINUS, "-", 8);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 9);
        assertToken(++index, tokenizer.text(Token.SINGLE_QUOTE), Token.TEXT_LITERAL, "World!", 10);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.SINGLE_QUOTE, "'", 16);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.PLUS, "+", 17);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.NUMERIC, "9e-2", new BigDecimal("0.09"), false, 19);
        assertToken(++index, tokenizer.interpolate(Token.END_OF_PROGRAM), Token.END_OF_PROGRAM, "", 23);
    }


    private void assertToken(int index,
            @Nonnull Terminal actual,
            @Nonnull Token expectedToken,
            @Nonnull String expectedLexeme,
            int expectedPosition) {
        assertToken(index, actual, expectedToken, expectedLexeme, null, false, expectedPosition);
    }

    private void assertToken(int index,
                             @Nonnull Terminal actual,
                             @Nonnull Token expectedToken,
                             @Nonnull String expectedLiteral,
                             @Nullable BigDecimal expectedValue,
                             boolean expectedDecimal,
                             int expectedPosition) {
        Assert.assertEquals("Token " + index + ". Unexpected token", expectedToken, actual.token());
        Assert.assertEquals("Token " + index + ". Unexpected lexeme", expectedLiteral, actual.lexeme());

        if (expectedValue != null) {
            Assert.assertTrue(
                "Token " + index + ". Unexpected value: " + actual.value() + ", expected " + expectedValue,
                expectedValue.compareTo(actual.value()) == 0);

            Assert.assertEquals(
                "Token " + index + ". Unexpected decimal flag: ",
                expectedDecimal,
                actual.isDecimal());

            if (expectedDecimal) {
                Assert.assertEquals("Token " + index + " unexpected decimal", expectedValue.intValue(), actual.decimal());
            }
        }

        Assert.assertEquals("Token " + index + ". unexpected position", expectedPosition, actual.position());
    }


    @Nonnull
    private Tokenizer buildTokenizer(@Nonnull String expression) {
        Source source = Source.build(expression, expression.length());

        return new Tokenizer(source);
    }
}