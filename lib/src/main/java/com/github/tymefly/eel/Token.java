package com.github.tymefly.eel;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.validate.Preconditions;

/**
 * All the token that can exist within an expression
 */
public enum Token {
    NUMERIC,                                        // Literals
    TEXT_LITERAL,
    IDENTIFIER,

    VALUE_INTERPOLATION("${"),                      // Interpolated tokens
    EXPRESSION_INTERPOLATION("$("),
    FUNCTION_INTERPOLATION("$"),
    LOOK_BACK("$"),

    TRUE("true"),                                   // Predefined constants
    FALSE("false"),

    TEXT("text"),                                   // Convert Ops
    NUMBER("number"),
    LOGIC("logic"),
    DATE("date"),

    CONCATENATE("~>"),                              // Text Ops

    LOGICAL_NOT("not"),                             // Logic Ops
    LOGICAL_AND("and"),
    LOGICAL_OR("or"),
    LOGICAL_XOR("xor"),

    PLUS("+"),                                      // Numeric Ops
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    DIVIDE_FLOOR("//"),
    DIVIDE_TRUNCATE("-/"),
    MODULUS("%"),
    EXPONENTIATION("**"),

    EQUAL("="),                                     // Relational Ops
    NOT_EQUAL("<>"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),
    IS_BEFORE("isBefore"),
    IS_AFTER("isAfter"),
    IN("in"),

    BITWISE_AND("&"),                               // BitWise Ops
    BITWISE_OR("|"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>"),

    LEFT_PARENTHESES("("),                          // Brackets
    RIGHT_PARENTHESES(")"),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),

    SINGLE_QUOTE("'"),                              // Misc
    DOUBLE_QUOTE("\""),
    COMMA(","),
    QUESTION_MARK("?"),
    SEMICOLON(";"),
    COLON(":"),
    BLANK_DEFAULT(":-"),
    HASH("#"),
    TILDE("~"),
    ALL_TOGGLE("~~"),
    CARET("^"),
    ALL_UPPER("^^"),
    ALL_LOWER(",,"),

    UNDEFINED,                                      // Special cases
    END_OF_PROGRAM(Input.END);


    private final String lexeme;

    Token() {
        lexeme = null;
    }

    Token(char lexeme) {
        this(Character.toString(lexeme));
    }

    Token(@Nonnull String lexeme) {
        this.lexeme = lexeme;
    }


    @Nonnull
    String lexeme() {
        Preconditions.checkState((lexeme != null), "Internal error: %s has undefined lexeme", this);

        return lexeme;
    }

    char start() {
        String lexeme = lexeme();

        Preconditions.checkState((this.lexeme.length() == 1), "Internal error: %s has unexpected length", this);

        return lexeme.charAt(0);
    }
}
