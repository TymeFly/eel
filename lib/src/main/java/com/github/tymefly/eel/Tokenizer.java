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
     * @param lexeme        The literal text of this token. If {@code token} is a {@link Token#NUMERIC} then this
     *                          an empty string.
     * @param value         If {@code token} is a {@link Token#NUMERIC} then this holds value of the number
     * @param isDecimal     {@literal true} only if {@code value} is a decimal number. That is no alternative number
     *                          bases, no fractional parts, and it's not in scientific notation
     * @param position      start position in the input stream of the token.
     */
    record Terminal(@Nonnull Token token,
                    @Nonnull String lexeme,
                    @Nullable BigDecimal value,
                    boolean isDecimal,
                    int position) {
        @Override
        @Nonnull
        public BigDecimal value() {
            Preconditions.checkState((value != null), "Value is undefined for a " + token);

            return value;
        }

        public int decimal() {
            Preconditions.checkState((value != null), "Value is undefined for a " + token);
            Preconditions.checkState(isDecimal, "Value '" + lexeme + "' is not decimal");

            return value.intValue();
        }
    }

    private static final int COMMENT_TOKEN_LEN = 2;

    private static final int UNICODE_DIGITS = 4;
    private static final int HEX_DIGIT_BITS = UNICODE_DIGITS;

    private static final Set<Character> NEW_LINE = Set.of('\n', '\r');
    private static final Set<Character> WHITESPACE = new CharSetBuilder(NEW_LINE)
        .with (' ')
        .with('\t')
        .immutable();
    private static final Set<Character> END_COMMENT = new CharSetBuilder(NEW_LINE)
        .with(Source.END)
        .immutable();
    private static final Map<Character, Character> ESCAPED = Map.ofEntries(
        entry('$', '$'),
        entry('t', '\t'),
        entry('f', '\f'),
        entry('n', '\n'),
        entry('r', '\r'),
        entry('b', '\b'),
        entry('\\', '\\'),
        entry('\'', '\''),
        entry('"', '"'));
    private static final Map<String, Token> RESERVED = Map.ofEntries(
        asEntry(Token.TRUE),                            // Constants
        asEntry(Token.FALSE),
        asEntry(Token.TEXT),                            // Conversion Ops
        asEntry(Token.NUMBER),
        asEntry(Token.LOGIC),
        asEntry(Token.DATE),
        asEntry(Token.LOGICAL_NOT),                     // Logic Ops
        asEntry(Token.LOGICAL_AND),
        asEntry(Token.LOGICAL_OR),
        asEntry(Token.LOGICAL_XOR),
        asEntry(Token.IS_BEFORE),                       // Named relational Ops
        asEntry(Token.IS_AFTER),
        asEntry(Token.IN));
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
        asEntry(Token.LEFT_BRACKET),
        asEntry(Token.RIGHT_BRACKET),
        asEntry(Token.LEFT_BRACE),
        asEntry(Token.RIGHT_BRACE),
        asEntry(Token.SINGLE_QUOTE),
        asEntry(Token.DOUBLE_QUOTE),
        asEntry(Token.COMMA),
        asEntry(Token.QUESTION_MARK),
        asEntry(Token.SEMICOLON),
        asEntry(Token.COLON),
        asEntry(Token.BLANK_DEFAULT),
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
    private boolean isDecimal;


    Tokenizer(@Nonnull Source source) {
        this.source = source;
        this.lexeme = new StringBuilder();
    }


    @Nonnull
    private static Map.Entry<String, Token> asEntry(@Nonnull Token token) {
        return entry(token.lexeme(), token);
    }

    /**
     * Read the next terminal symbol from the source as text
     * @param follow    The character, which may be {@link Input#END} that terminates the current expression
     * @return          The next terminal symbol.
     */
    @Nonnull
    Terminal text(@Nonnull Token follow) {
        char followChar = follow.start();
        int position = source.position();
        char current = source.current();
        Token token;

        initialise();

        while (NEW_LINE.contains(current)) {                    // Skip leading line breaks
            current = source.read();
        }

        if (current == Input.END) {
            token = Token.END_OF_PROGRAM;
        } else if (current == '$') {
            token = parseDollar(followChar);
        } else if (!endText(followChar)) {
            token = interpolateText(followChar);
        } else {
            token = parseSymbol(current);
        }

        return new Terminal(token, lexeme.toString(), value, isDecimal, position);
    }


    /**
     * Read the next terminal symbol from the source as part of an interpolation sequence
     * @param follow    The character, which may be {@link Input#END} that terminates the current expression
     * @return          The next terminal symbol
     */
    @Nonnull
    Terminal interpolate(@Nonnull Token follow) {
        int position = source.position();
        Token token = Token.UNDEFINED;
        char current;

        initialise();

        while (token == Token.UNDEFINED) {
            current = source.current();

            if (current == Input.END) {
                token = Token.END_OF_PROGRAM;
            } else if (current == '$') {
                token = parseDollar(follow.start());
            } else if (WHITESPACE.contains(current)) {
                source.read();
                position = source.position();
            } else if ((current == '.') || DECIMAL_DIGITS.contains(current)) {
                token = parseNumber();
            } else if (IDENTIFIER_START.contains(current)) {
                token = parseIdentifier();
            } else if ((current == '#') && (source.next() == '#')) {
                parseComment();
            } else {
                token = parseSymbol(current);
            }
        }

        return new Terminal(token, lexeme.toString(), value, isDecimal, position);
    }

    private void initialise() {
        lexeme.setLength(0);
        value = null;
        isDecimal = false;
    }


    private void parseComment() {
        int closingHash = 0;
        boolean done = false;

        source.read();
        source.read();

        while (!done) {
            char current = source.read();

            if (current == '#') {
                done = (++closingHash == COMMENT_TOKEN_LEN);
            } else {
                done = END_COMMENT.contains(current);
                closingHash = 0;
            }
        }

        source.read();
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
        } else if (next == '[') {
            token = Token.LOOK_BACK;
            appendLexme();
            appendLexme();
        } else if (IDENTIFIER_START.contains(next)) {
            token = Token.FUNCTION_INTERPOLATION;
            appendLexme();
        } else {
            token = interpolateText(follow);
        }

        return token;
    }

    @Nonnull
    private Token interpolateText(char follow) {
        boolean done;
        char current = source.current();

        do {
            if (current == '\\') {
                current = escapedCharacter();
            } else if (!isValidSourceCharacter(current)) {
                unexpectedCharacter();
            } else {
                // Do nothing - this was a printable source character
            }

            lexeme.append(current);
            current = source.read();

            done = endText(follow);
        } while (!done);

        return Token.TEXT_LITERAL;
    }

    private boolean endText(char follow) {
        boolean terminate;
        char current = source.current();

        terminate = (current == Input.END);
        terminate = terminate || (current == follow);
        terminate = terminate || (current == '$');

        return terminate;
    }


    @Nonnull
    private Token parseIdentifier() {
        char current;

        do {
            current = appendLexme();
        } while (IDENTIFIER_INTERNAL.contains(current));

        return RESERVED.getOrDefault(lexeme.toString(), Token.IDENTIFIER);
    }


    // see https://stackoverflow.com/questions/220547/printable-char-in-java
    private boolean isValidSourceCharacter(@Nullable Character test) {
        boolean invalid = (test == null);
        invalid = invalid || (Character.isISOControl(test));

        if (!invalid) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(test);

            invalid = (block == null);
            invalid = invalid || (block == Character.UnicodeBlock.SPECIALS);
        }

        return !invalid;
    }

    private char escapedCharacter() {
        Character resolved;
        char next = source.read();

        if (next == 'u') {
            resolved = parseUnicodeCharacter();
        } else {
            resolved = ESCAPED.get(next);
        }

        if (resolved == null) {
            unexpectedCharacter();
            resolved = 0;                       // Should not happen
        }

        return resolved;
    }

    @Nullable
    private Character parseUnicodeCharacter() {
        int value = 0;
        int count = UNICODE_DIGITS;
        boolean valid = true;

        while (valid && (count-- != 0)) {
            char in = source.read();
            int hex = CharUtils.hexValue(in);

            valid = (hex != -1);
            value = (value << HEX_DIGIT_BITS) | hex;
        }

        return (valid ? (char) value : null);
    }


    @Nonnull
    private Token parseNumber() {
        try {
            NumberParser parser = NumberParser.parse(source);

            lexeme.setLength(0);
            lexeme.append(parser.getText());
            value = parser.getValue();
            isDecimal = parser.isDecimal();
        } catch (NumberFormatException e) {
            String message = e.getMessage();

            if ((message == null) || message.isEmpty()) {
                message = "Invalid Number";
            }

            throw new EelSyntaxException(source.position(), message, e);
        }

        return Token.NUMERIC;
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


    private void unexpectedCharacter() {
        unexpectedCharacter(source.current());
    }

    private void unexpectedCharacter(char current) {
        throw new EelSyntaxException(source.position(),
            "Unexpected char %s(0x%02x)",
            (CharUtils.isAsciiPrintable(current) ? "'" + current + "' " : ""),
            (int) current);
    }
}
