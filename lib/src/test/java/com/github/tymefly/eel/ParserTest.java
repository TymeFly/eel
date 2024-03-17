package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelSyntaxException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Parser}
 */
public class ParserTest {
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

        when(symbolsTable.read("myStr"))
            .thenReturn("Hello World!");
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
        BigDecimal rounded = value.setScale(0, RoundingMode.UP);
        boolean fractional = (rounded.compareTo(value) != 0);

        mockToken(Token.NUMBER, value.toString(), value, fractional);
    }

    private void mockToken(@Nonnull Token token, @Nonnull String lexeme, @Nullable BigDecimal value) {
        mockToken(token, lexeme, value, false);
    }

    private void mockToken(@Nonnull Token token, @Nonnull String lexeme, @Nullable BigDecimal value, boolean fractional) {
        Tokenizer.Terminal terminal = mock(Tokenizer.Terminal.class);

        when(terminal.token()).thenReturn(token);
        when(terminal.lexeme()).thenReturn(lexeme);
        when(terminal.value()).thenReturn(value);
        when(terminal.position()).thenReturn(terminals.size() + 1);
        when(terminal.isFractional()).thenReturn(fractional);

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

        Assert.assertSame("Unexpected value", EelValue.BLANK, actual);
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
    public void test_string_with_dollar_and_identifier() {
        mockToken(Token.STRING, "Hello ");
        mockToken(Token.FUNCTION_INTERPOLATION, "$");
        mockToken(Token.IDENTIFIER, "fred");
        mockToken(Token.NUMBER, "123");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: '123' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_function_interpolation() {
        mockToken(Token.STRING, "myFunction returns: ");
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.STRING, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction returns: myFunction([])!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_function_interpolation_in_expression() {
        mockToken(Token.STRING, "myFunction returns: ");
        mockToken(Token.EXPRESSION_INTERPOLATION, "$(");
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.STRING, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction returns: myFunction([])!"), actual);
    }
    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_function_interpolation_in_variable() {
        mockToken(Token.STRING, "default variable: ");
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "unknown");
        mockToken(Token.MINUS);
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.STRING, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("default variable: myFunction([])!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_default() {
        mockToken(Token.VALUE_INTERPOLATION);
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
    public void test_variable_caseChanges() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.ALL_UPPER);
        mockToken(Token.COMMA);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("hELLO WORLD!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_substring() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(6);
        mockToken(Token.COLON);
        mockToken(5);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_substring_fractionalIndex() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(new BigDecimal("6.5"));
        mockToken(Token.COLON);
        mockToken(5);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: '6.5' has unexpected fractional part", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_substring_rangeError_start() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(new BigDecimal(Integer.MAX_VALUE).add(BigDecimal.ONE));
        mockToken(Token.COLON);
        mockToken(0);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: '2147483648' is out of range", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_substring_rangeError_count() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(new BigDecimal(Integer.MAX_VALUE));
        mockToken(Token.COLON);
        mockToken(1);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 6: count is out of range", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_length() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.HASH);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("12"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_two_variables() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!1234"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_variable_missingIdentifier() {
        mockToken(Token.VALUE_INTERPOLATION);
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
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 3: Unexpected end of expression", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_calculation_string() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.STRING, "text");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 3: Unexpected end of expression", actual.getMessage());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_calculation_variable() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_concatenation() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(123);
        mockToken(Token.CONCATENATE);
        mockToken(Token.STRING, " <<<");
        mockToken(Token.CONCATENATE);
        mockToken(Token.VALUE_INTERPOLATION);
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

        Assert.assertEquals("Unexpected value", Value.of("123 <<<Hello World!>>> 321"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_true() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_false() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_defined() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.QUESTION_MARK);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_logic_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.EQUAL);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_logic_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_logic_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_logic_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_string_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.STRING, "Hello World!");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_string_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.STRING, "???");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_string_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.STRING, "???");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_string_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.STRING, "Hello World!");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_and_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_and_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_or_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_OR);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_logical_or_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_AND);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_not_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_not_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_equal_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(12);
        mockToken(Token.EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_positive_number() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(12);
        mockToken(Token.EQUAL);
        mockToken(new BigDecimal("12.1"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(13);
        mockToken(Token.NOT_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_notEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(12);
        mockToken(Token.NOT_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThan_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(13);
        mockToken(Token.GREATER_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThan_numeric_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(12);
        mockToken(Token.GREATER_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThan_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(11);
        mockToken(Token.LESS_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThan_numeric_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(13);
        mockToken(Token.LESS_THAN);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThanEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.GREATER_THAN_EQUAL);
        mockToken(11);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_greaterThanEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.GREATER_THAN_EQUAL);
        mockToken(13);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThanEqual_numeric_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(11);
        mockToken(Token.LESS_THAN_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_lessThanEqual_numeric_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(13);
        mockToken(Token.LESS_THAN_EQUAL);
        mockToken(new BigDecimal("12.0"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_isBefore_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(11);
        mockToken(Token.IS_BEFORE);
        mockToken(15);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_isBefore_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2000);
        mockToken(Token.IS_BEFORE);
        mockToken(15);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_isAfter_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2000);
        mockToken(Token.IS_AFTER);
        mockToken(15);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_isAfter_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(20);
        mockToken(Token.IS_AFTER);
        mockToken(1500);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", EelValue.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_add() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
    public void test_divideFloor() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(-7);
        mockToken(Token.DIVIDE_FLOOR);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-4), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_divideTruncate() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(-7);
        mockToken(Token.DIVIDE_TRUNCATE);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-3), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_modulus() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2);
        mockToken(Token.EXPONENTIATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);

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
        mockToken(Token.EXPRESSION_INTERPOLATION);

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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TILDE);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(0xcc);
        mockToken(Token.CARET);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2);
        mockToken(Token.MULTIPLY);
        mockToken(3);
        mockToken(Token.EXPONENTIATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
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

        Assert.assertEquals("Unexpected value", EelValue.TRUE, actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_functionCall_NoArguments() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
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
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.STRING, "Arg1");
        mockToken(Token.COMMA);
        mockToken(123);
        mockToken(Token.COMMA);
        mockToken(Token.TRUE);
        mockToken(Token.COMMA);
        mockToken(Token.VALUE_INTERPOLATION);
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
                                 "Value{type=TEXT, value=Hello World!}])"),
            actual);
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_string_var_string_expression() {
        mockToken(Token.STRING, "Foo-");
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.STRING, "-Bar-");
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.STRING, "-!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .execute(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Foo-Hello World!-Bar-123-!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_changeSymbolsTable() {
        SymbolsTable symbolsTable2 = mock(SymbolsTable.class);

        when(symbolsTable2.read("myStr"))
            .thenReturn("otherValue");

        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Executor executor = new Parser(context, tokenizer, compiler).parse();

        Assert.assertEquals("First runtime", "Hello World!", executor.execute(symbolsTable).asText());
        Assert.assertEquals("other runtime", "otherValue", executor.execute(symbolsTable2).asText());
        Assert.assertEquals("rerun first", "Hello World!", executor.execute(symbolsTable).asText());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_syntaxError() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(123);
        mockToken(Token.LEFT_SHIFT);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message",
            "Error at position 4: Unexpected end of expression",
            actual.getMessage());
    }


    /**
     * Unit test {@link Parser#parse()}
     */
    @Test
    public void test_unexpectedSymbol() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.FALSE);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler)
                .parse()
                .execute(symbolsTable));

        Assert.assertEquals("Unexpected message", "Error at position 3: 'FALSE' was unexpected", actual.getMessage());
    }
}