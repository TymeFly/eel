package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.utils.CharSetBuilder;
import com.github.tymefly.eel.validate.Preconditions;

import static java.util.Map.entry;

/**
 * Convert an input stream into a sequence if terminal fields
 */
class Tokenizer {
    /**
     * Description of a single terminal read by the tokenizer
     * @param token         Token this terminal describes
     * @param lexeme        The literal text of this token
     * @param value         If {@code token} is a {@link Token#NUMBER} then this holds value of the number
     * @param isFractional  If {@code token} is a {@link Token#NUMBER} then this is {@literal true} if there is a
     *                          fractional part.
     * @param position      start position in the input stream of the token.
     */
    record Terminal(@Nonnull Token token,
                    @Nonnull String lexeme,
                    @Nullable BigDecimal value,
                    boolean isFractional,
                    int position) {
        @Override
        @Nonnull
        public BigDecimal value() {
            Preconditions.checkState((value != null), "Value is undefined for a " + token);

            return value;
        }
    }

    /**
     * Scanning mode used to read the next token. This determines where the breaks between tokens are
     */
    enum Mode {
        /**
         * Terminate at the start of an interpolation sequence or the end of the source
         */
        INTERPOLATE(true, Source.END),

        /**
         * Terminate at the start of an interpolation sequence or the end of the source or the end of the current block
         */
        INTERPOLATE_BLOCK(true, '}'),

        /**
         * Terminate at the end of the next number, identifier, punctuation or symbol
         */
        EXPRESSION(false, Source.END);

        private final boolean interpolate;
        private final char follow;

        Mode(boolean interpolate, char follow) {
            this.interpolate = interpolate;
            this.follow = follow;
        }
    }

    private static final Set<Character> WHITESPACE = Set.of(' ', '\t');
    private static final Map<Character, Character> ESCAPED = Map.ofEntries(
        entry('\\', '\\'),
        entry('\'', '\''),
        entry('"', '"'));
    private static final Map<String, Token> RESERVED = Map.ofEntries(
        entry("true", Token.TRUE),                      // Constants
        entry("false", Token.FALSE),
        entry("and", Token.LOGICAL_AND),                // Logic Ops
        entry("or", Token.LOGICAL_OR),
        entry("not", Token.LOGICAL_NOT),
        entry("isBefore", Token.IS_BEFORE),                // Date Ops
        entry("isAfter", Token.IS_AFTER));
    private static final Map<String, Token> SYMBOLS = Map.ofEntries(
        entry("~>", Token.CONCATENATE),                 // Text Ops
        entry("+", Token.PLUS),                         // Maths Ops
        entry("-", Token.MINUS),
        entry("*", Token.MULTIPLY),
        entry("/", Token.DIVIDE),
        entry("//", Token.DIVIDE_FLOOR),
        entry("-/", Token.DIVIDE_TRUNCATE),
        entry("%", Token.MODULUS),
        entry("**", Token.EXPONENTIATION),
        entry("=", Token.EQUAL),                        // Relational Ops
        entry("<>", Token.NOT_EQUAL),
        entry("!=", Token.NOT_EQUAL),
        entry(">", Token.GREATER_THAN),
        entry(">=", Token.GREATER_THAN_EQUAL),
        entry("<", Token.LESS_THAN),
        entry("<=", Token.LESS_THAN_EQUAL),
        entry("&", Token.BITWISE_AND),                   // Bitwise Ops
        entry("|", Token.BITWISE_OR),
        entry("<<", Token.LEFT_SHIFT),
        entry(">>", Token.RIGHT_SHIFT),
        entry("(", Token.LEFT_PARENTHESES),             // Other Symbols
        entry(")", Token.RIGHT_PARENTHESES),
        entry("{", Token.LEFT_BRACE),
        entry("}", Token.RIGHT_BRACE),
        entry(",", Token.COMMA),
        entry("?", Token.QUESTION_MARK),
        entry(":", Token.COLON),
        entry("#", Token.HASH),
        entry("~", Token.TILDE),
        entry("~~", Token.ALL_TOGGLE),
        entry("^", Token.CARET),
        entry("^^", Token.ALL_UPPER),
        entry(",,", Token.ALL_LOWER));
    private static final Set<Character> LETTER = new CharSetBuilder()
        .range('a', 'z')
        .range('A', 'Z')
        .immutable();
    private static final Set<Character> DIGITS = new CharSetBuilder()
        .range('0', '9')
        .immutable();
    private static final Set<Character> HEX = new CharSetBuilder()
        .withAll(DIGITS)
        .range('A', 'F')
        .range('a', 'f')
        .immutable();
    private static final Set<Character> IDENTIFIER_START = new CharSetBuilder()
        .withAll(LETTER)
        .with('_')
        .immutable();
    private static final Set<Character> IDENTIFIER_INTERNAL = new CharSetBuilder()
        .withAll(IDENTIFIER_START)
        .withAll(DIGITS)
        .with('.')
        .with('[').with(']')
        .immutable();


    private static final int HEX_BASE = 16;


    private final Source source;
    private final StringBuilder lexeme;
    private BigDecimal value;
    private boolean fractional;


    Tokenizer(@Nonnull Source source) {
        this.source = source;
        this.lexeme = new StringBuilder();
    }


