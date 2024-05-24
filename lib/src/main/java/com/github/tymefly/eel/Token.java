package com.github.tymefly.eel;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.validate.Preconditions;

/**
 * All the token that can exist within an expression
 */
public enum Token {
    NUMBER,                                         // Literals
    STRING,
    IDENTIFIER,

    VALUE_INTERPOLATION("${"),                      // Interpolated tokens
    EXPRESSION_INTERPOLATION("$("),
    FUNCTION_INTERPOLATION("$"),

    TRUE("true"),                                   // Predefined constants
    FALSE("false"),

    CONCATENATE("~>"),                              // Text Ops

    LOGICAL_AND("and"),                             // Logic Ops
    LOGICAL_OR("or"),
    LOGICAL_NOT("not"),

    PLUS("+"),                                      // Numeric Ops
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    DIVIDE_FLOOR("//"),
    DIVIDE_TRUNCATE("-/"),
    MODULUS("%"),
    EXPONENTIATION("**"),

    EQUAL("="),                                     // Relations
    NOT_EQUAL("<>"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),
    IS_BEFORE("isBefore"),
    IS_AFTER("isAfter"),

    BITWISE_AND("&"),                               // BitWise Ops
    BITWISE_OR("|"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>"),

    LEFT_PARENTHESES("("),                          // Brackets
    RIGHT_PARENTHESES(")"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),

    COMMA(","),                                     // Misc
    QUESTION_MARK("?"),
    COLON(":"),
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
        return Preconditions.checkNotNull(lexeme, "Internal error: %s has undefined lexeme", this);
    }
}
