package com.github.tymefly.eel;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.Tokenizer.Mode;
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
        Terminal actual = buildTokenizer("").next(Mode.EXPRESSION);

        assertToken(1, actual, Token.END_OF_PROGRAM, "", 0);
    }


    @Test
    public void test_whitespace() {
        Terminal actual = buildTokenizer(" \t \t  ").next(Mode.EXPRESSION);

        assertToken(1, actual, Token.END_OF_PROGRAM, "", 7);
    }

    @Test
    public void test_interpolate() {
        assertToken(1, buildTokenizer("").next(Mode.INTERPOLATE), Token.END_OF_PROGRAM, "", 0);
        assertToken(1, buildTokenizer("some text").next(Mode.INTERPOLATE), Token.STRING, "some text", 1);
        assertToken(1, buildTokenizer("  some text  ").next(Mode.INTERPOLATE), Token.STRING, "  some text  ", 1);
        assertToken(1, buildTokenizer("xxx $(").next(Mode.INTERPOLATE), Token.STRING, "xxx ", 1);
        assertToken(1, buildTokenizer("___${").next(Mode.INTERPOLATE), Token.STRING, "___", 1);
        assertToken(1, buildTokenizer("$1.23").next(Mode.INTERPOLATE), Token.STRING, "$1.23", 1);
        assertToken(1, buildTokenizer("$_").next(Mode.INTERPOLATE), Token.FUNCTION_INTERPOLATION, "$", 1);
        assertToken(1, buildTokenizer("${abc}").next(Mode.INTERPOLATE), Token.VALUE_INTERPOLATION, "${", 1);
        assertToken(1, buildTokenizer("$(abc)").next(Mode.INTERPOLATE), Token.EXPRESSION_INTERPOLATION, "$(", 1);
        assertToken(1, buildTokenizer("$func()").next(Mode.INTERPOLATE), Token.FUNCTION_INTERPOLATION, "$", 1);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_strings() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("'Hello World' \"foo\" ");

        assertToken(++index, tokenizer.next(Tokenizer.Mode.EXPRESSION), Token.STRING, "Hello World", 1);
        assertToken(++index, tokenizer.next(Tokenizer.Mode.EXPRESSION), Token.STRING, "foo", 15);
        assertToken(++index, tokenizer.next(Tokenizer.Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 21);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_strings_SingleQuote_escaped() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("'~ BackSlash:\\\\ Single Quote:\\' Double Quote:\\\" ~'");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.STRING, "~ BackSlash:\\ Single Quote:' Double Quote:\" ~", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 51);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_strings_DoubleQuote_escaped() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("\"~ BackSlash:\\\\ Single Quote:\\' Double Quote:\\\" ~\"");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.STRING, "~ BackSlash:\\ Single Quote:' Double Quote:\" ~", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 51);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_strings_ControlCharacters() {
        EelSyntaxException actual;

        actual = Assert.assertThrows(
            "Embedded new lines",
            EelSyntaxException.class,
            () -> buildTokenizer(" '\\r\\n   ' ").next(Mode.EXPRESSION));
        Assert.assertEquals("Unexpected message", "Error at position 4: Unexpected char 'r' (0x72)", actual.getMessage());

        actual = Assert.assertThrows(
            "Embedded Control characters",
            EelSyntaxException.class,
            () -> buildTokenizer(" '\u0004   ' ").next(Mode.EXPRESSION));
        Assert.assertEquals("Error at position 3: Unexpected char (0x04)", actual.getMessage());

        actual = Assert.assertThrows(
            "Unicode Specials Block (Interlinear Annotation Anchor)",
            EelSyntaxException.class,
            () -> buildTokenizer(" '\uFFF9   ' ").next(Mode.EXPRESSION));
        Assert.assertEquals("Error at position 3: Unexpected char (0xfff9)", actual.getMessage());

        actual = Assert.assertThrows(
            "Embedded CHAR_UNDEFINED (EOF marker)",
            EelSyntaxException.class,
            () -> buildTokenizer(" '\uffff   ' ").next(Mode.EXPRESSION));
        Assert.assertEquals("Error at position 2: Unterminated string. Expected '", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_string_unterminated() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("'unterminated").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message",
            "Error at position 1: Unterminated string. Expected '",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_decimals() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(".1 200 3.00 4.44 5e2 6E-3 7.3e10 8.");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal(".1"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("200"), 4);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("3.00"), 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("4.44"), 13);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("500"), 18);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("0.006"), 22);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("73000000000"), 27);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("8"), 34);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 36);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_decimals_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(".1_00  2_000  0_3.00  4.4_4  5_0e2_0  6_0E-3_0  7_00.3e1_000");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal(".1"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("2000"), 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("3.00"), 15);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("4.44"), 23);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("5.0E+21"), 30);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("6.0E-29"), 39);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("7.003E+1002"), 49);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 61);
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_binary() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0b0101 0B1010");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("5"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("10"), 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 14);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_binary_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0b01_01 0B10_10");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("5"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("10"), 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 16);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_octal() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0c773 0C532");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("507"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("346"), 7);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 12);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_octal_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0c77_3 0C5_32");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("507"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("346"), 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 14);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_hex() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0xfe 0Xa5");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("254"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("165"), 6);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 10);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_hex_withUnderscores() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("0xff_fe 0Xa5_5a");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("65534"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("42330"), 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 16);
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_double() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1__2").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message",
            "Error at position 2: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_lastChar() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer(".1_").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message",
            "Error at position 3: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforePoint() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1_.").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message",
            "Error at position 2: Unexpected char '_' (0x5f)",
            actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_afterPoint() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1._2").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message",
            "Error at position 3: Unexpected char '_' (0x5f)",
            actual.getMessage());

    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_numbers_withUnderscores_invalid_beforeExponent() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1_e3").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 2: Unexpected char '_' (0x5f)", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_missingBinaryDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0b").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_missingOctalDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0C").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_missingHexDigits() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("0x").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 3: Expected digits", actual.getMessage());
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_number_fractionalExponent() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("1.2e-3.4");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("0.0012"), 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("0.4"), 7);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 9);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_number_missingExponent() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("1.2e ").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 5: Expected exponent", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_constants() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("true false");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.TRUE, "true", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.FALSE, "false", 6);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 11);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_bitwise_op() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("~ & | ^");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.TILDE, "~", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.BITWISE_AND, "&", 3);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.BITWISE_OR, "|", 5);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.CARET, "^", 7);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 8);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_start_interpolation() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("${ $(");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.VALUE_INTERPOLATION, "${", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.EXPRESSION_INTERPOLATION, "$(", 4);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 6);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_math_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer(" + - * / // -/ % ** << >>");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.PLUS, "+", 2);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.MINUS, "-", 4);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.MULTIPLY, "*", 6);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.DIVIDE, "/", 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.DIVIDE_FLOOR, "//", 10);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.DIVIDE_TRUNCATE, "-/", 13);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.MODULUS, "%", 16);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.EXPONENTIATION, "**", 18);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LEFT_SHIFT, "<<", 21);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.RIGHT_SHIFT, ">>", 24);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 26);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_relational_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("= <> != >= > < <= isBefore isAfter");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.EQUAL, "=", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NOT_EQUAL, "<>", 3);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NOT_EQUAL, "!=", 6);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.GREATER_THAN_EQUAL, ">=", 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.GREATER_THAN, ">", 12);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LESS_THAN, "<", 14);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LESS_THAN_EQUAL, "<=", 16);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IS_BEFORE, "isBefore", 19);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IS_AFTER, "isAfter", 28);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 35);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_logical_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("not and or");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LOGICAL_NOT, "not", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LOGICAL_AND, "and", 5);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LOGICAL_OR, "or", 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 11);
    }

        /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_text_operators() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("~>");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.CONCATENATE, "~>", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 3);
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_variableExpansion() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("# ^ ^^ , ,, ~ ~~");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.HASH, "#", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.CARET, "^", 3);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.ALL_UPPER, "^^", 5);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.COMMA, ",", 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.ALL_LOWER, ",,", 10);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.TILDE, "~", 13);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.ALL_TOGGLE, "~~", 15);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 17);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_miscSymbols() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("() {} , ? : #");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LEFT_PARENTHESES, "(", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.RIGHT_PARENTHESES, ")", 2);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.LEFT_BRACE, "{", 4);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.RIGHT_BRACE, "}", 5);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.COMMA, ",", 7);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.QUESTION_MARK, "?", 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.COLON, ":", 11);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.HASH, "#", 13);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 14);
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_unexpectedPrintable() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("@").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 1: Unexpected char '@' (0x40)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_unexpectedUnprintable() {
        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> buildTokenizer("\u0002").next(Mode.EXPRESSION));

        Assert.assertEquals("Unexpected message", "Error at position 1: Unexpected char (0x02)", actual.getMessage());
    }

    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_identifier() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("Key Key2 Key_3 ENV_VAR _PWD system.property with.array[123].element");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "Key", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "Key2", 5);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "Key_3", 10);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "ENV_VAR", 16);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "_PWD", 24);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "system.property", 29);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "with.array[123].element", 45);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 68);
    }


    /**
     * Unit test {@link Tokenizer#next(Mode)}
     */
    @Test
    public void test_mixed() {
        int index = 0;
        Tokenizer tokenizer = buildTokenizer("$(Hello-'World!'+\t9e-2");

        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.EXPRESSION_INTERPOLATION, "$(", 1);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.IDENTIFIER, "Hello", 3);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.MINUS, "-", 8);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.STRING, "World!", 9);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.PLUS, "+", 17);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.NUMBER, "", new BigDecimal("0.09"), 19);
        assertToken(++index, tokenizer.next(Mode.EXPRESSION), Token.END_OF_PROGRAM, "", 23);
    }


    private void assertToken(int index,
            @Nonnull Terminal actual,
            @Nonnull Token expectedToken,
            @Nonnull String expectedLexeme,
            int expectedPosition) {
        assertToken(index, actual, expectedToken, expectedLexeme, null, expectedPosition);
    }


    private void assertToken(int index,
                             @Nonnull Terminal actual,
                             @Nonnull Token expectedToken,
                             @Nonnull String expectedLiteral,
                             @Nullable BigDecimal expectedValue,
                             int expectedPosition) {
        Assert.assertEquals("Token " + index + ". Unexpected token", expectedToken, actual.token());
        Assert.assertEquals("Token " + index + ". Unexpected lexeme", expectedLiteral, actual.lexeme());

        if (expectedValue != null) {
            Assert.assertTrue(
                "Token " + index + ". Unexpected value: " + actual.value() + ", expected " + expectedValue,
                expectedValue.compareTo(actual.value()) == 0);
        }

        Assert.assertEquals("Token " + index + ". unexpected position", expectedPosition, actual.position());
    }


    @Nonnull
    private Tokenizer buildTokenizer(@Nonnull String expression) {
        Source source = Source.build(expression, expression.length());

        return new Tokenizer(source);
    }
}