    /**
     * Read the next terminal symbol from the source
     * @param mode  Current read mode
     * @return      The next terminal symbol
     */
    @Nonnull
    Terminal next(@Nonnull Mode mode) {
        int position = source.position();
        char current = source.current();
        Token token = Token.UNDEFINED;

        lexeme.setLength(0);
        value = null;
        fractional = false;

        do {
            if (current == Source.END) {
                token = Token.END_OF_PROGRAM;
            } else {
                if (mode.interpolate && !endInterpolation(mode, current)) {
                    token = interpolateString(mode);
                } else if (current == '$') {
                    token = parseDollar(mode);
                } else if (WHITESPACE.contains(current)) {
                    current = source.read();
                    position = source.position();
                } else if ((current == '"') || (current == '\'')) {
                    token = parseString(current);
                } else if ((current == '.') || DIGITS.contains(current)) {
                    token = parseNumber(current);
                } else if (IDENTIFIER_START.contains(current)) {
                    token = parseIdentifier();
                } else {
                    token = parseSymbol(current);
                }
            }
        } while (token == Token.UNDEFINED);

        return new Terminal(token, lexeme.toString(), value, fractional, position);
    }

    @Nonnull
    private Token parseDollar(@Nonnull Mode mode) {
        char next = source.next();
        Token token;

        if (next == '{') {
            token = Token.VALUE_INTERPOLATION;
            appendLexme();
            appendLexme();
        } else if (next == '(') {
            token = Token.EXPRESSION_INTERPOLATION;
            appendLexme();
            appendLexme();
        } else if (IDENTIFIER_START.contains(next)) {
            token = Token.FUNCTION_INTERPOLATION;
            appendLexme();
        } else {
            token = interpolateString(mode);
        }

        return token;
    }

    @Nonnull
    private Token interpolateString(@Nonnull Mode mode) {
        boolean done;

        do {
            char current = appendLexme();

            done = (current == Source.END) || endInterpolation(mode, current);
        } while (!done);

        return Token.STRING;
    }

    private boolean endInterpolation(@Nonnull Mode mode, char current) {
        return (current == mode.follow) || (current == '$');
    }


    @Nonnull
    private Token parseIdentifier() {
        char current;

        do {
            current = appendLexme();
        } while (IDENTIFIER_INTERNAL.contains(current));

        return RESERVED.getOrDefault(lexeme.toString(), Token.IDENTIFIER);
    }


    @Nonnull
    private Token parseString(char terminator) {
        int position = source.position();
        char current = source.read();

        while (current != terminator) {
            Character resolved;

            if (current == Source.END) {
                throw new EelSyntaxException(position, "Unterminated string. Expected %c", terminator);
            }

            if (current == '\\') {
                resolved = ESCAPED.get(source.read());
            } else {
                resolved = current;
            }

            if (!isValidCharacter(resolved)) {
                unexpectedCharacter(resolved == null ? source.current() : resolved);
            }

            lexeme.append(resolved);
            current = source.read();
        }

        source.read();

        return Token.STRING;
    }


    // see https://stackoverflow.com/questions/220547/printable-char-in-java
    private boolean isValidCharacter(@Nullable Character test) {
        boolean invalid = (test == null);
        invalid = invalid || Character.isISOControl(test);

        if (!invalid) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(test);

            invalid = (block == null);
            invalid = invalid || (block == Character.UnicodeBlock.SPECIALS);
        }

        return !invalid;
    }


    /**
     * @param current       a digit
     */
    @Nonnull
    private Token parseNumber(char current) {
        char next = source.next();

        if ((current == '0') && ((next == 'x') || (next == 'X'))) {
            parseHex();
        } else {
            parseDecimal();
        }

        return Token.NUMBER;
    }

    private void parseHex() {
        appendLexme();                                      // skip 0x
        appendLexme();

        int digits = parseDigits(HEX);

        if (digits == 0) {
            throw new EelSyntaxException(source.position(), "Expected hex digit");
        }

        value = new BigDecimal(new BigInteger(lexeme.substring(2), HEX_BASE));
    }

    private void parseDecimal() {
        char current = parseInt();

        if (current == '.') {
            fractional = true;
            current = appendLexme();

            if (current == '_') {
                unexpectedCharacter();
            }

            current = parseInt();
        }

        if ((current == 'e') || (current == 'E')) {
            parseExponent();
        }

        value = new BigDecimal(lexeme.toString());
    }


    private char parseInt() {
        parseDigits(DIGITS);

        return source.current();
    }


    private int parseDigits(@Nonnull Set<Character> digits) {
        char current = source.current();
        int count = 0;

        while (digits.contains(current)) {
            current = appendLexme();
            count++;

            if (current == '_') {
                if (digits.contains(source.next())) {
                    current = source.read();
                } else {
                    unexpectedCharacter();
                }
            }
        }

        return count;
    }

    private void parseExponent() {
        char current = appendLexme();

        if (current == '-') {
            current = appendLexme();
        }

        if (!DIGITS.contains(current)) {
            throw new EelSyntaxException(source.position(), "Expected exponent");
        }

        parseInt();
    }


    @Nonnull
    private Token parseSymbol(char current) {
        lexeme.append(current)
            .append(source.next());
        Token result = SYMBOLS.get(lexeme.toString());

        if (result != null) {
            source.read();
        } else {
            lexeme.setLength(1);
            result = SYMBOLS.get(lexeme.toString());
        }

        if (result == null) {
            unexpectedCharacter(current);
        }

        source.read();

        return result;
    }

    private boolean isAsciiPrintable(char test) {
        return ((test >= ' ') && (test < '~'));
    }


    private char appendLexme() {
        lexeme.append(source.current());

        return source.read();
    }


    private void unexpectedCharacter() {
        unexpectedCharacter(source.current());
    }

    private void unexpectedCharacter(char current) {
        throw new EelSyntaxException(source.position(),
            "Unexpected char %s(0x%02x)",
            (isAsciiPrintable(current) ? "'" + current + "' " : ""),
            (int) current);
    }
}
