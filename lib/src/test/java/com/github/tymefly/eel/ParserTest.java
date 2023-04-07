package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Parser}
 */
public class ParserTest {
    private static final ZonedDateTime DATE = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);

    private SymbolsTable symbolsTable;
    private Tokenizer tokenizer;

    private EelContextImpl context;
    private LambdaCompiler compiler;
    private Queue<Tokenizer.Terminal> terminals;


    @Before
    public void setUp() {
        FunctionManager functionManager = mock(FunctionManager.class);

        symbolsTable = mock(SymbolsTable.class);
        tokenizer = mock(Tokenizer.class);
        context = mock(EelContextImpl.class);

        terminals = new LinkedList<>();
        compiler = new LambdaCompiler(context);

        when(context.getMathContext())
            .thenReturn(new MathContext(2, RoundingMode.HALF_UP));
        when(context.getFunctionManager())
            .thenReturn(functionManager);

        when(tokenizer.next(any(Tokenizer.Mode.class)))
            .thenAnswer(i -> terminals.remove());

        when(functionManager.compileCall(anyString(), any(EelContext.class), anyList()))
            .thenAnswer(i -> {
                String functionName = i.getArgument(0);
                List<Executor> argList = i.getArgument(2);
                List<Value> values = argList.stream()
                    .map(a -> a.execute(symbolsTable))
                    .collect(Collectors.toList());
                Value value = Value.of(functionName + "(" + values + ")");
                Executor result = r -> value;

                return result;
            });
        when(functionManager.compileCall(eq("time.utc"), any(EelContext.class), eq(Collections.emptyList())))
            .thenAnswer(i -> {
                Executor result = r -> Value.of(DATE);

                return result;
            });

        when(symbolsTable.read("myStr"))
            .thenReturn("Hello World");
        when(symbolsTable.read("myNumber"))
            .thenReturn("1234");
    }

    private void mockToken(@Nonnull Token token) {
        mockToken(token, token.toString(), null);
    }


    private void mockToken(@Nonnull Token token, @Nonnull String lexeme) {
        mockToken(token, lexeme, null);
    }


    private void mockToken(int value) {
        mockToken(Token.NUMBER, Integer.toString(value), BigDecimal.valueOf(value));
    }


    private void mockToken(@Nonnull BigDecimal value) {
        mockToken(Token.NUMBER, value.toString(), value);
    }


    private void mockToken(@Nonnull Token token, @Nonnull String lexeme, @Nullable BigDecimal value) {
        Tokenizer.Terminal terminal = mock(Tokenizer.Terminal.class);

        when(terminal.token()).thenReturn(token);
        when(terminal.lexeme()).thenReturn(lexeme);
        when(terminal.value()).thenReturn(value);
        when(terminal.position()).thenReturn(terminals.size() + 1);

        terminals.add(terminal);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_empty() {
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.BLANK, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_string() {
        mockToken(Token.STRING, "Some Text");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Some Text"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_default() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "unknown");
        mockToken(Token.MINUS);
        mockToken(Token.STRING, "This is the default value");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("This is the default value"), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_length() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.HASH);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(11), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_two_variables() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World1234"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_missingIdentifier() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.RIGHT_BRACE, "}");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 2: '}' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_missingBrace() {
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 3: 'END_OF_PROGRAM' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_calculation_string() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.STRING, "text");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("text"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_expression_missingParentheses() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.STRING, "text");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 3: 'END_OF_PROGRAM' was unexpected", actual.getMessage());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_calculation_variable() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_concatenation() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(123);
        mockToken(Token.CONCATENATE);
        mockToken(Token.STRING, " <<<");
        mockToken(Token.CONCATENATE);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.CONCATENATE);
        mockToken(Token.STRING, ">>> ");
        mockToken(Token.CONCATENATE);
        mockToken(321);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("123 <<<Hello World>>> 321"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_e() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.E);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertTrue("Unexpected value: " + actual.asText(), actual.asText().startsWith("2.71828182"));
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_pi() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.PI);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertTrue("Unexpected value: " + actual.asText(), actual.asText().startsWith("3.141592653"));
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_true() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_false() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_defined() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.QUESTION_MARK);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_logic_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.EQUAL);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_logic_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_logic_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_logic_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_string_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.STRING, "Hello World");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_string_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.STRING, "???");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_string_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.STRING, "???");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_string_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.STRING, "Hello World");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_and_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_and_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_short_circuit_and_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.SHORT_CIRCUIT_AND);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_short_circuit_and_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.TRUE);
        mockToken(Token.SHORT_CIRCUIT_AND);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_or_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_OR);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_or_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_short_circuit_or_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.SHORT_CIRCUIT_OR);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_short_circuit_or_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.FALSE);
        mockToken(Token.SHORT_CIRCUIT_OR);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_not_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_not_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(12);
        mockToken(Token.EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_positive_number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.PLUS);
        mockToken(1234);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(1234), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_negative_number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.MINUS);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-123), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(12);
        mockToken(Token.EQUAL);
        mockToken(new BigDecimal("12.1"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(13);
        mockToken(Token.NOT_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(12);
        mockToken(Token.NOT_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThan_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(13);
        mockToken(Token.GREATER_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThan_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(12);
        mockToken(Token.GREATER_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThan_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(11);
        mockToken(Token.LESS_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThan_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(13);
        mockToken(Token.LESS_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThanEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.GREATER_THAN_EQUAL);
        mockToken(11);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThanEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.GREATER_THAN_EQUAL);
        mockToken(13);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThanEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(11);
        mockToken(Token.LESS_THAN_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThanEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(13);
        mockToken(Token.LESS_THAN_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_add() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(12);
        mockToken(Token.PLUS);
        mockToken(33);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(45), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_subtract() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(27);
        mockToken(Token.MINUS);
        mockToken(85);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-58), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_multiply() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(2);
        mockToken(Token.MULTIPLY);
        mockToken(3);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(6), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_divide() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(7);
        mockToken(Token.DIVIDE);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3.5), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_modulus() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(7);
        mockToken(Token.MODULUS);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

         Assert.assertEquals("Unexpected value", Value.of(1), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_power() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(2);
        mockToken(Token.POWER);
        mockToken(6);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(64), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_leftShift() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(14);
        mockToken(Token.LEFT_SHIFT);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(56), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_rightShift() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(14);
        mockToken(Token.RIGHT_SHIFT);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_Ternary_conditional_positive() {
        mockToken(Token.EXPRESSION_EXPANSION);

        mockToken(Token.TRUE);
        mockToken(Token.QUESTION_MARK);
        mockToken(2);
        mockToken(Token.COLON);
        mockToken(3);

        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(2), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_Ternary_conditional_negative() {
        mockToken(Token.EXPRESSION_EXPANSION);

        mockToken(Token.FALSE);
        mockToken(Token.QUESTION_MARK);
        mockToken(2);
        mockToken(Token.COLON);
        mockToken(3);

        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_bitwise_not() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.BITWISE_NOT);
        mockToken(0xa5);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0x5A, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_bitwise_and() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(0xcc);
        mockToken(Token.BITWISE_AND);
        mockToken(0xaa);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0x88, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_bitwise_xor() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(0xcc);
        mockToken(Token.BITWISE_XOR);
        mockToken(0xaa);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of((byte) 0x66), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_bitwise_or() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(0xcc);
        mockToken(Token.BITWISE_OR);
        mockToken(0xaa);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0xee, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_math() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(2);
        mockToken(Token.MULTIPLY);
        mockToken(3);
        mockToken(Token.POWER);
        mockToken(2);
        mockToken(Token.PLUS);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(4);
        mockToken(Token.MINUS);
        mockToken(1);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(21), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_mixed_numeric_and_logic() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(2);
        mockToken(Token.MULTIPLY);
        mockToken(3);
        mockToken(Token.EQUAL);
        mockToken(3);
        mockToken(Token.MULTIPLY);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Text_Text() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "Hello");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Number_Text() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(12345);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("12345"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Logic_Text() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("true"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Date_Text() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.IDENTIFIER, "time.utc");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("2000-01-02T03:04:05Z"), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Text_Number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "123.456");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(123.456), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Number_Number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(12345);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(12345), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Logic_Number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.ONE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Date_Number() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.IDENTIFIER, "time.utc");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(946782245), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Text_Logic() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "false");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Number_Logic() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(1);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Logic_Logic() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Date_Logic() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.IDENTIFIER, "time.utc");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        EelConvertException actual = Assert.assertThrows(EelConvertException.class,
            () -> new Parser(context, tokenizer, compiler)
                .parse()
                .execute(symbolsTable));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().matches("Can not convert Value\\{type=DATE, value=[0-9T:-]+Z} to LOGIC"));
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_convert_Text_Date() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "2000-01-02T03:04:05Z");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(DATE), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Number_Date() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(946782245);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(DATE), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Logic_Date() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        EelConvertException actual = Assert.assertThrows(EelConvertException.class,
            () -> new Parser(context, tokenizer, compiler)
                .parse()
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message",
            "Can not convert Value{type=LOGIC, value=true} to DATE",
            actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_cast_Date_Date() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.CONVERT_TO_DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.IDENTIFIER, "time.utc");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(DATE), actual);
    }



    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_functionCall_NoArguments() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction([])"), actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_functionCall_withArguments() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "Arg1");
        mockToken(Token.COMMA);
        mockToken(123);
        mockToken(Token.COMMA);
        mockToken(Token.TRUE);
        mockToken(Token.COMMA);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of("myFunction([Value{type=TEXT, value=Arg1}, " +
                                 "Value{type=NUMBER, value=123}, " +
                                 "Value{type=LOGIC, value=true}, " +
                                 "Value{type=TEXT, value=Hello World}])"),
            actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_string_var_string_expression() {
        mockToken(Token.STRING, "Foo-");
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.STRING, "-Bar-");
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.STRING, "-!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Foo-Hello World-Bar-123-!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_changeSymbolsTable() {
        SymbolsTable symbolsTable2 = mock(SymbolsTable.class);

        when(symbolsTable2.read("myStr"))
            .thenReturn("otherValue");

        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.VARIABLE_EXPANSION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Executor executor = new Parser(context, tokenizer, compiler).parse();

        Assert.assertEquals("First runtime", "Hello World", executor.execute(symbolsTable).asText());
        Assert.assertEquals("other runtime", "otherValue", executor.execute(symbolsTable2).asText());
        Assert.assertEquals("rerun first", "Hello World", executor.execute(symbolsTable).asText());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_syntaxError() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(123);
        mockToken(Token.LEFT_SHIFT);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message",
            "Error at position 4: 'END_OF_PROGRAM' was unexpected",
            actual.getMessage());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_unexpectedSymbol() {
        mockToken(Token.EXPRESSION_EXPANSION);
        mockToken(Token.PI);
        mockToken(Token.E);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler)
                .parse()
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message", "Error at position 3: 'E' was unexpected", actual.getMessage());
    }
}