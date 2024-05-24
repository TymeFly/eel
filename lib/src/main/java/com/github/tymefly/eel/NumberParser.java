package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.utils.CharSetBuilder;
import com.github.tymefly.eel.utils.CharUtils;

/**
 * Class to parse a number in any of EEL's supported formats. These are:
 * <ul>
 *  <li>Decimal integers (e.g. `1234`)</li>
 *  <li>Binary integers (e.g. `0b1010`)</li>
 *  <li>Octal integers (e.g. `0c1234567`)</li>
 *  <li>Hexadecimal integers (e.g. `0x89ab`)</li>
 *  <li>Decimals with fractional parts (e.g. `123.456789`)</li>
 *  <li>Scientific format (e.g. `2.99792e8`)</li>
 * </ul>
 * All numbers may be proceeded by either a positive or negative sign
 */
class NumberParser {
    private static class StringInput implements Input {
        private final String input;
        private int index = 0;

        private StringInput(@Nonnull String input) {
            this.input = input;
        }

        @Override
        public char current() {
            return (index < input.length() ? input.charAt(index) : END);
        }

        @Override
        public char next() {
            int next = index + 1;

            return (next < input.length() ? input.charAt(next) : END);
        }

        @Override
        public char read() {
            index++;
            return current();
        }

        private boolean atEnd() {
            return (index >= input.length());
        }
    }


    private enum Base {
        BINARY(2, BINARY_DIGITS),
        OCTAL(8, OCTAL_DIGITS),
        HEX(16, HEX_DIGITS);

        private final Set<Character> digits;
        private final int base;

        Base(int base, @Nonnull Set<Character> digits) {
            this.base = base;
            this.digits = digits;
        }
    }


    private static final Set<Character> BINARY_DIGITS = new CharSetBuilder()
        .with('0')
        .with('1')
        .immutable();
    private static final Set<Character> OCTAL_DIGITS = new CharSetBuilder()
        .range('0', '7')
        .immutable();
    private static final Set<Character> DECIMAL_DIGITS = new CharSetBuilder()
        .range('0', '9')
        .immutable();
    private static final Set<Character> HEX_DIGITS = new CharSetBuilder()
        .range('0', '9')
        .range('A', 'F')
        .range('a', 'f')
        .immutable();


    private final Input input;
    private final StringBuilder text;


    private NumberParser(@Nonnull Input input) {
        this.input = input;
        this.text = new StringBuilder();
    }


    /**
     * Parse a numeric value from the input. If is not required that all the data from the inpurt is read.
     * @param input     Numeric input to parse in any of the supported formats
     * @return          The numeric value of the string
     * @throws NumberFormatException if the {@code text} is not a valid number
     */
    @Nonnull
    static BigDecimal parse(@Nonnull Input input) throws NumberFormatException {
        return new NumberParser(input)
            .parseNumber();
    }

    /**
     * Parse all the text from a string.
     * @param text      numeric string to parse in any of the supported formats
     * @return          The numeric value of the string
     * @throws NumberFormatException if the {@code text} is not a valid number
     */
    @Nonnull
    static BigDecimal parse(@Nonnull String text) throws NumberFormatException {
        StringInput input = new StringInput(text);
        BigDecimal value = parse(input);

        if (!input.atEnd()) {
            throw new NumberFormatException("For input string: \"" + text + "\"");
        }

        return value;
    }


    @Nonnull
    private BigDecimal parseNumber() {
        BigDecimal value;
        char current = input.current();

        if (current == '+') {
            current = input.read();
        } else if (current == '-') {
            current = appendInput();
        } else {
            // no special action required
        }

        char next = input.next();

        if (current == Input.END) {
            throw new NumberFormatException("For input \"\"");
        } else if ((current == '0') && ((next == 'x') || (next == 'X'))) {
            value = parseBase(Base.HEX);
        } else if ((current == '0') && ((next == 'c') || (next == 'C'))) {
            value = parseBase(Base.OCTAL);
        } else if ((current == '0') && ((next == 'b') || (next == 'B'))) {
            value = parseBase(Base.BINARY);
        } else {
            value = parseDecimal();
        }

        return value;
    }

    @Nonnull
    private BigDecimal parseBase(@Nonnull Base base) {
        input.read();                               // skip leading 0 and base quantifier character
        input.read();

        int digits = parseDigits(base.digits);

        if (digits == 0) {
            throw new NumberFormatException("Expected digits");
        }

        return new BigDecimal(new BigInteger(text.toString(), base.base));
    }

    @Nonnull
    private BigDecimal parseDecimal() {
        char current = parseInt();

        if (current == '.') {
            current = appendInput();

            if (current == '_') {
                unexpectedCharacter();
            }

            current = parseInt();
        }

        if ((current == 'e') || (current == 'E')) {
            parseExponent();
        }

        return new BigDecimal(text.toString());
    }

    private char parseInt() {
        parseDigits(DECIMAL_DIGITS);

        return input.current();
    }


    private int parseDigits(@Nonnull Set<Character> digits) {
        char current = input.current();
        int count = 0;

        while (digits.contains(current)) {
            current = appendInput();
            count++;

            if (current == '_') {
                if (digits.contains(input.next())) {
                    current = input.read();
                } else {
                    unexpectedCharacter();
                }
            }
        }

        return count;
    }

    private void parseExponent() {
        char current = appendInput();

        if (current == '-') {
            current = appendInput();
        }

        if (!DECIMAL_DIGITS.contains(current)) {
            throw new NumberFormatException("Expected exponent");
        }

        parseInt();
    }

    private char appendInput() {
        text.append(input.current());

        return input.read();
    }

    private void unexpectedCharacter() {
        char current = input.current();

        throw new NumberFormatException(
            "Unexpected char " + (CharUtils.isAsciiPrintable(current) ? "'" + current + "' " : "") +
            "(0x" + Integer.toHexString(current) + ")"
        );
    }
}
