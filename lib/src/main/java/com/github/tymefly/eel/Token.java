package com.github.tymefly.eel;

/**
 * All the token that can exist within an expression
 */
public enum Token {
    UNDEFINED,

    NUMBER,                                         // Literals
    STRING,
    IDENTIFIER,

    VARIABLE_EXPANSION,                             // Interpolated tokens
    EXPRESSION_EXPANSION,

    TRUE,                                           // Predefined constants
    FALSE,
    PI,
    E,

    CONCATENATE,                                    // Text Ops

    LOGICAL_AND,                                    // Logic Ops
    LOGICAL_OR,
    SHORT_CIRCUIT_AND,
    SHORT_CIRCUIT_OR,
    LOGICAL_NOT,

    PLUS,                                           // Numeric Ops
    MINUS,
    MULTIPLY,
    DIVIDE,
    MODULUS,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    POWER,

    EQUAL,                                          // Relations
    NOT_EQUAL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,

    BITWISE_AND,                                    // BitWise Ops
    BITWISE_OR,
    BITWISE_XOR,
    BITWISE_NOT,

    LEFT_PARENTHESES,                               // Brackets
    RIGHT_PARENTHESES,
    LEFT_BRACE,
    RIGHT_BRACE,

    CONVERT_TO_TEXT,                                // Convert
    CONVERT_TO_NUMBER,
    CONVERT_TO_LOGIC,
    CONVERT_TO_DATE,

    COMMA,                                          // Misc
    QUESTION_MARK,
    COLON,
    HASH,
    TOGGLE,
    ALL_TOGGLE,
    ALL_UPPER,
    ALL_LOWER,

    END_OF_PROGRAM
}
