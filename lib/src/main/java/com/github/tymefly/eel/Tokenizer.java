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
     * @param token     Token this terminal describes
     * @param lexeme    The literal text of this token
     * @param value     If {@code token} is a {@link Token#NUMBER} then this holds value of the number
     * @param position  start position in the input stream of the token.
     */
    record Terminal(@Nonnull Token token, @Nonnull String lexeme, @Nullable BigDecimal value, int position) {
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
         * Interpolated strings terminate at the start of an expression, the start of a variable or
         * the end of the expression
         */
        INTERPOLATE(true, Source.END),

        /**
         * Interpolated blocks terminate at the start of an expression, the start of a variable or
         * the end of the current block or the end of the expression
         */
        INTERPOLATE_BLOCK(true, '}'),

        /**
         * Expressions terminate at the end of the next number, identifier, punctuation or symbol.
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
        entry("true", Token.TRUE),
        entry("false", Token.FALSE),
        entry("e", Token.E),
        entry("pi", Token.PI),
        entry("AND", Token.BITWISE_AND),
        entry("OR", Token.BITWISE_OR),
        entry("XOR", Token.BITWISE_XOR),
        entry("NOT", Token.BITWISE_NOT),
        entry("TEXT", Token.CONVERT_TO_TEXT),
        entry("NUMBER", Token.CONVERT_TO_NUMBER),
        entry("LOGIC", Token.CONVERT_TO_LOGIC),
        entry("DATE", Token.CONVERT_TO_DATE));
    private static final Map<String, Token> SYMBOLS = Map.ofEntries(
        entry("${", Token.VARIABLE_EXPANSION),          // Interpolation markers
        entry("$(", Token.EXPRESSION_EXPANSION),
        entry("~>", Token.CONCATENATE),                 // Text Ops
        entry("+", Token.PLUS),                         // Maths Ops
        entry("-", Token.MINUS),
        entry("*", Token.MULTIPLY),
        entry("/", Token.DIVIDE),
        entry("%", Token.MODULUS),
        entry("<<", Token.LEFT_SHIFT),
        entry(">>", Token.RIGHT_SHIFT),
        entry("^", Token.POWER),
        entry("**", Token.POWER),
        entry("=", Token.EQUAL),                        // Relational Ops
        entry("<>", Token.NOT_EQUAL),
        entry("!=", Token.NOT_EQUAL),
        entry(">", Token.GREATER_THAN),
        entry(">=", Token.GREATER_THAN_EQUAL),
        entry("<", Token.LESS_THAN),
        entry("<=", Token.LESS_THAN_EQUAL),
        entry("&", Token.LOGICAL_AND),                  // Logical Ops
        entry("|", Token.LOGICAL_OR),
        entry("&&", Token.SHORT_CIRCUIT_AND),
        entry("||", Token.SHORT_CIRCUIT_OR),
        entry("!", Token.LOGICAL_NOT),
        entry("(", Token.LEFT_PARENTHESES),             // other symbols
        entry(")", Token.RIGHT_PARENTHESES),
        entry("{", Token.LEFT_BRACE),
        entry("}", Token.RIGHT_BRACE),
        entry(",", Token.COMMA),
        entry("?", Token.QUESTION_MARK),
        entry(":", Token.COLON),
        entry("#", Token.HASH),
        entry("~", Token.TOGGLE),
        entry("~~", Token.ALL_TOGGLE),
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
    private static final Set<Character> WORD_START = new CharSetBuilder()
        .withAll(LETTER)
        .with('_')
        .immutable();
    private static final Set<Character> WORD_INTERNAL = new CharSetBuilder()
        .withAll(WORD_START)
        .withAll(DIGITS)
        .with('.')
        .immutable();


    private static final int HEX_BASE = 16;


    private final Source source;
    private final StringBuilder lexeme;
    private BigDecimal value;


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

        do {
            if (current == Source.END) {
                token = Token.END_OF_PROGRAM;
            } else if (mode.interpolate && !endInterpolation(mode, current)) {
                token = interpolateString(mode, current);
            } else if (WHITESPACE.contains(current)) {
                current = source.read();
                position = source.position();
            } else if ((current == '"') || (current == '\'')) {
                token = parseString(current);
            } else if ((current == '.') || DIGITS.contains(current)) {
                token = parseNumber(current);
            } else if (WORD_START.contains(current)) {
                token = parseWord(current);
            } else {
                token = parseSymbol(current);
            }
        } while (token == Token.UNDEFINED);

        return new Terminal(token, lexeme.toString(), value, position);
    }


    @Nonnull
    private Token interpolateString(@Nonnull Mode mode, char current) {
        boolean done;

        do {
            lexeme.append(current);
            current = source.read();

            done = (current == Source.END) || endInterpolation(mode, current);
        } while (!done);

        return Token.STRING;
    }

    private boolean endInterpolation(@Nonnull Mode mode, char current) {
        char next = source.next();
        boolean end;

        end = (current == mode.follow);
        end = end || ((current == '$') && (next == '{'));
        end = end || ((current == '$') && (next == '('));

        return end;
    }


    @Nonnull
    private Token parseWord(char current) {
        while(WORD_INTERNAL.contains(current)) {
            lexeme.append(current);
            current = source.read();
        }

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
                throw new EelSyntaxException(position, "Invalid character 0x%04x",
                    (short)(resolved == null ? source.current() : resolved));
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


    @Nonnull
    private Token parseNumber(char current) {
        char next = source.next();

        if ((current == '0') && ((next == 'x') || (next == 'X'))) {
            parseHex(current);
        } else {
            current = parseInt(current);

            if (current == '.') {
                do {
                    lexeme.append(current);
                    current = source.read();
                } while (DIGITS.contains(current));
            }

            if ((current == 'e') || (current == 'E')) {
                parseExponent(current);
            }

            value = new BigDecimal(lexeme.toString());
        }

        return Token.NUMBER;
    }

    private void parseHex(char current) {
        int size = 0;

        lexeme.append(current);
        current = source.read();

        lexeme.append(current);
        current = source.read();

        while (HEX.contains(current)) {
            lexeme.append(current);
            current = source.read();
            size++;
        }

        if (size == 0) {
            throw new EelSyntaxException(source.position(), "Expected hex digit");
        }

        value = new BigDecimal(new BigInteger(lexeme.substring(2), HEX_BASE));
    }


    private char parseInt(char current) {
        do {
            lexeme.append(current);
            current = source.read();
        } while (DIGITS.contains(current));

        return current;
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
            throw new EelSyntaxException(source.position(),
                "Unexpected char %s(0x%02x)",
                (isAsciiPrintable(current) ? "'" + current + "' " : ""),
                (int) current);
        }

        source.read();

        return result;
    }

    private void parseExponent(char current) {
        lexeme.append(current);
        current = source.read();

        if (current == '-') {
            lexeme.append(current);
            current = source.read();
        }

        if (!DIGITS.contains(current)) {
            throw new EelSyntaxException(source.position(), "Expected exponent");
        }

        parseInt(current);
    }


    private boolean isAsciiPrintable(char test) {
        return ((test >= ' ') && (test < '~'));
    }
}
