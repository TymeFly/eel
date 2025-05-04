package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelSemanticException;
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
        FunctionManager functionManager = mock();

        symbolsTable = mock();
        tokenizer = mock();
        context = mock();

        terminals = new LinkedList<>();
        compiler = new LambdaCompiler(context);

        when(context.getMathContext())
            .thenReturn(new MathContext(2, RoundingMode.HALF_UP));
        when(context.getFunctionManager())
            .thenReturn(functionManager);

        when(tokenizer.text(any(Token.class)))
            .thenAnswer(i -> terminals.remove());
        when(tokenizer.interpolate(any(Token.class)))
            .thenAnswer(i -> terminals.remove());

        when(functionManager.compileCall(anyString(), any(EelContext.class), anyList()))
            .thenAnswer(i -> {
                String functionName = i.getArgument(0);
                List<Term> argList = i.getArgument(2);
                List<Value> values = argList.stream()
                    .map(a -> a.evaluate(symbolsTable))
                    .toList();
                Value value = Value.of(functionName + "(" + values + ")");
                Term result = r -> value;

                return result;
            });

        when(symbolsTable.read("myStr"))
            .thenReturn("Hello World!");
        when(symbolsTable.read("myNumber"))
            .thenReturn("1234");
        when(symbolsTable.read("blank"))
            .thenReturn("");
        when(symbolsTable.read("one"))
            .thenReturn("1");
    }


    private void mockToken(@Nonnull Token token) {
        mockToken(token, token.toString(), null);
    }

    private void mockToken(@Nonnull Token token, @Nonnull String lexeme) {
        mockToken(token, lexeme, null);
    }

    private void mockToken(int value) {
        mockToken(Token.NUMERIC, Integer.toString(value), BigDecimal.valueOf(value));
    }

    private void mockToken(@Nonnull BigDecimal value) {
        mockToken(Token.NUMERIC, value.toString(), value);
    }

    private void mockToken(@Nonnull Token token, @Nonnull String lexeme, @Nullable BigDecimal value) {
        int decimal = (value == null ? 0 : value.intValue());
        boolean isDecimal = lexeme.indexOf('.') == -1;

        Tokenizer.Terminal terminal = mock();

        when(terminal.token()).thenReturn(token);
        when(terminal.lexeme()).thenReturn(lexeme);
        when(terminal.value()).thenReturn(value);
        when(terminal.decimal()).thenReturn(decimal);
        when(terminal.isDecimal()).thenReturn(isDecimal);
        when(terminal.position()).thenReturn(terminals.size() + 1);

        terminals.add(terminal);
    }

    /**
     * Unit test {@link Parser#parse()} for an empty string
     */
    @Test
    public void test_empty() {
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertSame("Unexpected value", Value.BLANK, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal Some Text  }
     */
    @Test
    public void test_text() {
        mockToken(Token.TEXT_LITERAL, "Some Text");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Some Text"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal Hello $fred 123 }
     */
    @Test
    public void test_text_with_dollar_and_identifier() {
        mockToken(Token.TEXT_LITERAL, "Hello ");
        mockToken(Token.FUNCTION_INTERPOLATION, "$");
        mockToken(Token.IDENTIFIER, "fred");
        mockToken(Token.NUMERIC, "123");
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: '123' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal myFunction returns: $myFunction()! }
     */
    @Test
    public void test_function_interpolation() {
        mockToken(Token.TEXT_LITERAL, "myFunction returns: ");
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.TEXT_LITERAL, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction returns: myFunction([])!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal myFunction returns: $( $myFunction())! }
     */
    @Test
    public void test_function_interpolation_in_expression() {
        mockToken(Token.TEXT_LITERAL, "myFunction returns: ");
        mockToken(Token.EXPRESSION_INTERPOLATION, "$(");
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.TEXT_LITERAL, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction returns: myFunction([])!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal default variable: ${unknown-$myFunction()}! }
     */
    @Test
    public void test_function_interpolation_in_variable() {
        mockToken(Token.TEXT_LITERAL, "default variable: ");
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "unknown");
        mockToken(Token.MINUS);
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.TEXT_LITERAL, "!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("default variable: myFunction([])!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr} }
     */
    @Test
    public void test_variable() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for various reserved words
     */
    @Test
    public void test_variable_reserved() {
        test_variable_reserved_helper("true", "test-true");
        test_variable_reserved_helper("false", "test-false");
        test_variable_reserved_helper("text", "test-text");
        test_variable_reserved_helper("number", "test-number");
        test_variable_reserved_helper("logic", "test-logic");
        test_variable_reserved_helper("date", "test-date");
        test_variable_reserved_helper("not", "test-not");
        test_variable_reserved_helper("and", "test-and");
        test_variable_reserved_helper("or", "test-or");
        test_variable_reserved_helper("xor", "test-xor");
        test_variable_reserved_helper("isBefore", "test-isBefore");
        test_variable_reserved_helper("isAfter", "test-isAfter");
        test_variable_reserved_helper("in", "test-in");

    }

    private void test_variable_reserved_helper(@Nonnull String key, @Nonnull String value) {
        when(symbolsTable.read(key))
            .thenReturn(value);

        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, key);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value for " + key, Value.of(value), actual);
    }




    /**
     * Unit test {@link Parser#parse()} for {@literal ${unknown-This is the default value} }
     */
    @Test
    public void test_variable_default() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "unknown");
        mockToken(Token.MINUS);
        mockToken(Token.TEXT_LITERAL, "This is the default value");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("This is the default value"), actual);
    }
    /**
     * Unit test {@link Parser#parse()} for {@literal ${blank-This is the default value} }
     */
    @Test
    public void test_variable_blankDefault() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "blank");
        mockToken(Token.BLANK_DEFAULT);
        mockToken(Token.TEXT_LITERAL, "This is the default value");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("This is the default value"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr^^,} }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("hELLO WORLD!"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:6:5} }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:6:-This is the default value} }
     */
    @Test
    public void test_substring_noCount() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(6);
        mockToken(Token.BLANK_DEFAULT);
        mockToken(Token.TEXT_LITERAL, "This is the default value");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World!"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:6} }
     */
    @Test
    public void test_substring_negativeCount() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(6);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World!"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:6.5:5} }
     */
    @Test
    public void test_substring_fractionalIndex() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(new BigDecimal("6.5"));                       // fraction will be truncated
        mockToken(Token.COLON);
        mockToken(5);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:$(2+4):5} }
     */
    @Test
    public void test_substring_start_expression() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2);
        mockToken(Token.PLUS);
        mockToken(4);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.COLON);
        mockToken(5);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr:6:$(2+3)} }
     */
    @Test
    public void test_substring_count_expression() {
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.COLON);
        mockToken(6);
        mockToken(Token.COLON);
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(2);
        mockToken(Token.PLUS);
        mockToken(3);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("World"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${#myStr} }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("12"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${myStr}${myNumber} }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!1234"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal ${} }
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
     * Unit test {@link Parser#parse()} for {@literal ${myStr }
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
     * Unit test {@link Parser#parse()} for {@literal $('text') }
     */
    @Test
    public void test_expression_text() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "text");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("text"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $('text") }
     */
    @Test
    public void test_expression_text_quoteMismatch() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "text");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Exception actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: 'DOUBLE_QUOTE' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $('text) }
     */
    @Test
    public void test_expression_text_quoteMissing() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "text");
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Exception actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 4: 'RIGHT_PARENTHESES' was unexpected", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $('value: ${myStr}!') }
     */
    @Test
    public void test_expression_text_embedded_value() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "value: ");
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.TEXT_LITERAL, "!");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("value: Hello World!!"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $('value: $myFunction()') }
     */
    @Test
    public void test_expression_text_embedded_function() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "value: ");
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("value: myFunction([])"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $('value: $( 1 + 2 )!') }
     */
    @Test
    public void test_expression_text_embedded_expression() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "value: ");
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(1);
        mockToken(Token.PLUS);
        mockToken(2);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.TEXT_LITERAL, "!");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("value: 3!"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $("text" }
     */
    @Test
    public void test_expression_missingParentheses() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "text");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.END_OF_PROGRAM);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 5: Unexpected end of expression", actual.getMessage());
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(( ${myStr} )) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Hello World!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(123~>' <<<'~>${myStr}~>">>> "~>321) }
     */
    @Test
    public void test_concatenation() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(123);
        mockToken(Token.CONCATENATE);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, " <<<");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.CONCATENATE);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.CONCATENATE);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, ">>> ");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.CONCATENATE);
        mockToken(321);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("123 <<<Hello World!>>> 321"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(true) }
     */
    @Test
    public void test_true() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false) }
     */
    @Test
    public void test_false() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(myNumber?) }
     */
    @Test
    public void test_isDefined() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.QUESTION_MARK);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(true=true) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(true=false) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(true!=false) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false!=false) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myStr}='Hello World!') }
     */
    @Test
    public void test_equal_text_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "Hello World!");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myStr}='???') }
     */
    @Test
    public void test_equal_text_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.EQUAL);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "???");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myStr}!='???') }
     */
    @Test
    public void test_notEqual_text_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "???");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myStr}!='Hello World!') }
     */
    @Test
    public void test_notEqual_text_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.NOT_EQUAL);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "Hello World!");
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${one} in {0,1}) }
     */
    @Test
    public void test_in_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "one");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.IN);
        mockToken(Token.LEFT_BRACE);
        mockToken(0);
        mockToken(Token.COMMA);
        mockToken(1);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myNumber} in {0,1}) }
     */
    @Test
    public void test_in_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.IN);
        mockToken(Token.LEFT_BRACE);
        mockToken(0);
        mockToken(Token.COMMA);
        mockToken(1);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myNumber} in {}) }
     */
    @Test
    public void test_in_negative_emptySequence() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myNumber");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.IN);
        mockToken(Token.LEFT_BRACE);
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(true and true) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(true and false) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(false or true) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false or false) }
     */
    @Test
    public void test_logical_or_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_OR);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false xor true) }
     */
    @Test
    public void test_logical_xor_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_XOR);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false xor false) }
     */
    @Test
    public void test_logical_xor_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.FALSE);
        mockToken(Token.LOGICAL_XOR);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(not false) }
     */
    @Test
    public void test_logical_not_positive() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.FALSE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(not true) }
     */
    @Test
    public void test_logical_not_negative() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LOGICAL_NOT);
        mockToken(Token.TRUE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(12 = 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(+1234) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(1234), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(-123) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-123), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(12 = 12.1) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(13 != 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(12 != 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(13 > 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(12 > 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(11 < 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(13 < 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(12.0 >= 11) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(12.0 >= 13) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(11 <= 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(13 <= 12.0) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(11 isBefore 15) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2000 isBefore 15) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2000 isAfter 15) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(20 isAfter 1500) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.FALSE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(12 + 33) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(45), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(27 - 85) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-58), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2 * 3) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(6), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(7 / 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3.5), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(-7 // 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-4), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(-7 -/ 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(-3), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(7 % 2) }
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
            .evaluate(symbolsTable);

         Assert.assertEquals("Unexpected value", Value.of(1), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2 ** 6) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(64), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(14 << 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(56), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(14 >> 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(true ? 2 : 3) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(2), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(false ? 2 : 3) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(3), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $(~0xa5) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0x5A, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(0xcc & 0xaa) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0x88, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(0xcc ^ 0xaa) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of((byte) 0x66), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(0xcc | 0xaa) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Value", (byte) 0xee, actual.asNumber().byteValue());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2 * 3 ** 2 + (4 - 1)) }
     */
    @Test
    public void test_math_precedence() {
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(21), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(2 * 3 = 3 * 2) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( text(123) )  }
     */
    @Test
    public void test_functionCall_Convert_Text() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("123"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $( number("123") )  }
     */
    @Test
    public void test_functionCall_Convert_Number() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "123");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(123), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( logic("true") )  }
     */
    @Test
    public void test_functionCall_Convert_Logic() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "true");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( date(123) )  }
     */
    @Test
    public void test_functionCall_Convert_Date() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of(ZonedDateTime.of(1970, 1, 1, 0, 2, 3, 0, ZoneOffset.UTC)),
            actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $text(123) !! }
     */
    @Test
    public void test_interpolate_Convert_Text() {
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.TEXT);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.TEXT_LITERAL, " !!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("123 !!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $number("123")  }
     */
    @Test
    public void test_interpolate_Convert_Number() {
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.NUMBER);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "123");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(123), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $logic("true")  }
     */
    @Test
    public void test_interpolate_Convert_Logic() {
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.LOGIC);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "true");
        mockToken(Token.DOUBLE_QUOTE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.TRUE, actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $date(123) }
     */
    @Test
    public void test_interpolate_Convert_Date() {
        mockToken(Token.FUNCTION_INTERPOLATION);
        mockToken(Token.DATE);
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of(ZonedDateTime.of(1970, 1, 1, 0, 2, 3, 0, ZoneOffset.UTC)),
            actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( myFunction() ) }
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("myFunction([])"), actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( myFunction('Arg1', 123, true, ${myStr}) ) }
     */
    @Test
    public void test_functionCall_withArguments() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myFunction");
        mockToken(Token.LEFT_PARENTHESES);
        mockToken(Token.SINGLE_QUOTE);
        mockToken(Token.TEXT_LITERAL, "Arg1");
        mockToken(Token.SINGLE_QUOTE);
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
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value",
            Value.of("myFunction([Constant{type=TEXT, value=Arg1}, " +
                                 "Constant{type=NUMBER, value=123}, " +
                                 "Constant{type=LOGIC, value=true}, " +
                                 "Constant{type=TEXT, value=Hello World!}])"),
            actual);
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal Foo-${myStr}-Bar-$(123)-! }
     */
    @Test
    public void test_string_var_text_expression() {
        mockToken(Token.TEXT_LITERAL, "Foo-");
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.TEXT_LITERAL, "-Bar-");
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(123);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.TEXT_LITERAL, "-!");
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of("Foo-Hello World!-Bar-123-!"), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $(${myStr}) } with multiple symbols tables
     */
    @Test
    public void test_changeSymbolsTable() {
        SymbolsTable symbolsTable2 = mock();

        when(symbolsTable2.read("myStr"))
            .thenReturn("otherValue");

        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.VALUE_INTERPOLATION);
        mockToken(Token.IDENTIFIER, "myStr");
        mockToken(Token.RIGHT_BRACE);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Term term = new Parser(context, tokenizer, compiler).parse();

        Assert.assertEquals("First runtime", "Hello World!", term.evaluate(symbolsTable).asText());
        Assert.assertEquals("other runtime", "otherValue", term.evaluate(symbolsTable2).asText());
        Assert.assertEquals("rerun first", "Hello World!", term.evaluate(symbolsTable).asText());
    }


    /**
     * Unit test {@link Parser#parse()} for {@literal $( 1.1 ; 2.2 ; $[1] + $[2] }
     */
    @Test
    public void test_LookBack_happyPath() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(new BigDecimal("1.1"));
        mockToken(Token.SEMICOLON);
        mockToken(new BigDecimal("2.2"));
        mockToken(Token.SEMICOLON);
        mockToken(Token.LOOK_BACK);
        mockToken(1);
        mockToken(Token.RIGHT_BRACKET);
        mockToken(Token.PLUS);
        mockToken(Token.LOOK_BACK);
        mockToken(2);
        mockToken(Token.RIGHT_BRACKET);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        Value actual = new Parser(context, tokenizer, compiler)
            .parse()
            .evaluate(symbolsTable);

        Assert.assertEquals("Unexpected value", Value.of(new BigDecimal("3.3")), actual);
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $( 1 ; $[0] )}
     */
    @Test
    public void test_LookBack_0Index() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(1);
        mockToken(Token.SEMICOLON);
        mockToken(Token.LOOK_BACK);
        mockToken(0);
        mockToken(Token.RIGHT_BRACKET);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        EelSemanticException actual = Assert.assertThrows(EelSemanticException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 6: Undefined lookBack $[0]", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $( 1 ; $[2] )}
     */
    @Test
    public void test_LookBack_undefinedIndex() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(1);
        mockToken(Token.SEMICOLON);
        mockToken(Token.LOOK_BACK);
        mockToken(2);
        mockToken(Token.RIGHT_BRACKET);
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        EelSemanticException actual = Assert.assertThrows(EelSemanticException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 6: Undefined lookBack $[2]", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $( 1 ; $2.2 )}
     */
    @Test
    public void test_LookBack_FractionalIndex() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(1);
        mockToken(Token.SEMICOLON);
        mockToken(Token.LOOK_BACK);
        mockToken(new BigDecimal("2.2"));
        mockToken(Token.RIGHT_PARENTHESES);
        mockToken(Token.END_OF_PROGRAM);

        EelSemanticException actual = Assert.assertThrows(EelSemanticException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 5: Invalid lookBack $2.2", actual.getMessage());
    }

    /**
     * Unit test {@link Parser#parse()} for {@literal $( 123 << }
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
     * Unit test {@link Parser#parse()} for {@literal $( true false }
     */
    @Test
    public void test_unexpectedSymbol() {
        mockToken(Token.EXPRESSION_INTERPOLATION);
        mockToken(Token.TRUE);
        mockToken(Token.FALSE);

        EelSyntaxException actual = Assert.assertThrows(EelSyntaxException.class,
            () -> new Parser(context, tokenizer, compiler).parse());

        Assert.assertEquals("Unexpected message", "Error at position 3: 'FALSE' was unexpected", actual.getMessage());
    }
}