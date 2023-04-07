package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import com.github.tymefly.eel.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
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
        EelContext context = mock(EelContext.class);

        symbols = mock(SymbolsTable.class);

        when(context.getMathContext())
            .thenReturn(new MathContext(3, RoundingMode.HALF_UP));

        when(symbols.read("key"))
            .thenReturn("This Value!");

        compile = new LambdaCompiler(context);
    }

                //*** Variables ***//

    /**
     * Unit test {@link LambdaCompiler#isDefined(String)}
     */
    @Test
    public void test_isDefined_defined() {
        Executor actual = compile.isDefined("key");
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#isDefined(String)}
     */
    @Test
    public void test_isDefined_undefined() {
        Executor actual = compile.isDefined("bad.key");
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
    }


    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_withoutCaseOp() {
        Executor actual = compile.readVariable("key", null, null, false);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "This Value!", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_withCaseOp() {
        Executor actual = compile.readVariable("key", null, StringUtils::toggleAll, false);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "tHIS vALUE!", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_defaultIgnored() {
        Executor defaultValue = s -> Value.of("Hello");

        Executor actual = compile.readVariable("key", defaultValue, null, false);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "This Value!", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_defaultString() {
        Executor defaultValue = s -> Value.of("Hello");

        Executor actual = compile.readVariable("other", defaultValue, StringUtils::toggleAll, false);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "Hello", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_length() {
        Executor defaultValue = s -> Value.of("Hello");

        Executor actual = compile.readVariable("key", defaultValue, null, true);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 11, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_defaultLength() {
        Executor defaultValue = s -> Value.of(95);

        Executor actual = compile.readVariable("other", defaultValue, null, true);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 95, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#readVariable(String, Executor, CompileVariableOp, boolean)}
     */
    @Test
    public void test_variable_BadName() {
        EelUnknownSymbolException actual = Assert.assertThrows(EelUnknownSymbolException.class,
            () -> compile.readVariable("other", null, null, false).execute(symbols));

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
        Executor actual = compile.textConstant("Hello World");
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "Hello World", value.asText());
        Assert.assertSame("Value was not cached", actual, compile.textConstant("Hello World"));
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_true() {
        Executor actual = compile.logicConstant(true);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
        Assert.assertSame("Value was not cached", actual, compile.logicConstant(true));
        Assert.assertNotSame("Unexpected executor", actual, compile.logicConstant(false));
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_false() {
        Executor actual = compile.logicConstant(false);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
        Assert.assertSame("Value was not cached", actual, compile.logicConstant(false));
        Assert.assertNotSame("Unexpected executor", actual, compile.logicConstant(true));
    }

    /**
     * Unit test {@link LambdaCompiler#numericConstant(Number)}
     */
    @Test
    public void test_numericConstant() {
        Executor actual = compile.numericConstant(123.456);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("123.456"), value.asNumber());
        Assert.assertSame("Value was not cached", actual, compile.numericConstant(123.456));
    }

    /**
     * Unit test {@link LambdaCompiler#pi()}
     */
    @Test
    public void test_pi() {
        Executor actual = compile.pi();
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("3.141592653589793"), value.asNumber());
        Assert.assertSame("Value was not cached", actual,compile.pi());
    }

    /**
     * Unit test {@link LambdaCompiler#e()}
     */
    @Test
    public void test_e() {
        Executor actual = compile.e();
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("2.718281828459045"), value.asNumber());
        Assert.assertSame("Value was not cached", actual,compile.e());
    }


                //*** ternary Op ***//

    /**
     * Unit test {@link LambdaCompiler#condition(Executor, Executor, Executor)}
     */
    @Test
    public void test_condition_true() {
        Executor condition = s -> Value.TRUE;
        Executor first = s -> Value.of("ab");
        Executor second = s -> Value.of("cd");

        Executor actual = compile.condition(condition, first, second);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "ab", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#condition(Executor, Executor, Executor)}
     */
    @Test
    public void test_condition_false() {
        Executor condition = s -> Value.FALSE;
        Executor first = s -> Value.of("ab");
        Executor second = s -> Value.of("cd");

        Executor actual = compile.condition(condition, first, second);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "cd", value.asText());
    }

                //*** Relational Ops ***//

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_text() {
        equalHelper("Equal Strings", Value.of("ab"), Value.of("ab"), true);
        equalHelper("Different Strings", Value.of("ab"), Value.of("cd"), false);
        equalHelper("String 0 to Number 0", Value.of("0"), Value.of(0), true);
        equalHelper("String 0 to Number 1", Value.of("0"), Value.of(1), false);
        equalHelper("String false to Logic false", Value.of("false"), Value.FALSE, true);
        equalHelper("String other to Logic false", Value.of("other"), Value.FALSE, false);
        equalHelper("String other to Date 0", Value.of("other"), Value.of(START), false);
        equalHelper("String 1970 to Date 0", Value.of("1970-01-01T00:00:00Z"), Value.of(START), true);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_number() {
        equalHelper("Equal Numbers", Value.of(15), Value.of(15), true);
        equalHelper("Different Numbers", Value.of(14), Value.of(5), false);
        equalHelper("Number 0 to String 0", Value.of(0), Value.of("0"), true);
        equalHelper("Number 1 to String other", Value.of(1), Value.of("other"), false);
        equalHelper("Number 0 to Logic false", Value.of(0), Value.FALSE, true);
        equalHelper("Number 0 to Logic true", Value.of(0), Value.TRUE, false);
        equalHelper("Number 0 to Date 0", Value.of(0), Value.of(START), true);
        equalHelper("Number 0 to Date 1", Value.of(0), Value.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_logic() {
        equalHelper("Both True", Value.TRUE, Value.TRUE, true);
        equalHelper("Both True/False", Value.TRUE, Value.FALSE, false);
        equalHelper("Both False/True", Value.FALSE, Value.TRUE, false);
        equalHelper("Both False/False", Value.FALSE, Value.FALSE, true);
        equalHelper("Logic false to String false", Value.FALSE, Value.of("false"), true);
        equalHelper("Logic false to String other", Value.FALSE, Value.of("other"), false);
        equalHelper("Logic false to Number 0", Value.FALSE, Value.of(0), true);
        equalHelper("Logic false to Number 123", Value.FALSE, Value.of(123), false);
        equalHelper("Logic false to Date 0", Value.FALSE, Value.of(START), true);
        equalHelper("Logic false to Date 1", Value.FALSE, Value.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_date() {
        equalHelper("Date 0 to String 1970", Value.of(START), Value.of("1970-01-01T00:00:00Z"), true);
        equalHelper("Date 0 to String 0", Value.of(START), Value.of("0"), false);
        equalHelper("Date 0 to String false", Value.of(START), Value.of("false"), false);
        equalHelper("Date 0 to String other", Value.of(START), Value.of("other"), false);
        equalHelper("Date 0 to Number 0", Value.of(START), Value.of(0), true);
        equalHelper("Date 0 to Number 123", Value.of(START), Value.of(123), false);
        equalHelper("Date 0 to Logic false", Value.of(START), Value.FALSE, true);
        equalHelper("Date 0 to Logic true", Value.of(START), Value.TRUE, false);
        equalHelper("Date 0 to Date 0", Value.of(START), Value.of(START), true);
        equalHelper("Date 0 to Date 1", Value.of(START), Value.of(ONE_SECOND), false);
    }

    private void equalHelper(@Nonnull String message, @Nonnull Value left, @Nonnull Value right, boolean expected) {
        Executor actual = compile.equal(s -> left, s -> right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type for " + message, Type.LOGIC, value.getType());
        Assert.assertEquals("Unexpected value for " + message, expected, value.asLogic());
    }


    /**
     * Unit test {@link LambdaCompiler#notEqual(Executor, Executor)}
     */
    @Test
    public void test_notEqual_text() {
        notEqualHelper("Equal Strings", Value.of("ab"), Value.of("ab"), false);
        notEqualHelper("Different Strings", Value.of("ab"), Value.of("cd"), true);
        notEqualHelper("String 0 to Number 0", Value.of("0"), Value.of(0), false);
        notEqualHelper("String 0 to Number 1", Value.of("0"), Value.of(1), true);
        notEqualHelper("String false to Logic false", Value.of("false"), Value.FALSE, false);
        notEqualHelper("String other to Logic false", Value.of("other"), Value.FALSE, true);
        notEqualHelper("String other to Date", Value.of("other"), Value.of(START), true);
        notEqualHelper("String 1970 to Date", Value.of("1970-01-01T00:00:00Z"), Value.of(START), false);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Executor, Executor)}
     */
    @Test
    public void test_notEqual_number() {
        notEqualHelper("Equal Numbers", Value.of(15), Value.of(15), false);
        notEqualHelper("Different Numbers", Value.of(14), Value.of(5), true);
        notEqualHelper("Number 0 to String 0", Value.of(0), Value.of("0"), false);
        notEqualHelper("Number 1 to String other", Value.of(1), Value.of("other"), true);
        notEqualHelper("Number 0 to Logic false", Value.of(0), Value.FALSE, false);
        notEqualHelper("Number 0 to Logic true", Value.of(0), Value.TRUE, true);
        notEqualHelper("Number 0 to Date 0", Value.of(0), Value.of(START), false);
        notEqualHelper("Number 0 to Date 1", Value.of(0), Value.of(ONE_SECOND), true);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Executor, Executor)}
     */
    @Test
    public void test_notEqual_logic() {
        notEqualHelper("Both True", Value.TRUE, Value.TRUE, false);
        notEqualHelper("Both True/False", Value.TRUE, Value.FALSE, true);
        notEqualHelper("Both False/True", Value.FALSE, Value.TRUE, true);
        notEqualHelper("Both False/False", Value.FALSE, Value.FALSE, false);
        notEqualHelper("Logic false to String false", Value.FALSE, Value.of("false"), false);
        notEqualHelper("Logic false to String other", Value.FALSE, Value.of("other"), true);
        notEqualHelper("Logic false to Number 0", Value.FALSE, Value.of(0), false);
        notEqualHelper("Logic false to Number 123", Value.FALSE, Value.of(123), true);
        notEqualHelper("Logic false to Date 0", Value.FALSE, Value.of(START), false);
        notEqualHelper("Logic false to Date 1", Value.FALSE, Value.of(ONE_SECOND), true);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Executor, Executor)}
     */
    @Test
    public void test_notEqual_date() {
        notEqualHelper("Date 0 to String 1970", Value.of(START), Value.of("1970-01-01T00:00:00Z"), false);
        notEqualHelper("Date 0 to String 0", Value.of(START), Value.of("0"), true);
        notEqualHelper("Date 0 to String false", Value.of(START), Value.of("false"), true);
        notEqualHelper("Date 0 to String other", Value.of(START), Value.of("other"), true);
        notEqualHelper("Date 0 to Number 0", Value.of(START), Value.of(0), false);
        notEqualHelper("Date 0 to Number 123", Value.of(START), Value.of(123), true);
        notEqualHelper("Date 0 to Logic false", Value.of(START), Value.FALSE, false);
        notEqualHelper("Date 0 to Logic true", Value.of(START), Value.TRUE, true);
        notEqualHelper("Date 0 to Date 0", Value.of(START), Value.of(START), false);
        notEqualHelper("Date 0 to Date 1", Value.of(START), Value.of(ONE_SECOND), true);
    }

    private void notEqualHelper(@Nonnull String message, @Nonnull Value left, @Nonnull Value right, boolean expected) {
        Executor actual = compile.notEqual(s -> left, s -> right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type for " + message, Type.LOGIC, value.getType());
        Assert.assertEquals("Unexpected value for " + message, expected, value.asLogic());
    }


    /**
     * Unit test {@link LambdaCompiler#greaterThan(Executor, Executor)}
     */
    @Test
    public void test_greaterThan() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.greaterThan(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#greaterThenEquals(Executor, Executor)}
     */
    @Test
    public void test_greaterThanEqual() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.greaterThenEquals(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#lessThan(Executor, Executor)}
     */
    @Test
    public void test_lessThan() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.lessThan(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#lessThanEquals(Executor, Executor)}
     */
    @Test
    public void test_lessThanEqual() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.lessThanEquals(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
    }

                  //*** Shift Ops ***//

    /**
     * Unit test {@link LambdaCompiler#leftShift(Executor, Executor)}
     */
    @Test
    public void test_leftShift() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.leftShift(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 448, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#rightShift(Executor, Executor)}
     */
    @Test
    public void test_rightShift() {
        Executor left = s -> Value.of(448);
        Executor right = s -> Value.of(5);

        Executor actual = compile.rightShift(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 14, value.asNumber().intValue());
    }

                 //*** Numeric Ops ***//

    /**
     * Unit test {@link LambdaCompiler#negate(Executor)}
     */
    @Test
    public void test_negate() {
        Executor operand = s -> Value.ONE;

        Executor actual = compile.negate(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("-1"), value.asNumber());
    }


    /**
     * Unit test {@link LambdaCompiler#plus(Executor, Executor)}
     */
    @Test
    public void test_plus_numbers() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.plus(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 19, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#minus(Executor, Executor)}
     */
    @Test
    public void test_minus() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.minus(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 9, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#multiply(Executor, Executor)}
     */
    @Test
    public void test_multiply() {
        Executor left = s -> Value.of(3);
        Executor right = s -> Value.of(4);

        Executor actual = compile.multiply(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 12, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Executor, Executor)}
     */
    @Test
    public void test_divide() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.divide(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("3.75"), value.asNumber());
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Executor, Executor)}
     */
    @Test
    public void test_divideByZero() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divide(left, right).execute(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#modulus(Executor, Executor)}
     */
    @Test
    public void test_modulus() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.modulus(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 3, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#modulus(Executor, Executor)}
     */
    @Test
    public void test_modulusByZero() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.modulus(left, right).execute(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#power(Executor, Executor)}
     */
    @Test
    public void test_power() {
        Executor left = s -> Value.of(3);
        Executor right = s -> Value.of(4);

        Executor actual = compile.power(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 81, value.asNumber().intValue());
    }

                //*** Logic Ops ***//

    /**
     * Unit test {@link LambdaCompiler#logicalNot(Executor)}
     */
    @Test
    public void test_logicalNot() {
        Executor operand = s -> Value.TRUE;

        Executor actual = compile.logicalNot(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Executor, Executor)}
     */
    @Test
    public void test_logicalAnd() {
        Executor left = s -> Value.TRUE;
        Executor right = s -> Value.TRUE;

        Executor actual = compile.logicalAnd(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#shortCircuitAnd(Executor, Executor)}
     */
    @Test
    public void test_shortCircuitAnd() {
        Executor left = s -> Value.FALSE;
        Executor right = s -> Value.BLANK;

        Executor actual = compile.shortCircuitAnd(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertFalse("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#logicalOr(Executor, Executor)}
     */
    @Test
    public void test_logicalOr() {
        Executor left = s -> Value.TRUE;
        Executor right = s -> Value.FALSE;

        Executor actual = compile.logicalOr(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#shortCircuitOr(Executor, Executor)}
     */
    @Test
    public void test_shortCircuitOr() {
        Executor left = s -> Value.TRUE;
        Executor right = s -> Value.BLANK;

        Executor actual = compile.shortCircuitOr(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

                //*** Bitwise Ops ***//

    /**
     * Unit test {@link LambdaCompiler#bitwiseNot(Executor)}
     */
    @Test
    public void test_bitwiseNot() {
        Executor operand = s -> Value.of(0xa5);

        Executor actual = compile.bitwiseNot(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 0x5a, value.asNumber().byteValue());
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseAnd(Executor, Executor)}
     */
    @Test
    public void test_bitwiseAnd() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.bitwiseAnd(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 4, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseOr(Executor, Executor)}
     */
    @Test
    public void test_bitwiseOr() {
        Executor left = s -> Value.of(12);
        Executor right = s -> Value.of(5);

        Executor actual = compile.bitwiseOr(left,right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 13, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#bitwiseXor(Executor, Executor)}
     */
    @Test
    public void test_bitwiseXor() {
        Executor left = s -> Value.of(14);
        Executor right = s -> Value.of(5);

        Executor actual = compile.bitwiseXor(left, right);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 11, value.asNumber().intValue());
    }

                //*** Cast Ops ***//

    /**
     * Unit test {@link LambdaCompiler#toText(Executor)}
     */
    @Test
    public void test_cast_toText() {
        Executor operand = s -> Value.TRUE;

        Executor actual = compile.toText(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "true", value.asText());
    }

    /**
     * Unit test {@link LambdaCompiler#toNumber(Executor)}
     */
    @Test
    public void test_toNumber() {
        Executor operand = s -> Value.TRUE;

        Executor actual = compile.toNumber(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", 1, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#toLogic(Executor)}
     */
    @Test
    public void test_cast_toLogic() {
        Executor operand = s -> Value.ONE;

        Executor actual = compile.toLogic(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertTrue("Unexpected value", value.asLogic());
    }

    /**
     * Unit test {@link LambdaCompiler#toDate(Executor)}
     */
    @Test
    public void test_cast_toDate() {
        Executor operand = s -> Value.ZERO;

        Executor actual = compile.toDate(operand);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.DATE, value.getType());
        Assert.assertEquals("Unexpected value",
            ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            value.asDate());
    }

                //*** Misc Ops ***//

    /**
     * Unit test {@link LambdaCompiler#concatenate(Executor, Executor)}
     */
    @Test
    public void test_concatenate() {
        Executor first = s -> Value.of("Hello ");
        Executor second = s -> Value.of("World");

        Executor actual = compile.concatenate(first, second);
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", "Hello World", value.asText());
    }
}