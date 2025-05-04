package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link LambdaCompiler}
 */
public class LambdaCompilerTest {
    private static final ZonedDateTime START = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("UTC"));
    private static final ZonedDateTime ONE_SECOND = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1), ZoneId.of("UTC"));

    private SymbolsTable symbols;

    private LambdaCompiler compile;

    @Before
    public void setUp() {
        EelContextImpl context = mock();

        symbols = mock();

        when(context.getMathContext())
            .thenReturn(new MathContext(3, RoundingMode.HALF_UP));

        when(symbols.read("key"))
            .thenReturn("This Value!");
        when(symbols.read("blank"))
            .thenReturn("");

        compile = new LambdaCompiler(context);
    }

                //*** Mock values that are not constants ***//

    @Nonnull
    private static Term mockValue(@Nonnull Value result) {
        Term value = mock();

        when(value.isConstant())
            .thenReturn(false);
        when(value.evaluate(any(SymbolsTable.class)))
            .thenReturn(result);

        return value;
    }


                //*** Cached terms ***//

    /**
     * Unit test {@link LambdaCompiler#cached(Term)}
     */
    @Test
    public void test_Cached_constant() {
        Term backing = spy(Constant.of("abc"));
        Term actual = compile.cached(backing);

        Value value = actual.evaluate(symbols);
        Assert.assertEquals("#1 evaluate()", "abc", value.asText());
        verify(backing, times(1)).evaluate(any(SymbolsTable.class));

        value = actual.evaluate(symbols);
        Assert.assertEquals("#2 evaluate()", "abc", value.asText());
        verify(backing, times(2)).evaluate(any(SymbolsTable.class));
    }

    /**
     * Unit test {@link LambdaCompiler#cached(Term)}
     */
    @Test
    public void test_Cached_variable() {
        Term backing = mockValue(Constant.of("abc"));
        Term actual = compile.cached(backing);

        Value value = actual.evaluate(symbols);
        Assert.assertEquals("#1 evaluate()", "abc", value.asText());
        verify(backing, times(1)).evaluate(any(SymbolsTable.class));

        value = actual.evaluate(symbols);
        Assert.assertEquals("#2 evaluate()", "abc", value.asText());
        verify(backing, times(1)).evaluate(any(SymbolsTable.class));
    }


                //*** Variables ***//

    /**
     * Unit test {@link LambdaCompiler#isDefined(String)}
     */
    @Test
    public void test_isDefined_defined() {
        Term actual = compile.isDefined("key");
        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#isDefined(String)}
     */
    @Test
    public void test_isDefined_undefined() {
        Term actual = compile.isDefined("bad.key");
        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }


    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_withoutCaseOp() {
        Term actual = compile.read("key")
            .build();
        Value value = actual.evaluate(symbols);

        assertText("This Value!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_withCaseOp() {
        Term actual = compile.read("key")
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.evaluate(symbols);

        assertText("tHIS vALUE!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_defaultIgnored() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("key")
            .withDefault(defaultValue)
            .build();
        Value value = actual.evaluate(symbols);

        assertText("This Value!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_defaultString() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("other")
            .withDefault(defaultValue)
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.evaluate(symbols);

        assertText("Hello", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_blankDefaultIgnored() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("key")
            .withBlankDefault(defaultValue)
            .build();
        Value value = actual.evaluate(symbols);

        assertText("This Value!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_blankDefaultUndefined() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("other")
            .withBlankDefault(defaultValue)
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.evaluate(symbols);

        assertText("Hello", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_blankDefaultBlank() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("blank")
            .withBlankDefault(defaultValue)
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.evaluate(symbols);

        assertText("Hello", value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_length() {
        Term defaultValue = Constant.of("Hello");
        Term actual = compile.read("key")
            .withDefault(defaultValue)
            .withTransformation(Compiler.SymbolTransformation.LENGTH)
            .build();
        Value value = actual.evaluate(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", 11, value.asInt());
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_defaultLength() {
        Term defaultValue = Constant.of(95);
        Term actual = compile.read("other")
            .withDefault(defaultValue)
            .withTransformation(Compiler.SymbolTransformation.LENGTH)
            .build();
        Value value = actual.evaluate(symbols);

        assertNumber(95, value);
    }

    /**
     * Unit test {@link LambdaCompiler#read(String)}
     */
    @Test
    public void test_variable_Unknown() {
        EelUnknownSymbolException actual = Assert.assertThrows(EelUnknownSymbolException.class,
            () -> compile.read("other").build().evaluate(symbols));

        Assert.assertEquals("Unexpected message",
            "Unknown variable 'other'",
            actual.getMessage());
    }

                //*** Constants ***//

    /**
     * Unit test {@link LambdaCompiler#textConstant(String)}
     */
    @Test
    public void test_textConstant() {
        Term actual = compile.textConstant("Hello World");
        Value value = actual.evaluate(symbols);

        assertText("Hello World", value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_true() {
        Term actual = compile.logicConstant(true);
        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_false() {
        Term actual = compile.logicConstant(false);
        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#numericConstant(Number)}
     */
    @Test
    public void test_numericConstant() {
        Term actual = compile.numericConstant(123.456);
        Value value = actual.evaluate(symbols);

        assertNumber(new BigDecimal("123.456"), value);
    }


                //*** ternary Op ***//


    /**
     * Unit test {@link LambdaCompiler#conditional(Term, Term, Term)}
     */
    @Test
    public void test_conditional_true_variables() {
        Term condition = mockValue(Value.TRUE);
        Term first = Constant.of("ab");
        Term second = Constant.of("cd");

        Term actual = compile.conditional(condition, first, second);

        verify(condition, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        verify(condition).evaluate(any(SymbolsTable.class));

        assertText("ab", value);
    }


    /**
     * Unit test {@link LambdaCompiler#conditional(Term, Term, Term)}
     */
    @Test
    public void test_conditional_false_variables() {
        Term condition = mockValue(Value.FALSE);
        Term first = Constant.of(1);
        Term second = Constant.of(2);

        Term actual = compile.conditional(condition, first, second);

        verify(condition, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        verify(condition).evaluate(any(SymbolsTable.class));

        assertNumber(2, value);
    }
    
    /**
     * Unit test {@link LambdaCompiler#conditional(Term, Term, Term)}
     */
    @Test
    public void test_conditional_true_constants() {
        Term condition = spy(Constant.of(true));
        Term first = Constant.of("ab");
        Term second = Constant.of("cd");
        Term actual = compile.conditional(condition, first, second);

        verify(condition).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        verify(condition).evaluate(any(SymbolsTable.class));

        assertText("ab", value);
    }

    /**
     * Unit test {@link LambdaCompiler#conditional(Term, Term, Term)}
     */
    @Test
    public void test_conditional_false_constants() {
        Term condition = spy(Constant.of(false));
        Term first = Constant.of(1);
        Term second = Constant.of(2);
        Term actual = compile.conditional(condition, first, second);

        verify(condition).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        verify(condition).evaluate(any(SymbolsTable.class));

        assertNumber(2, value);
    }


                //*** Relational Ops ***//

    /**
     * Unit test {@link LambdaCompiler#equal(Term, Term)}
     */
    @Test
    public void test_equal_text() {
        equalHelper("Equal Strings", Constant.of("ab"), Constant.of("ab"), true);
        equalHelper("Different Strings", Constant.of("ab"), Constant.of("cd"), false);
        equalHelper("String 0 to Number 0", Constant.of("0"), Constant.of(0), true);
        equalHelper("String 0 to Number 1", Constant.of("0"), Constant.of(1), false);
        equalHelper("String false to Logic false", Constant.of("false"), Constant.of(false), true);
        equalHelper("String other to Logic false", Constant.of("other"), Constant.of(false), false);
        equalHelper("String other to Date 0", Constant.of("other"), Constant.of(START), false);
        equalHelper("String 1970 to Date 0", Constant.of("1970-01-01T00:00:00Z"), Constant.of(START), true);
    }


    /**
     * Unit test {@link LambdaCompiler#equal(Term, Term)}
     */
    @Test
    public void test_equal_number() {
        equalHelper("Equal Numbers", Constant.of(15), Constant.of(15), true);
        equalHelper("Different Numbers", Constant.of(14), Constant.of(5), false);
        equalHelper("Number 0 to String 0", Constant.of(0), Constant.of("0"), true);
        equalHelper("Number 1 to String other", Constant.of(1), Constant.of("other"), false);
        equalHelper("Number 0 to Logic false", Constant.of(0), Constant.of(false), true);
        equalHelper("Number 0 to Logic true", Constant.of(0), Constant.of(true), false);
        equalHelper("Number 0 to Date 0", Constant.of(0), Constant.of(START), true);
        equalHelper("Number 0 to Date 1", Constant.of(0), Constant.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Term, Term)}
     */
    @Test
    public void test_equal_logic() {
        equalHelper("Both True", Constant.of(true), Constant.of(true), true);
        equalHelper("Both True/False", Constant.of(true), Constant.of(false), false);
        equalHelper("Both False/True", Constant.of(false), Constant.of(true), false);
        equalHelper("Both False/False", Constant.of(false), Constant.of(false), true);
        equalHelper("Logic false to String false", Constant.of(false), Constant.of("false"), true);
        equalHelper("Logic false to String other", Constant.of(false), Constant.of("other"), false);
        equalHelper("Logic false to Number 0", Constant.of(false), Constant.of(0), true);
        equalHelper("Logic false to Number 123", Constant.of(false), Constant.of(123), false);
        equalHelper("Logic false to Date 0", Constant.of(false), Constant.of(START), true);
        equalHelper("Logic false to Date 1", Constant.of(false), Constant.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Term, Term)}
     */
    @Test
    public void test_equal_date() {
        LocalDateTime local = LocalDateTime.of(2000, 1, 2, 12, 30, 45);

        ZonedDateTime date1 = ZonedDateTime.of(local, ZoneId.of("-5"));
        ZonedDateTime date2 = date1.withZoneSameInstant(ZoneId.of("-6")); // Same instant as date1
        ZonedDateTime date3 = date2.plusHours(1);                         // Same clock time as date1, but different zone

        equalHelper("Date 0 to String 1970", Constant.of(START), Constant.of("1970-01-01T00:00:00Z"), true);
        equalHelper("Date 0 to String 0", Constant.of(START), Constant.of("0"), false);
        equalHelper("Date 0 to String false", Constant.of(START), Constant.of("false"), false);
        equalHelper("Date 0 to String other", Constant.of(START), Constant.of("other"), false);
        equalHelper("Date 0 to Number 0", Constant.of(START), Constant.of(0), true);
        equalHelper("Date 0 to Number 123", Constant.of(START), Constant.of(123), false);
        equalHelper("Date 0 to Logic false", Constant.of(START), Constant.of(false), true);
        equalHelper("Date 0 to Logic true", Constant.of(START), Constant.of(true), false);
        equalHelper("Date 0 to Date 0", Constant.of(START), Constant.of(START), true);
        equalHelper("Date 0 to Date 1", Constant.of(START), Constant.of(ONE_SECOND), false);

        equalHelper("date 1 to date2", Constant.of(date1), Constant.of(date2), true);
        equalHelper("date 1 to date3", Constant.of(date1), Constant.of(date3), false);
    }

    private void equalHelper(@Nonnull String message,
                             @Nonnull Constant left,
                             @Nonnull Constant right,
                             boolean expected) {
        equalHelper(message, spy(left), spy(right), times(1), expected);
        equalHelper(message, mockValue(left), mockValue(right), never(), expected);
    }

    private void equalHelper(@Nonnull String message,
                             @Nonnull Term left,
                             @Nonnull Term right,
                             @Nonnull VerificationMode times,
                             boolean expected) {
        Term actual = compile.equal(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }


    /**
     * Unit test {@link LambdaCompiler#notEqual(Term, Term)}
     */
    @Test
    public void test_notEqual_text() {
        notEqualHelper("Equal Strings", Constant.of("ab"), Constant.of("ab"), false);
        notEqualHelper("Different Strings", Constant.of("ab"), Constant.of("cd"), true);
        notEqualHelper("String 0 to Number 0", Constant.of("0"), Constant.of(0), false);
        notEqualHelper("String 0 to Number 1", Constant.of("0"), Constant.of(1), true);
        notEqualHelper("String false to Logic false", Constant.of("false"), Constant.of(false), false);
        notEqualHelper("String other to Logic false", Constant.of("other"), Constant.of(false), true);
        notEqualHelper("String other to Date", Constant.of("other"), Constant.of(START), true);
        notEqualHelper("String 1970 to Date", Constant.of("1970-01-01T00:00:00Z"), Constant.of(START), false);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Term, Term)}
     */
    @Test
    public void test_notEqual_number() {
        notEqualHelper("Equal Numbers", Constant.of(15), Constant.of(15), false);
        notEqualHelper("Different Numbers", Constant.of(14), Constant.of(5), true);
        notEqualHelper("Number 0 to String 0", Constant.of(0), Constant.of("0"), false);
        notEqualHelper("Number 1 to String other", Constant.of(1), Constant.of("other"), true);
        notEqualHelper("Number 0 to Logic false", Constant.of(0), Constant.of(false), false);
        notEqualHelper("Number 0 to Logic true", Constant.of(0), Constant.of(true), true);
        notEqualHelper("Number 0 to Date 0", Constant.of(0), Constant.of(START), false);
        notEqualHelper("Number 0 to Date 1", Constant.of(0), Constant.of(ONE_SECOND), true);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Term, Term)}
     */
    @Test
    public void test_notEqual_logic() {
        notEqualHelper("Both True", Constant.of(true), Constant.of(true), false);
        notEqualHelper("Both True/False", Constant.of(true), Constant.of(false), true);
        notEqualHelper("Both False/True", Constant.of(false), Constant.of(true), true);
        notEqualHelper("Both False/False", Constant.of(false), Constant.of(false), false);
        notEqualHelper("Logic false to String false", Constant.of(false), Constant.of("false"), false);
        notEqualHelper("Logic false to String other", Constant.of(false), Constant.of("other"), true);
        notEqualHelper("Logic false to Number 0", Constant.of(false), Constant.of(0), false);
        notEqualHelper("Logic false to Number 123", Constant.of(false), Constant.of(123), true);
        notEqualHelper("Logic false to Date 0", Constant.of(false), Constant.of(START), false);
        notEqualHelper("Logic false to Date 1", Constant.of(false), Constant.of(ONE_SECOND), true);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Term, Term)}
     */
    @Test
    public void test_notEqual_date() {
        notEqualHelper("Date 0 to String 1970", Constant.of(START), Constant.of("1970-01-01T00:00:00Z"), false);
        notEqualHelper("Date 0 to String 0", Constant.of(START), Constant.of("0"), true);
        notEqualHelper("Date 0 to String false", Constant.of(START), Constant.of("false"), true);
        notEqualHelper("Date 0 to String other", Constant.of(START), Constant.of("other"), true);
        notEqualHelper("Date 0 to Number 0", Constant.of(START), Constant.of(0), false);
        notEqualHelper("Date 0 to Number 123", Constant.of(START), Constant.of(123), true);
        notEqualHelper("Date 0 to Logic false", Constant.of(START), Constant.of(false), false);
        notEqualHelper("Date 0 to Logic true", Constant.of(START), Constant.of(true), true);
        notEqualHelper("Date 0 to Date 0", Constant.of(START), Constant.of(START), false);
        notEqualHelper("Date 0 to Date 1", Constant.of(START), Constant.of(ONE_SECOND), true);
    }

    private void notEqualHelper(@Nonnull String message, @Nonnull Constant left, @Nonnull Constant right, boolean expected) {
        notEqualHelper(message, spy(left), spy(right), times(1), expected);
        notEqualHelper(message, mockValue(left), mockValue(right), never(), expected);
    }

    private void notEqualHelper(@Nonnull String message,
                                @Nonnull Term left,
                                @Nonnull Term right,
                                @Nonnull VerificationMode times,
                                boolean expected) {
        Term actual = compile.notEqual(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }

    /**
     * Unit test {@link LambdaCompiler#greaterThan(Term, Term)}
     */
    @Test
    public void test_greaterThan_variables() {
        Term left = mockValue(Value.of(14));
        Term right = mockValue(Value.of(55));

        Term actual = compile.greaterThan(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#greaterThan(Term, Term)}
     */
    @Test
    public void test_greaterThan_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.greaterThan(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#greaterThenEquals(Term, Term)}
     */
    @Test
    public void test_greaterThanEqual_variables() {
        Term left = mockValue(Value.of(14));
        Term right = mockValue(Value.of(5));

        Term actual = compile.greaterThenEquals(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#greaterThenEquals(Term, Term)}
     */
    @Test
    public void test_greaterThanEqual_constants() {
        Term left = spy(Constant.of(14));
        Term right =  spy(Constant.of(5));

        Term actual = compile.greaterThenEquals(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#lessThan(Term, Term)}
     */
    @Test
    public void test_lessThan_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.lessThan(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#lessThan(Term, Term)}
     */
    @Test
    public void test_lessThan_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.lessThan(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#lessThanEquals(Term, Term)}
     */
    @Test
    public void test_lessThanEqual_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.lessThanEquals(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#lessThanEquals(Term, Term)}
     */
    @Test
    public void test_lessThanEqual_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.lessThanEquals(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }


    /**
     * Unit test {@link LambdaCompiler#isBefore(Term, Term)}
     */
    @Test
    public void test_isBefore_variables() {
        isBefore_Helper("left before right", mockValue(Constant.of(500)), mockValue(Constant.of(9000)), never(), true);
        isBefore_Helper("left equals right", mockValue(Constant.of(500)), mockValue(Constant.of(500)), never(), false);
        isBefore_Helper("left after right", mockValue(Constant.of(9000)), mockValue(Constant.of(500)), never(), false);
    }

    /**
     * Unit test {@link LambdaCompiler#isBefore(Term, Term)}
     */
    @Test
    public void test_isBefore_constants() {
        isBefore_Helper("left before right", spy(Constant.of(500)), spy(Constant.of(9000)), times(1), true);
        isBefore_Helper("left equals right", spy(Constant.of(500)), spy(Constant.of(500)), times(1), false);
        isBefore_Helper("left after right", spy(Constant.of(9000)), spy(Constant.of(500)), times(1), false);
    }

    private void isBefore_Helper(@Nonnull String message,
                                 @Nonnull Term left,
                                 @Nonnull Term right,
                                 @Nonnull VerificationMode times,
                                 boolean expected) {
        Term actual = compile.isBefore(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }

    /**
     * Unit test {@link LambdaCompiler#isAfter(Term, Term)}
     */
    @Test
    public void test_isAfter_variables() {
        isAfter_Helper("left before right", spy(Constant.of(500)), spy(Constant.of(9000)), times(1), false);
        isAfter_Helper("left equals right", spy(Constant.of(500)), spy(Constant.of(500)), times(1), false);
        isAfter_Helper("left after right", spy(Constant.of(9000)), spy(Constant.of(500)), times(1), true);
    }

    /**
     * Unit test {@link LambdaCompiler#isAfter(Term, Term)}
     */
    @Test
    public void test_isAfter_constants() {
        isAfter_Helper("left before right", mockValue(Constant.of(500)), mockValue(Constant.of(9000)), never(), false);
        isAfter_Helper("left equals right", mockValue(Constant.of(500)), mockValue(Constant.of(500)), never(), false);
        isAfter_Helper("left after right", mockValue(Constant.of(9000)), mockValue(Constant.of(500)), never(), true);
    }

    private void isAfter_Helper(@Nonnull String message,
                                @Nonnull Term first,
                                @Nonnull Term second,
                                @Nonnull VerificationMode times,
                                boolean expected) {
        Term actual = compile.isAfter(first, second);

        verify(first, times).evaluate(any(SymbolsTable.class));
        verify(second, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }


    /**
     * Unit test {@link LambdaCompiler#in(Term, List)}
     */
    @Test
    public void test_in() {
        in_Helper("500 in [ 9000 ]", mockValue(Constant.of(500)), mockValue(Constant.of(9000)), never(), false);
        in_Helper("500 in [ 500 ]", mockValue(Constant.of(500)), mockValue(Constant.of(500)), never(), true);
    }

    private void in_Helper(@Nonnull String message,
                           @Nonnull Term first,
                           @Nonnull Term second,
                           @Nonnull VerificationMode times,
                           boolean expected) {
        Term actual = compile.in(first, List.of(second));

        verify(first, times).evaluate(any(SymbolsTable.class));
        verify(second, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }


                 //*** Numeric Ops ***//

    /**
     * Unit test {@link LambdaCompiler#negate(Term)}
     */
    @Test
    public void test_negate_variables() {
        Term operand = mockValue(Constant.of(1));

        Term actual = compile.negate(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(new BigDecimal("-1"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#negate(Term)}
     */
    @Test
    public void test_negate_constants() {
        Term operand = spy(Constant.of(1));

        Term actual = compile.negate(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(new BigDecimal("-1"), value);
    }


    /**
     * Unit test {@link LambdaCompiler#add(Term, Term)}
     */
    @Test
    public void test_add_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.add(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(19, value);
    }

    /**
     * Unit test {@link LambdaCompiler#add(Term, Term)}
     */
    @Test
    public void test_add_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.add(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(19, value);
    }

    /**
     * Unit test {@link LambdaCompiler#add(Term, Term)}
     */
    @Test
    public void test_add_zero() {
        test_add_zero("14 + 0", 14, 0);
        test_add_zero("0 + 15", 0, 15);
    }

    private void test_add_zero(@Nonnull String message, int left, int right) {
        Term leftTerm = (left == 0 ? Constant.of(0) : mockValue(Constant.of(left)));
        Term rightTerm = (right == 0 ? Constant.of(0) : mockValue(Constant.of(right)));

        Term actual = compile.add(leftTerm, rightTerm);

        assertNumber(message, BigDecimal.valueOf(left + right), actual.evaluate(symbols));
        assertTrue(message + ": Unexpected term returned", (rightTerm == actual) || (leftTerm == actual));
    }

    /**
     * Unit test {@link LambdaCompiler#subtract(Term, Term)}
     */
    @Test
    public void test_subtract_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.subtract(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(9, value);
    }

    /**
     * Unit test {@link LambdaCompiler#subtract(Term, Term)}
     */
    @Test
    public void test_subtract_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.subtract(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(9, value);
    }

    /**
     * Unit test {@link LambdaCompiler#subtract(Term, Term)}
     */
    @Test
    public void test_subtract_zero() {
        Term left = mockValue(Constant.of(16));
        Term right = Constant.of(0);

        Term actual = compile.subtract(left, right);

        assertNumber((16 - 0), actual.evaluate(symbols));
        assertSame("Unexpected term returned", left, actual);
    }

    /**
     * Unit test {@link LambdaCompiler#multiply(Term, Term)}
     */
    @Test
    public void test_multiply_variables() {
        Term left = mockValue(Constant.of(3));
        Term right = mockValue(Constant.of(4));

        Term actual = compile.multiply(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(12, value);
    }

    /**
     * Unit test {@link LambdaCompiler#multiply(Term, Term)}
     */
    @Test
    public void test_multiply_constants() {
        Term left = spy(Constant.of(3));
        Term right = spy(Constant.of(4));

        Term actual = compile.multiply(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(12, value);
    }

    /**
     * Unit test {@link LambdaCompiler#multiply(Term, Term)}
     */
    @Test
    public void test_multiply_one() {
        test_multiply_one("14 * 1", 14, 1);
        test_multiply_one("1 * 15", 1, 15);
    }

    private void test_multiply_one(@Nonnull String message, long left, long right) {
        Term leftTerm = (left == 1 ? Constant.of(1) : mockValue(Constant.of(left)));
        Term rightTerm = (right == 1 ? Constant.of(1) : mockValue(Constant.of(right)));

        Term actual = compile.multiply(leftTerm, rightTerm);

        assertNumber(message, BigDecimal.valueOf(left * right), actual.evaluate(symbols));
        assertTrue(message + ": Unexpected term returned", (rightTerm == actual) || (leftTerm == actual));
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Term, Term)}
     */
    @Test
    public void test_divide_byZero() {
        Term left = Constant.of(15);
        Term right = Constant.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divide(left, right).evaluate(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Term, Term)}
     */
    @Test
    public void test_divide_variables() {
        Term left = mockValue(Constant.of(15));
        Term right = mockValue(Constant.of(4));

        Term actual = compile.divide(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(new BigDecimal("3.75"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Term, Term)}
     */
    @Test
    public void test_divide_constants() {
        Term left = spy(Constant.of(15));
        Term right = spy(Constant.of(4));

        Term actual = compile.divide(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(new BigDecimal("3.75"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Term, Term)}
     */
    @Test
    public void test_divide_one() {
        Term left = mockValue(Constant.of(15));
        Term right = Constant.of(1);

        Term actual = compile.divide(left, right);

        assertNumber(15, actual.evaluate(symbols));
        assertSame("Unexpected term returned", left, actual);
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Term, Term)}
     */
    @Test
    public void test_divideFloor_byZero() {
        Term left = Constant.of(15);
        Term right = Constant.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divideFloor(left, right).evaluate(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Term, Term)}
     */
    @Test
    public void test_divideFloor_variables() {
        divideFloor_Helper("positive", mockValue(Constant.of(15)), mockValue(Constant.of(4)), never(), new BigDecimal("3"));
        divideFloor_Helper("negative", mockValue(Constant.of(-15)), mockValue(Constant.of(4)), never(), new BigDecimal("-4"));
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Term, Term)}
     */
    @Test
    public void test_divideFloor_constants() {
        divideFloor_Helper("positive", spy(Constant.of(15)), spy(Constant.of(4)), atLeastOnce(), new BigDecimal("3"));
        divideFloor_Helper("negative", spy(Constant.of(-15)), spy(Constant.of(4)), atLeastOnce(), new BigDecimal("-4"));
    }

    private void divideFloor_Helper(@Nonnull String message,
                                    @Nonnull Term left,
                                    @Nonnull Term right,
                                    @Nonnull VerificationMode times,
                                    @Nonnull BigDecimal expected) {
        Term actual = compile.divideFloor(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(message, expected, value);
    }

    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Term, Term)}
     */
    @Test
    public void test_divideTruncate_byZero() {
        Term left = Constant.of(15);
        Term right = Constant.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divideTruncate(left, right).evaluate(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Term, Term)}
     */
    @Test
    public void test_divideTruncate_variables() {
        divideTruncate_Helper("positive", mockValue(Constant.of(15)), mockValue(Constant.of(4)), never(), new BigDecimal("3"));
        divideTruncate_Helper("negative", mockValue(Constant.of(-15)), mockValue(Constant.of(4)), never(), new BigDecimal("-3"));
    }

    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Term, Term)}
     */
    @Test
    public void test_divideTruncate_constants() {
        divideTruncate_Helper("positive", spy(Constant.of(15)), spy(Constant.of(4)), atLeastOnce(), new BigDecimal("3"));
        divideTruncate_Helper("negative", spy(Constant.of(-15)), spy(Constant.of(4)), atLeastOnce(), new BigDecimal("-3"));
    }

    private void divideTruncate_Helper(@Nonnull String message,
                                       @Nonnull Term left,
                                       @Nonnull Term right,
                                       @Nonnull VerificationMode times,
                                       @Nonnull BigDecimal expected) {
        Term actual = compile.divideTruncate(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(message, expected, value);
    }


    /**
     * Unit test {@link LambdaCompiler#modulus(Term, Term)}
     */
    @Test
    public void test_modulusByZero() {
        Term left = Constant.of(15);
        Term right = Constant.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.modulus(left, right).evaluate(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#modulus(Term, Term)}
     */
    @Test
    public void test_modulus_variables() {
        Term left = mockValue(Constant.of(15));
        Term right = mockValue(Constant.of(4));

        Term actual = compile.modulus(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(3, value);
    }

    /**
     * Unit test {@link LambdaCompiler#modulus(Term, Term)}
     */
    @Test
    public void test_modulus_constants() {
        Term left = spy(Constant.of(15));
        Term right = spy(Constant.of(4));

        Term actual = compile.modulus(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(3, value);
    }

    /**
     * Unit test {@link LambdaCompiler#power(Term, Term)}
     */
    @Test
    public void test_power_variables() {
        Term left = mockValue(Constant.of(3));
        Term right = mockValue(Constant.of(4));

        Term actual = compile.power(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(81, value);
    }

    /**
     * Unit test {@link LambdaCompiler#power(Term, Term)}
     */
    @Test
    public void test_power_constants() {
        Term left = spy(Constant.of(3));
        Term right = spy(Constant.of(4));

        Term actual = compile.power(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(81, value);
    }

    /**
     * Unit test {@link LambdaCompiler#power(Term, Term)}
     */
    @Test
    public void test_power_one() {
        Term left = mockValue(Constant.of(3));
        Term right = Constant.of(1);

        Term actual = compile.power(left, right);

        assertNumber(3, actual.evaluate(symbols));
        assertSame("Unexpected term returned", left, actual);
    }

                //*** Logic Ops ***//

    /**
     * Unit test {@link LambdaCompiler#logicalNot(Term)}
     */
    @Test
    public void test_logicalNot_variables() {
        Term operand = mockValue(Constant.of(true));

        Term actual = compile.logicalNot(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalNot(Term)}
     */
    @Test
    public void test_logicalNot_constants() {
        Term operand = spy(Constant.of(true));

        Term actual = compile.logicalNot(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Term, Term)}
     */
    @Test
    public void test_logicalAnd_variables() {
        Term left = mockValue(Constant.of(false));
        Term right = mockValue(Constant.of(true));

        Term actual = compile.logicalAnd(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(false, value);

        // Check short circuit
        verify(left).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Term, Term)}
     */
    @Test
    public void test_logicalAnd_constants() {
        Term left = spy(Constant.of(true));
        Term right = spy(Constant.of(true));

        Term actual = compile.logicalAnd(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Term, Term)}
     */
    @Test
    public void test_logicalAnd_false() {
        test_logicalAnd_false("false and true", true, false, true);
        test_logicalAnd_false("true and false", false, true, false);
    }

    private void test_logicalAnd_false(@Nonnull String message, boolean optimised, boolean left, boolean right) {
        Term leftTerm = (!left ? Constant.of(false) : mockValue(Constant.of(true)));
        Term rightTerm = (!right ? Constant.of(false) : mockValue(Constant.of(true)));

        Term actual = compile.logicalAnd(leftTerm, rightTerm);

        assertLogic(message, (left && right), actual.evaluate(symbols));
        boolean sameTerm = (rightTerm == actual) || (leftTerm == actual);

        assertEquals(message + ": optimised failed", optimised, sameTerm);
    }


    /**
     * Unit test {@link LambdaCompiler#logicalOr(Term, Term)}
     */
    @Test
    public void test_logicalOr_variables() {
        Term left = mockValue(Constant.of(true));
        Term right = mockValue(Constant.of(false));

        Term actual = compile.logicalOr(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);

        // Check short circuit
        verify(left).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));
    }

    /**
     * Unit test {@link LambdaCompiler#logicalOr(Term, Term)}
     */
    @Test
    public void test_logicalOr_constants() {
        Term left = spy(Constant.of(false));
        Term right = spy(Constant.of(true));

        Term actual = compile.logicalOr(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }
    /**
     * Unit test {@link LambdaCompiler#logicalOr(Term, Term)}
     */
    @Test
    public void test_logicalAnd_true() {
        test_logicalOr_true("false and true", false, false, true);
        test_logicalOr_true("true and false", true, true, false);
    }

    private void test_logicalOr_true(@Nonnull String message, boolean optimised, boolean left, boolean right) {
        Term leftTerm = (!left ? mockValue(Constant.of(false)) : Constant.of(true));
        Term rightTerm = (!right ? mockValue(Constant.of(false)) : Constant.of(true));

        Term actual = compile.logicalOr(leftTerm, rightTerm);

        assertLogic(message, (left || right), actual.evaluate(symbols));
        boolean sameTerm = (rightTerm == actual) || (leftTerm == actual);

       assertEquals(message + ": optimised failed", optimised, sameTerm);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalXor(Term, Term)}.
     * This cannot be short-circuited.
     */
    @Test
    public void test_logicalXor_variables() {
        xorHelper("false xor false", mockValue(Constant.of(false)), mockValue(Constant.of(false)), never(), false);
        xorHelper("false xor true", mockValue(Constant.of(false)), mockValue(Constant.of(true)), never(), true);
        xorHelper("true xor false", mockValue(Constant.of(true)), mockValue(Constant.of(false)), never(), true);
        xorHelper("true xor true", mockValue(Constant.of(true)), mockValue(Constant.of(true)), never(), false);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalXor(Term, Term)}.
     * This cannot be short-circuited.
     */
    @Test
    public void test_logicalXor_constants() {
        xorHelper("false xor false", spy(Constant.of(false)), spy(Constant.of(false)), times(1), false);
        xorHelper("false xor true", spy(Constant.of(false)), spy(Constant.of(true)), times(1), true);
        xorHelper("true xor false", spy(Constant.of(true)), spy(Constant.of(false)), times(1), true);
        xorHelper("true xor true", spy(Constant.of(true)), spy(Constant.of(true)), times(1), false);
    }

    private void xorHelper(@Nonnull String message,
                           @Nonnull Term left,
                           @Nonnull Term right,
                           @Nonnull VerificationMode times,
                           boolean expected) {
        Term actual = compile.logicalXor(left, right);

        verify(left, times).evaluate(any(SymbolsTable.class));
        verify(right, times).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(message, expected, value);
    }


                //*** Bitwise Ops ***//

    /**
     * Unit test {@link LambdaCompiler#bitwiseNot(Term)}
     */
    @Test
    public void test_bitwiseNot_variables() {
        Term operand = mockValue(Constant.of(0xa5));

        Term actual = compile.bitwiseNot(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber((byte)0x5a, value);
    }
    /**
     * Unit test {@link LambdaCompiler#bitwiseNot(Term)}
     */
    @Test
    public void test_bitwiseNot_constants() {
        Term operand = spy(Constant.of(0xa5));

        Term actual = compile.bitwiseNot(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber((byte)0x5a, value);
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseAnd(Term, Term)}
     */
    @Test
    public void test_bitwiseAnd_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.bitwiseAnd(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(4, value);
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseAnd(Term, Term)}
     */
    @Test
    public void test_bitwiseAnd_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.bitwiseAnd(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(4, value);
    }


    /**
     * Unit test {@link LambdaCompiler#bitwiseOr(Term, Term)}
     */
    @Test
    public void test_bitwiseOr_variables() {
        Term left = mockValue(Constant.of(12));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.bitwiseOr(left,right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(13, value);
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseOr(Term, Term)}
     */
    @Test
    public void test_bitwiseOr_constants() {
        Term left = spy(Constant.of(12));
        Term right = spy(Constant.of(5));

        Term actual = compile.bitwiseOr(left,right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(13, value);
    }


    /**
     * Unit test {@link LambdaCompiler#bitwiseXor(Term, Term)}
     */
    @Test
    public void test_bitwiseXor_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.bitwiseXor(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(11, value);
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseXor(Term, Term)}
     */
    @Test
    public void test_bitwiseXor_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.bitwiseXor(left, right);

        verify(left).evaluate(any(SymbolsTable.class));
        verify(right).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(11, value);
    }

                //*** Shift Ops ***//

    /**
     * Unit test {@link LambdaCompiler#leftShift(Term, Term)}
     */
    @Test
    public void test_leftShift_variables() {
        Term left = mockValue(Constant.of(14));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.leftShift(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(448, value);
    }

    /**
     * Unit test {@link LambdaCompiler#leftShift(Term, Term)}
     */
    @Test
    public void test_leftShift_constants() {
        Term left = spy(Constant.of(14));
        Term right = spy(Constant.of(5));

        Term actual = compile.leftShift(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(448, value);
    }

    /**
     * Unit test {@link LambdaCompiler#leftShift(Term, Term)}
     */
    @Test
    public void test_leftShift_zero() {
        Term left = mockValue(Constant.of(14));
        Term right = Constant.of(0);

        Term actual = compile.leftShift(left, right);

        assertNumber((14 << 0), actual.evaluate(symbols));
        assertSame("Unexpected term returned", left, actual);
    }

    /**
     * Unit test {@link LambdaCompiler#rightShift(Term, Term)}
     */
    @Test
    public void test_rightShift_variables() {
        Term left = mockValue(Constant.of(448));
        Term right = mockValue(Constant.of(5));

        Term actual = compile.rightShift(left, right);

        verify(left, never()).evaluate(any(SymbolsTable.class));
        verify(right, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(14, value);
    }

    /**
     * Unit test {@link LambdaCompiler#rightShift(Term, Term)}
     */
    @Test
    public void test_rightShift_constants() {
        Term left = spy(Constant.of(448));
        Term right = spy(Constant.of(5));

        Term actual = compile.rightShift(left, right);

        verify(left, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(right, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(14, value);
    }

    /**
     * Unit test {@link LambdaCompiler#rightShift(Term, Term)}
     */
    @Test
    public void test_rightShift_zero() {
        Term left = mockValue(Constant.of(14));
        Term right = Constant.of(0);

        Term actual = compile.rightShift(left, right);

        assertNumber((14 >> 0), actual.evaluate(symbols));
        assertSame("Unexpected term returned", left, actual);
    }

                //*** Text Ops ***//

    /**
     * Unit test {@link LambdaCompiler#concatenate(Term, Term)}
     */
    @Test
    public void test_concatenate_variables() {
        Term first = mockValue(Constant.of("Hello "));
        Term second = mockValue(Constant.of("World"));

        Term actual = compile.concatenate(first, second);

        verify(first, never()).evaluate(any(SymbolsTable.class));
        verify(second, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertText("Hello World", value);
    }

    /**
     * Unit test {@link LambdaCompiler#concatenate(Term, Term)}
     */
    @Test
    public void test_concatenate_constants() {
        Term first = spy(Constant.of("Hello "));
        Term second = spy(Constant.of("World"));

        Term actual = compile.concatenate(first, second);

        verify(first, atLeastOnce()).evaluate(any(SymbolsTable.class));
        verify(second, atLeastOnce()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertText("Hello World", value);
    }

    /**
     * Unit test {@link LambdaCompiler#concatenate(Term, Term)}
     */
    @Test
    public void test_concatenate_empty() {
        test_concatenate_empty("'' ~> 'abc'", "", "abc");
        test_concatenate_empty("'abc' ~> ''", "abc", "");
    }

    private void test_concatenate_empty(@Nonnull String message, @Nonnull String left, @Nonnull String right) {
        Term leftTerm = (left.isEmpty() ? Constant.of("") : mockValue(Constant.of(left)));
        Term rightTerm = (right.isEmpty() ? Constant.of("") : mockValue(Constant.of(right)));

        Term actual = compile.concatenate(leftTerm, rightTerm);

        assertText(message, (left + right), actual.evaluate(symbols));
        assertTrue(message + ": Unexpected term returned", (rightTerm == actual) || (leftTerm == actual));
    }


    /**
     * Unit test {@link LambdaCompiler#callText(Term)} 
     */
    @Test
    public void test_callText_variables() {
        Term operand = mockValue(Constant.of(true));

        Term actual = compile.callText(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertText("true", value);
    }

    /**
     * Unit test {@link LambdaCompiler#callText(Term)}
     */
    @Test
    public void test_callText_constants() {
        Term operand = spy(Constant.of(true));

        Term actual = compile.callText(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertText("true", value);
    }


    /**
     * Unit test {@link LambdaCompiler#callNumber(Term)}
     */
    @Test
    public void test_callNumber_variables() {
        Term operand = mockValue(Constant.of("123"));

        Term actual = compile.callNumber(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(123, value);
    }

    /**
     * Unit test {@link LambdaCompiler#callNumber(Term)}
     */
    @Test
    public void test_callNumber_constants() {
        Term operand = spy(Constant.of("123"));

        Term actual = compile.callNumber(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertNumber(123, value);
    }

    /**
     * Unit test {@link LambdaCompiler#callNumber(Term)}
     */
    @Test
    public void test_callNumber_invalid() {
        Term operand = spy(Constant.of("not a number"));

        Term actual = compile.callNumber(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Assert.assertThrows(EelConvertException.class, () -> actual.evaluate(symbols));
    }


    /**
     * Unit test {@link LambdaCompiler#callLogic(Term)}
     */
    @Test
    public void test_callLogic_variables() {
        Term operand = mockValue(Constant.of("true"));

        Term actual = compile.callLogic(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#callLogic(Term)}
     */
    @Test
    public void test_callLogic_constants() {
        Term operand = spy(Constant.of("true"));

        Term actual = compile.callLogic(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#callLogic(Term)}
     */
    @Test
    public void test_callLogic_invalid() {
        Term operand = spy(Constant.of("not a logic value"));

        Term actual = compile.callLogic(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Assert.assertThrows(EelConvertException.class, () -> actual.evaluate(symbols));
    }


    /**
     * Unit test {@link LambdaCompiler#callDate(Term)}
     */
    @Test
    public void test_callDate_variables() {
        Term operand = mockValue(Constant.of(1234));

        Term actual = compile.callDate(operand);

        verify(operand, never()).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertDate(ZonedDateTime.of(1970, 1, 1, 0, 20, 34, 0, ZoneOffset.UTC), value);
    }

    /**
     * Unit test {@link LambdaCompiler#callDate(Term)}
     */
    @Test
    public void test_callDate_constants() {
        Term operand = spy(Constant.of(1234));

        Term actual = compile.callDate(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Value value = actual.evaluate(symbols);

        assertDate(ZonedDateTime.of(1970, 1, 1, 0, 20, 34, 0, ZoneOffset.UTC), value);
    }

    /**
     * Unit test {@link LambdaCompiler#callDate(Term)}
     */
    @Test
    public void test_callDate_invalid() {
        Term operand = spy(Constant.of("not a date"));

        Term actual = compile.callDate(operand);

        verify(operand).evaluate(any(SymbolsTable.class));

        Assert.assertThrows(EelConvertException.class, () -> actual.evaluate(symbols));
    }


                //*** Helper methods ***//

    private void assertText(@Nonnull String expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asText());
    }

    private void assertText(@Nonnull String message, @Nonnull String expected, @Nonnull Value value) {
        Assert.assertEquals(message + "Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals(message + "Unexpected value", expected, value.asText());
    }

    private void assertNumber(byte expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asNumber().byteValue());
    }

    private void assertNumber(int expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asInt());
    }

    private void assertNumber(@Nonnull BigDecimal expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asNumber());
    }

    private void assertNumber(@Nonnull String message, @Nonnull BigDecimal expected, @Nonnull Value value) {
        Assert.assertEquals(message + ": Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals(message + ": Unexpected value", expected, value.asNumber());
    }

    private void assertLogic(boolean expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asLogic());
    }

    private void assertLogic(@Nonnull String message, boolean expected, @Nonnull Value value) {
        Assert.assertEquals(message + ": Unexpected type", Type.LOGIC, value.getType());
        Assert.assertEquals(message + ": Unexpected value", expected, value.asLogic());
    }

    private void assertDate(@Nonnull ZonedDateTime expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.DATE, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asDate());
    }
}