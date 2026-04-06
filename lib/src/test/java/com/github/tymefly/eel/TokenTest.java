package com.github.tymefly.eel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Token}
 */
public class TokenTest {

    /**
     * Unit test {@link Token#lexeme()}
     */
    @Test
    public void test_Lexeme() {
        assertThrows(IllegalStateException.class, Token.NUMERIC::lexeme, "NUMERIC");
        assertThrows(IllegalStateException.class, Token.TEXT_LITERAL::lexeme, "STRING");
        assertThrows(IllegalStateException.class, Token.IDENTIFIER::lexeme, "IDENTIFIER");
        assertThrows(IllegalStateException.class, Token.IDENTIFIER::lexeme, "UNDEFINED");

        assertEquals("${", Token.VALUE_INTERPOLATION.lexeme(), "VALUE_INTERPOLATION");
        assertEquals("$(", Token.EXPRESSION_INTERPOLATION.lexeme(), "EXPRESSION_INTERPOLATION");
        assertEquals("$", Token.FUNCTION_INTERPOLATION.lexeme(), "FUNCTION_INTERPOLATION");
        assertEquals("$", Token.LOOK_BACK.lexeme(), "LOOK_BACK");
        assertEquals("isAfter", Token.IS_AFTER.lexeme(), "IS_AFTER");

        assertEquals("true", Token.TRUE.lexeme(), "TRUE");
        assertEquals("text", Token.TEXT.lexeme(), "TEXT");
        assertEquals("in", Token.IN.lexeme(), "IN");
        assertEquals("and", Token.LOGICAL_AND.lexeme(), "LOGICAL_AND");

        assertEquals("&", Token.BITWISE_AND.lexeme(), "BITWISE_AND");
        assertEquals("+", Token.PLUS.lexeme(), "PLUS");
        assertEquals("**", Token.EXPONENTIATION.lexeme(), "EXPONENTIATION");
        assertEquals("<=", Token.LESS_THAN_EQUAL.lexeme(), "LESS_THAN_EQUAL");
        assertEquals(",,", Token.ALL_LOWER.lexeme(), "ALL_LOWER");

        assertEquals("(", Token.LEFT_PARENTHESES.lexeme(), "LEFT_PARENTHESES");
        assertEquals("{", Token.LEFT_BRACE.lexeme(), "LEFT_BRACE");
        assertEquals("[", Token.LEFT_BRACKET.lexeme(), "LEFT_BRACKET");

        assertEquals("\u0000", Token.END_OF_PROGRAM.lexeme(), "END_OF_PROGRAM");
    }

    /**
     * Unit test {@link Token#start()}
     */
    @Test
    public void test_start() {
        assertThrows(IllegalStateException.class, Token.NUMERIC::start, "NUMERIC");
        assertThrows(IllegalStateException.class, Token.TEXT_LITERAL::start, "STRING");
        assertThrows(IllegalStateException.class, Token.IDENTIFIER::start, "IDENTIFIER");
        assertThrows(IllegalStateException.class, Token.IDENTIFIER::start, "UNDEFINED");

        assertThrows(IllegalStateException.class, Token.VALUE_INTERPOLATION::start, "VALUE_INTERPOLATION");
        assertThrows(IllegalStateException.class, Token.EXPRESSION_INTERPOLATION::start, "EXPRESSION_INTERPOLATION");
        assertEquals('$', Token.FUNCTION_INTERPOLATION.start(), "FUNCTION_INTERPOLATION");
        assertEquals('$', Token.LOOK_BACK.start(), "LOOK_BACK");
        assertThrows(IllegalStateException.class, Token.IS_AFTER::start, "IS_AFTER");

        assertThrows(IllegalStateException.class, Token.TRUE::start, "TRUE");
        assertThrows(IllegalStateException.class, Token.TEXT::start, "TEXT");
        assertThrows(IllegalStateException.class, Token.LOGICAL_AND::start, "LOGICAL_AND");

        assertEquals('&', Token.BITWISE_AND.start(), "BITWISE_AND");
        assertEquals('+', Token.PLUS.start(), "PLUS");
        assertThrows(IllegalStateException.class, Token.EXPONENTIATION::start, "EXPONENTIATION");
        assertThrows(IllegalStateException.class, Token.LESS_THAN_EQUAL::start, "LESS_THAN_EQUAL");
        assertThrows(IllegalStateException.class, Token.ALL_LOWER::start, "ALL_LOWER");

        assertEquals('(', Token.LEFT_PARENTHESES.start(), "LEFT_PARENTHESES");
        assertEquals('{', Token.LEFT_BRACE.start(), "LEFT_BRACE");
        assertEquals('[', Token.LEFT_BRACKET.start(), "LEFT_BRACKET");

        assertEquals('\u0000', Token.END_OF_PROGRAM.start(), "END_OF_PROGRAM");
    }
}