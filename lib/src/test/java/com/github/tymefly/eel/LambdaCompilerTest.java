package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
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


    private static class CheckedValue implements Executor {
        private final Value value;
        private boolean read = false;

        CheckedValue(@Nonnull Value value) {
            this.value = value;
        }

        @Nonnull
        @Override
        public Value execute(@Nonnull SymbolsTable symbols) {
            read = true;

            return value;
        }

        boolean wasRead() {
            return read;
        }
    }


    @Before
    public void setUp() {
        EelContextImpl context = mock();

        symbols = mock();

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

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#isDefined(String)}
     */
    @Test
    public void test_isDefined_undefined() {
        Executor actual = compile.isDefined("bad.key");
        Value value = actual.execute(symbols);

        assertLogic(false, value);
    }


    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_withoutCaseOp() {
        Executor actual = compile.readSymbol("key")
            .build();
        Value value = actual.execute(symbols);

        assertText("This Value!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_withCaseOp() {
        Executor actual = compile.readSymbol("key")
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.execute(symbols);

        assertText("tHIS vALUE!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_defaultIgnored() {
        Executor defaultValue = s -> Value.of("Hello");
        Executor actual = compile.readSymbol("key")
            .withDefault(defaultValue)
            .build();
        Value value = actual.execute(symbols);

        assertText("This Value!", value);
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_defaultString() {
        Executor defaultValue = s -> Value.of("Hello");
        Executor actual = compile.readSymbol("other")
            .withDefault(defaultValue)
            .withTransformation((s, t) -> StringUtils.toggleAll(t))
            .build();
        Value value = actual.execute(symbols);

        assertText("Hello", value);
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_length() {
        Executor defaultValue = s -> Value.of("Hello");
        Executor actual = compile.readSymbol("key")
            .withDefault(defaultValue)
            .withTransformation(Compiler.SymbolTransformation.LENGTH)
            .build();
        Value value = actual.execute(symbols);

        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", 11, value.asNumber().intValue());
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_defaultLength() {
        Executor defaultValue = s -> Value.of(95);
        Executor actual = compile.readSymbol("other")
            .withDefault(defaultValue)
            .withTransformation(Compiler.SymbolTransformation.LENGTH)
            .build();
        Value value = actual.execute(symbols);

        assertNumber(95, value);
    }

    /**
     * Unit test {@link LambdaCompiler#readSymbol(String)}
     */
    @Test
    public void test_variable_Unknown() {
        EelUnknownSymbolException actual = Assert.assertThrows(EelUnknownSymbolException.class,
            () -> compile.readSymbol("other").build().execute(symbols));

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

        assertText("Hello World", value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_true() {
        Executor actual = compile.logicConstant(true);
        Value value = actual.execute(symbols);

        assertLogic(true, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicConstant(boolean)}
     */
    @Test
    public void test_logicConstant_false() {
        Executor actual = compile.logicConstant(false);
        Value value = actual.execute(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#numericConstant(Number)}
     */
    @Test
    public void test_numericConstant() {
        Executor actual = compile.numericConstant(123.456);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("123.456"), value);
    }


                //*** ternary Op ***//

    /**
     * Unit test {@link LambdaCompiler#conditional(Executor, Executor, Executor)}
     */
    @Test
    public void test_condition_true() {
        Executor condition = s -> Value.of(true);
        Executor first = s -> Value.of("ab");
        Executor second = s -> Value.of("cd");

        Executor actual = compile.conditional(condition, first, second);
        Value value = actual.execute(symbols);

        assertText("ab", value);
    }

    /**
     * Unit test {@link LambdaCompiler#conditional(Executor, Executor, Executor)}
     */
    @Test
    public void test_condition_false() {
        Executor condition = s -> Value.of(false);
        Executor first = s -> Value.of(1);
        Executor second = s -> Value.of(2);

        Executor actual = compile.conditional(condition, first, second);
        Value value = actual.execute(symbols);

        assertNumber(2, value);
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
        equalHelper("String false to Logic false", Value.of("false"), Value.of(false), true);
        equalHelper("String other to Logic false", Value.of("other"), Value.of(false), false);
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
        equalHelper("Number 0 to Logic false", Value.of(0), Value.of(false), true);
        equalHelper("Number 0 to Logic true", Value.of(0), Value.of(true), false);
        equalHelper("Number 0 to Date 0", Value.of(0), Value.of(START), true);
        equalHelper("Number 0 to Date 1", Value.of(0), Value.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_logic() {
        equalHelper("Both True", Value.of(true), Value.of(true), true);
        equalHelper("Both True/False", Value.of(true), Value.of(false), false);
        equalHelper("Both False/True", Value.of(false), Value.of(true), false);
        equalHelper("Both False/False", Value.of(false), Value.of(false), true);
        equalHelper("Logic false to String false", Value.of(false), Value.of("false"), true);
        equalHelper("Logic false to String other", Value.of(false), Value.of("other"), false);
        equalHelper("Logic false to Number 0", Value.of(false), Value.of(0), true);
        equalHelper("Logic false to Number 123", Value.of(false), Value.of(123), false);
        equalHelper("Logic false to Date 0", Value.of(false), Value.of(START), true);
        equalHelper("Logic false to Date 1", Value.of(false), Value.of(ONE_SECOND), false);
    }

    /**
     * Unit test {@link LambdaCompiler#equal(Executor, Executor)}
     */
    @Test
    public void test_equal_date() {
        ZonedDateTime date1 = ZonedDateTime.now(ZoneId.of("-5"));
        ZonedDateTime date2 = ZonedDateTime.now(ZoneId.of("-6"));   // Same instant as date1
        ZonedDateTime date3 = date2.plusHours(1);                   // Same time on clock as date1, but different zone

        equalHelper("Date 0 to String 1970", Value.of(START), Value.of("1970-01-01T00:00:00Z"), true);
        equalHelper("Date 0 to String 0", Value.of(START), Value.of("0"), false);
        equalHelper("Date 0 to String false", Value.of(START), Value.of("false"), false);
        equalHelper("Date 0 to String other", Value.of(START), Value.of("other"), false);
        equalHelper("Date 0 to Number 0", Value.of(START), Value.of(0), true);
        equalHelper("Date 0 to Number 123", Value.of(START), Value.of(123), false);
        equalHelper("Date 0 to Logic false", Value.of(START), Value.of(false), true);
        equalHelper("Date 0 to Logic true", Value.of(START), Value.of(true), false);
        equalHelper("Date 0 to Date 0", Value.of(START), Value.of(START), true);
        equalHelper("Date 0 to Date 1", Value.of(START), Value.of(ONE_SECOND), false);

        equalHelper("date 1 to date2", Value.of(date1), Value.of(date2), true);
        equalHelper("date 1 to date3", Value.of(date1), Value.of(date3), false);
    }

    private void equalHelper(@Nonnull String message, @Nonnull Value left, @Nonnull Value right, boolean expected) {
        Executor actual = compile.equal(s -> left, s -> right);
        Value value = actual.execute(symbols);

        assertLogic(message, expected, value);
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
        notEqualHelper("String false to Logic false", Value.of("false"), Value.of(false), false);
        notEqualHelper("String other to Logic false", Value.of("other"), Value.of(false), true);
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
        notEqualHelper("Number 0 to Logic false", Value.of(0), Value.of(false), false);
        notEqualHelper("Number 0 to Logic true", Value.of(0), Value.of(true), true);
        notEqualHelper("Number 0 to Date 0", Value.of(0), Value.of(START), false);
        notEqualHelper("Number 0 to Date 1", Value.of(0), Value.of(ONE_SECOND), true);
    }

    /**
     * Unit test {@link LambdaCompiler#notEqual(Executor, Executor)}
     */
    @Test
    public void test_notEqual_logic() {
        notEqualHelper("Both True", Value.of(true), Value.of(true), false);
        notEqualHelper("Both True/False", Value.of(true), Value.of(false), true);
        notEqualHelper("Both False/True", Value.of(false), Value.of(true), true);
        notEqualHelper("Both False/False", Value.of(false), Value.of(false), false);
        notEqualHelper("Logic false to String false", Value.of(false), Value.of("false"), false);
        notEqualHelper("Logic false to String other", Value.of(false), Value.of("other"), true);
        notEqualHelper("Logic false to Number 0", Value.of(false), Value.of(0), false);
        notEqualHelper("Logic false to Number 123", Value.of(false), Value.of(123), true);
        notEqualHelper("Logic false to Date 0", Value.of(false), Value.of(START), false);
        notEqualHelper("Logic false to Date 1", Value.of(false), Value.of(ONE_SECOND), true);
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
        notEqualHelper("Date 0 to Logic false", Value.of(START), Value.of(false), false);
        notEqualHelper("Date 0 to Logic true", Value.of(START), Value.of(true), true);
        notEqualHelper("Date 0 to Date 0", Value.of(START), Value.of(START), false);
        notEqualHelper("Date 0 to Date 1", Value.of(START), Value.of(ONE_SECOND), true);
    }

    private void notEqualHelper(@Nonnull String message, @Nonnull Value left, @Nonnull Value right, boolean expected) {
        Executor actual = compile.notEqual(s -> left, s -> right);
        Value value = actual.execute(symbols);

        assertLogic(message, expected, value);
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

        assertLogic(true, value);
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

        assertLogic(true, value);
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

        assertLogic(false, value);
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

        assertLogic(false, value);
    }


    /**
     * Unit test {@link LambdaCompiler#isBefore(Executor, Executor)}
     */
    @Test
    public void test_isBefore() {
        Executor early = s -> Value.of(500);
        Executor late = s -> Value.of(9000);

        assertLogic("left before right", true, compile.isBefore(early, late).execute(symbols));
        assertLogic("left equals right", false, compile.isBefore(early, early).execute(symbols));
        assertLogic("left after right", false, compile.isBefore(late, early).execute(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#isAfter(Executor, Executor)}
     */
    @Test
    public void test_isAfter() {
        Executor early = s -> Value.of(500);
        Executor late = s -> Value.of(9000);

        assertLogic("left before right", false, compile.isAfter(early, late).execute(symbols));
        assertLogic("left equals right", false, compile.isAfter(early, early).execute(symbols));
        assertLogic("left after right", true, compile.isAfter(late, early).execute(symbols));
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

        assertNumber(448, value);
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

        assertNumber(14, value);
    }

                 //*** Numeric Ops ***//

    /**
     * Unit test {@link LambdaCompiler#negate(Executor)}
     */
    @Test
    public void test_negate() {
        Executor operand = s -> Value.of(1);

        Executor actual = compile.negate(operand);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("-1"), value);
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

        assertNumber(19, value);
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

        assertNumber(9, value);
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

        assertNumber(12, value);
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

        assertNumber(new BigDecimal("3.75"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divide(Executor, Executor)}
     */
    @Test
    public void test_divide_byZero() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divide(left, right).execute(symbols));
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Executor, Executor)}
     */
    @Test
    public void test_divideFloor_positive() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.divideFloor(left, right);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("3"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Executor, Executor)}
     */
    @Test
    public void test_divideFloor_negative() {
        Executor left = s -> Value.of(-15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.divideFloor(left, right);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("-4"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divideFloor(Executor, Executor)}
     */
    @Test
    public void test_divideFloor_byZero() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divideFloor(left, right).execute(symbols));
    }


    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Executor, Executor)}
     */
    @Test
    public void test_divideTruncate_positive() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.divideTruncate(left, right);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("3"), value);
    }

    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Executor, Executor)}
     */
    @Test
    public void test_divideTruncate_negative() {
        Executor left = s -> Value.of(-15);
        Executor right = s -> Value.of(4);

        Executor actual = compile.divideTruncate(left, right);
        Value value = actual.execute(symbols);

        assertNumber(new BigDecimal("-3"), value);
    }


    /**
     * Unit test {@link LambdaCompiler#divideTruncate(Executor, Executor)}
     */
    @Test
    public void test_divideTruncate_byZero() {
        Executor left = s -> Value.of(15);
        Executor right = s -> Value.of(0);

        Assert.assertThrows(ArithmeticException.class,
            () -> compile.divideTruncate(left, right).execute(symbols));
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

        assertNumber(3, value);
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

        assertNumber(81, value);
    }

                //*** Logic Ops ***//

    /**
     * Unit test {@link LambdaCompiler#logicalNot(Executor)}
     */
    @Test
    public void test_logicalNot() {
        Executor operand = s -> Value.of(true);

        Executor actual = compile.logicalNot(operand);
        Value value = actual.execute(symbols);

        assertLogic(false, value);
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Executor, Executor)}
     */
    @Test
    public void test_logicalAnd_true() {
        CheckedValue left = new CheckedValue(Value.of(true));
        CheckedValue right = new CheckedValue(Value.of(true));

        Executor actual = compile.logicalAnd(left, right);
        Value value = actual.execute(symbols);

        assertLogic(true, value);

        // Check short circuit
        Assert.assertTrue("left was read", left.wasRead());
        Assert.assertTrue("right was read", right.wasRead());
    }

    /**
     * Unit test {@link LambdaCompiler#logicalAnd(Executor, Executor)}
     */
    @Test
    public void test_logicalAnd_false() {
        CheckedValue left = new CheckedValue(Value.of(false));
        CheckedValue right = new CheckedValue(Value.of(true));

        Executor actual = compile.logicalAnd(left, right);
        Value value = actual.execute(symbols);

        assertLogic(false, value);

        // Check short circuit
        Assert.assertTrue("left was read", left.wasRead());
        Assert.assertFalse("right was not read (short circuit)", right.wasRead());
    }

    /**
     * Unit test {@link LambdaCompiler#logicalOr(Executor, Executor)}
     */
    @Test
    public void test_logicalOr_true() {
        CheckedValue left = new CheckedValue(Value.of(true));
        CheckedValue right = new CheckedValue(Value.of(false));

        Executor actual = compile.logicalOr(left, right);
        Value value = actual.execute(symbols);

        assertLogic(true, value);

        // Check short circuit
        Assert.assertTrue("left was read", left.wasRead());
        Assert.assertFalse("right was not read (short circuit)", right.wasRead());
    }

    /**
     * Unit test {@link LambdaCompiler#logicalOr(Executor, Executor)}
     */
    @Test
    public void test_logicalOr_false() {
        CheckedValue left = new CheckedValue(Value.of(false));
        CheckedValue right = new CheckedValue(Value.of(false));

        Executor actual = compile.logicalOr(left, right);
        Value value = actual.execute(symbols);

        assertLogic(false, value);

        // Check short circuit
        Assert.assertTrue("left was read", left.wasRead());
        Assert.assertTrue("right was read", right.wasRead());
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

        assertNumber((byte)0x5a, value);
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

        assertNumber(4, value);
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

        assertNumber(13, value);
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

        assertNumber(11, value);
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

        assertText("Hello World", value);
    }


                //*** Helper methods ***//

    private void assertText(@Nonnull String expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.TEXT, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asText());
    }

    private void assertNumber(byte expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asNumber().byteValue());
    }

    private void assertNumber(int expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asNumber().intValue());
    }

    private void assertNumber(@Nonnull BigDecimal expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.NUMBER, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asNumber());
    }

    private void assertLogic(boolean expected, @Nonnull Value value) {
        Assert.assertEquals("Unexpected type", Type.LOGIC, value.getType());
        Assert.assertEquals("Unexpected value", expected, value.asLogic());
    }

    private void assertLogic(@Nonnull String message, boolean expected, @Nonnull Value value) {
        Assert.assertEquals(message + ": Unexpected type", Type.LOGIC, value.getType());
        Assert.assertEquals(message + ": Unexpected value", expected, value.asLogic());
    }
}