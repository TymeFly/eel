package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.utils.CharSetBuilder;
import com.github.tymefly.eel.utils.CharUtils;
import com.github.tymefly.eel.validate.Preconditions;

import static java.util.Map.entry;

/**
 * Convert an input stream into a sequence if terminal fields
 */
class Tokenizer {
    /**
     * Description of a single terminal read by the tokenizer
     * @param token         Token this terminal describes
     * @param lexeme        The literal text of this token. If {@code token} is a {@link Token#NUMBER} then this
     *                          an empty string.
     * @param value         If {@code token} is a {@link Token#NUMBER} then this holds value of the number
     * @param position      start position in the input stream of the token.
     */
    record Terminal(@Nonnull Token token,
                    @Nonnull String lexeme,
                    @Nullable BigDecimal value,
                    int position) {
        @Override
        @Nonnull
        public BigDecimal value() {
            Preconditions.checkState((value != null), "Value is undefined for a " + token);

            return value;
        }
    }

    /**
     * Scanning mode used to read the next token. This determines where the breaks between tokens are.
     */
    enum Mode {
        /**
         * Process a full expression
         */
        EXPRESSION(false),

        /**
         * Process an interpolation sequence
         */
        INTERPOLATE(true);


        private final boolean interpolate;

        Mode(boolean interpolate) {
            this.interpolate = interpolate;
        }
    }


    private static final Set<Character> WHITESPACE = Set.of(' ', '\t');
    private static final Map<Character, Character> ESCAPED = Map.ofEntries(
        entry('\\', '\\'),
        entry('\'', '\''),
        entry('"', '"'));
    private static final Map<String, Token> RESERVED = Map.ofEntries(
        asEntry(Token.TRUE),                            // Constants
        asEntry(Token.FALSE),
        asEntry(Token.LOGICAL_AND),                     // Logic Ops
        asEntry(Token.LOGICAL_OR),
        asEntry(Token.LOGICAL_NOT),
        asEntry(Token.IS_BEFORE),                       // Date Ops
        asEntry(Token.IS_AFTER));
    private static final Map<String, Token> SYMBOLS = Map.ofEntries(
        asEntry(Token.CONCATENATE),                     // Text Ops
        asEntry(Token.PLUS),                            // Maths Ops
        asEntry(Token.MINUS),
        asEntry(Token.MULTIPLY),
        asEntry(Token.DIVIDE),
        asEntry(Token.DIVIDE_FLOOR),
        asEntry(Token.DIVIDE_TRUNCATE),
        asEntry(Token.MODULUS),
        asEntry(Token.EXPONENTIATION),
        asEntry(Token.EQUAL),                           // Relational Ops
        asEntry(Token.NOT_EQUAL),
        entry("!=", Token.NOT_EQUAL),
        asEntry(Token.GREATER_THAN),
        asEntry(Token.GREATER_THAN_EQUAL),
        asEntry(Token.LESS_THAN),
        asEntry(Token.LESS_THAN_EQUAL),
        asEntry(Token.BITWISE_AND),                     // Bitwise Ops
        asEntry(Token.BITWISE_OR),
        asEntry(Token.LEFT_SHIFT),
        asEntry(Token.RIGHT_SHIFT),
        asEntry(Token.LEFT_PARENTHESES),                // Other Symbols
        asEntry(Token.RIGHT_PARENTHESES),
        asEntry(Token.LEFT_BRACE),
        asEntry(Token.RIGHT_BRACE),
        asEntry(Token.COMMA),
        asEntry(Token.QUESTION_MARK),
        asEntry(Token.COLON),
        asEntry(Token.HASH),
        asEntry(Token.TILDE),
        asEntry(Token.ALL_TOGGLE),
        asEntry(Token.CARET),
        asEntry(Token.ALL_UPPER),
        asEntry(Token.ALL_LOWER));
    private static final Set<Character> LETTER = new CharSetBuilder()
        .range('a', 'z')
        .range('A', 'Z')
        .immutable();
    private static final Set<Character> DECIMAL_DIGITS = new CharSetBuilder()
        .range('0', '9')
        .immutable();
    private static final Set<Character> IDENTIFIER_START = new CharSetBuilder()
        .withAll(LETTER)
        .with('_')
        .immutable();
    private static final Set<Character> IDENTIFIER_INTERNAL = new CharSetBuilder()
        .withAll(IDENTIFIER_START)
        .withAll(DECIMAL_DIGITS)
        .with('.')
        .with('[').with(']')
        .immutable();


    private final Source source;
    private final StringBuilder lexeme;
    private BigDecimal value;


    Tokenizer(@Nonnull Source source) {
        this.source = source;
        this.lexeme = new StringBuilder();
    }


    @Nonnull
    private static Map.Entry<String, Token> asEntry(@Nonnull Token token) {
        return entry(token.lexeme(), token);
    }


    /**
     * Read the next terminal symbol from the source. The follow character is assumed to be {@link Input#END}
     * @param mode  Current read mode
     * @return      The next terminal symbol
     */
    @Nonnull
    Terminal next(@Nonnull Mode mode) {
        return next(mode, Input.END);
    }

    /**
     * Read the next terminal symbol from the source
     * @param mode      Current read mode
     * @param follow    The character, which may be {@link Input#END} that terminates the current expression
     * @return          The next terminal symbol
     */
    @Nonnull
    Terminal next(@Nonnull Mode mode, char follow) {
        int position = source.position();
        char current = source.current();
        Token token;

        lexeme.setLength(0);
        value = null;

        do {
            if (current == Input.END) {
                token = Token.END_OF_PROGRAM;
            } else if (mode.interpolate && !endInterpolation(follow)) {
                token = interpolateString(follow);
            } else if (current == '$') {
                token = parseDollar(follow);
            } else if (WHITESPACE.contains(current)) {
                current = source.read();
                position = source.position();
                token = Token.UNDEFINED;
            } else if ((current == '"') || (current == '\'')) {
                token = parseString(current);
            } else if ((current == '.') || DECIMAL_DIGITS.contains(current)) {
                token = parseNumber();
            } else if (IDENTIFIER_START.contains(current)) {
                token = parseIdentifier();
            } else {
                token = parseSymbol(current);
            }
        } while (token == Token.UNDEFINED);

        return new Terminal(token, lexeme.toString(), value, position);
    }

    @Nonnull
    private Token parseDollar(char follow) {
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
            token = interpolateString(follow);
        }

        return token;
    }

    @Nonnull
    private Token interpolateString(char follow) {
        boolean done;

        do {
            char current = appendLexme();

            done = (current == Input.END) || endInterpolation(follow);
        } while (!done);

        return Token.STRING;
    }

    private boolean endInterpolation(char follow) {
        char current = source.current();

        return (current == follow) || (current == '$');
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

            if (current == Input.END) {
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


    @Nonnull
    private Token parseNumber() {
        try {
            value = NumberParser.parse(source);
        } catch (NumberFormatException e) {
            String message = e.getMessage();

            if ((message == null) || message.isEmpty()) {
                message = "Invalid Number";
            }

            throw new EelSyntaxException(source.position(), message, e);
        }

        return Token.NUMBER;
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

    private char appendLexme() {
        lexeme.append(source.current());

        return source.read();
    }


    private void unexpectedCharacter(char current) {
        throw new EelSyntaxException(source.position(),
            "Unexpected char %s(0x%02x)",
            (CharUtils.isAsciiPrintable(current) ? "'" + current + "' " : ""),
            (int) current);
    }
}
