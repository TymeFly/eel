package com.github.tymefly.eel;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Token}
 */
public class TokenTest {

    /**
     * Unit test {@link Token#lexeme()}
     */
    @Test
    public void test_Lexeme() {
        Assert.assertThrows("NUMERIC", IllegalStateException.class, Token.NUMERIC::lexeme);
        Assert.assertThrows("STRING", IllegalStateException.class, Token.TEXT_LITERAL::lexeme);
        Assert.assertThrows("IDENTIFIER", IllegalStateException.class, Token.IDENTIFIER::lexeme);
        Assert.assertThrows("UNDEFINED", IllegalStateException.class, Token.IDENTIFIER::lexeme);

        Assert.assertEquals("VALUE_INTERPOLATION", "${", Token.VALUE_INTERPOLATION.lexeme());
        Assert.assertEquals("EXPRESSION_INTERPOLATION", "$(", Token.EXPRESSION_INTERPOLATION.lexeme());
        Assert.assertEquals("FUNCTION_INTERPOLATION", "$", Token.FUNCTION_INTERPOLATION.lexeme());
        Assert.assertEquals("LOOK_BACK", "$", Token.LOOK_BACK.lexeme());
        Assert.assertEquals("IS_AFTER", "isAfter", Token.IS_AFTER.lexeme());

        Assert.assertEquals("TRUE", "true", Token.TRUE.lexeme());
        Assert.assertEquals("TEXT", "text", Token.TEXT.lexeme());
        Assert.assertEquals("IN", "in", Token.IN.lexeme());
        Assert.assertEquals("LOGICAL_AND", "and", Token.LOGICAL_AND.lexeme());

        Assert.assertEquals("BITWISE_AND", "&", Token.BITWISE_AND.lexeme());
        Assert.assertEquals("PLUS", "+", Token.PLUS.lexeme());
        Assert.assertEquals("EXPONENTIATION", "**", Token.EXPONENTIATION.lexeme());
        Assert.assertEquals("LESS_THAN_EQUAL", "<=", Token.LESS_THAN_EQUAL.lexeme());
        Assert.assertEquals("ALL_LOWER", ",,", Token.ALL_LOWER.lexeme());

        Assert.assertEquals("LEFT_PARENTHESES", "(", Token.LEFT_PARENTHESES.lexeme());
        Assert.assertEquals("LEFT_BRACE", "{", Token.LEFT_BRACE.lexeme());
        Assert.assertEquals("LEFT_BRACKET", "[", Token.LEFT_BRACKET.lexeme());

        Assert.assertEquals("END_OF_PROGRAM", "\u0000", Token.END_OF_PROGRAM.lexeme());
    }

    /**
     * Unit test {@link Token#start()}
     */
    @Test
    public void test_start() {
        Assert.assertThrows("NUMERIC", IllegalStateException.class, Token.NUMERIC::start);
        Assert.assertThrows("STRING", IllegalStateException.class, Token.TEXT_LITERAL::start);
        Assert.assertThrows("IDENTIFIER", IllegalStateException.class, Token.IDENTIFIER::start);
        Assert.assertThrows("UNDEFINED", IllegalStateException.class, Token.IDENTIFIER::start);

        Assert.assertThrows("VALUE_INTERPOLATION", IllegalStateException.class, Token.VALUE_INTERPOLATION::start);
        Assert.assertThrows("EXPRESSION_INTERPOLATION", IllegalStateException.class, Token.EXPRESSION_INTERPOLATION::start);
        Assert.assertEquals("FUNCTION_INTERPOLATION", '$', Token.FUNCTION_INTERPOLATION.start());
        Assert.assertEquals("LOOK_BACK", '$', Token.LOOK_BACK.start());
        Assert.assertThrows("IS_AFTER", IllegalStateException.class, Token.IS_AFTER::start);

        Assert.assertThrows("TRUE", IllegalStateException.class, Token.TRUE::start);
        Assert.assertThrows("TEXT", IllegalStateException.class, Token.TEXT::start);
        Assert.assertThrows("LOGICAL_AND", IllegalStateException.class, Token.LOGICAL_AND::start);

        Assert.assertEquals("BITWISE_AND", '&', Token.BITWISE_AND.start());
        Assert.assertEquals("PLUS", '+', Token.PLUS.start());
        Assert.assertThrows("EXPONENTIATION", IllegalStateException.class, Token.EXPONENTIATION::start);
        Assert.assertThrows("LESS_THAN_EQUAL", IllegalStateException.class, Token.LESS_THAN_EQUAL::start);
        Assert.assertThrows("ALL_LOWER", IllegalStateException.class, Token.ALL_LOWER::start);

        Assert.assertEquals("LEFT_PARENTHESES", '(', Token.LEFT_PARENTHESES.start());
        Assert.assertEquals("LEFT_BRACE", '{', Token.LEFT_BRACE.start());
        Assert.assertEquals("LEFT_BRACKET", '[', Token.LEFT_BRACKET.start());

        Assert.assertEquals("END_OF_PROGRAM", '\u0000', Token.END_OF_PROGRAM.start());
    }
}