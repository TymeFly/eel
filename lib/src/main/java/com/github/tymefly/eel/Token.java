package com.github.tymefly.eel;

/**
 * All the token that can exist within an expression
 */
public enum Token {
    UNDEFINED,

    NUMBER,                                         // Literals
    STRING,
    IDENTIFIER,

    VALUE_INTERPOLATION,                            // Interpolated tokens
    EXPRESSION_INTERPOLATION,
    FUNCTION_INTERPOLATION,

    TRUE,                                           // Predefined constants
    FALSE,

    CONCATENATE,                                    // Text Ops

    LOGICAL_AND,                                    // Logic Ops
    LOGICAL_OR,
    LOGICAL_NOT,

    PLUS,                                           // Numeric Ops
    MINUS,
    MULTIPLY,
    DIVIDE,
    DIVIDE_FLOOR,
    DIVIDE_TRUNCATE,
    MODULUS,
    EXPONENTIATION,

    EQUAL,                                          // Relations
    NOT_EQUAL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    IS_BEFORE,
    IS_AFTER,

    BITWISE_AND,                                    // BitWise Ops
    BITWISE_OR,
    LEFT_SHIFT,
    RIGHT_SHIFT,

    LEFT_PARENTHESES,                               // Brackets
    RIGHT_PARENTHESES,
    LEFT_BRACE,
    RIGHT_BRACE,

    COMMA,                                          // Misc
    QUESTION_MARK,
    COLON,
    HASH,
    TILDE,
    CARET,
    ALL_TOGGLE,
    ALL_UPPER,
    ALL_LOWER,

    END_OF_PROGRAM
}
