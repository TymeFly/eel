package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Functional testing of each of the EEL operators
 */
public class OperatorsIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    private EelContext context;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitwise() {
        Assert.assertEquals("not", (byte) 0xf5, Eel.compile(context, "$( ~ 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("Double not", (byte) 0x0a, Eel.compile(context, "$( ~ ~ 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("and", 0x8, Eel.compile(context, "$( 0xc & 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("or", 0xe, Eel.compile(context, "$( 0xc | 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("xor", 0x6, Eel.compile(context, "$( 0xc ^ 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("Combined",
            (byte) 0x5,
            Eel.compile(context, "$( (0xc & ~ 0xA) | 0x1 )").evaluate().asNumber().byteValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logic() {
        Assert.assertTrue("not", Eel.compile(context, "$( not false )").evaluate().asLogic());
        Assert.assertFalse("Double not", Eel.compile(context, "$( not not false )").evaluate().asLogic());
        Assert.assertFalse("and", Eel.compile(context, "$( true and false )").evaluate().asLogic());
        Assert.assertTrue("or", Eel.compile(context, "$( true or false )").evaluate().asLogic());
        Assert.assertTrue("xor", Eel.compile(context, "$( true xor false )").evaluate().asLogic());
        Assert.assertTrue("Combined", Eel.compile(context, "$( not false or false)").evaluate().asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ternary() {
        Result actual = Eel.compile(context, "$( (true != false) ? 'first' ~> '!' : 'second' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "first!", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_relations() {
        Assert.assertTrue("= positive", Eel.compile(context, "$( 1 = 1 )").evaluate().asLogic());
        Assert.assertFalse("= negative", Eel.compile(context, "$( 1 = 2 )").evaluate().asLogic());

        Assert.assertTrue("<> positive", Eel.compile(context, "$( 1 <> 2 )").evaluate().asLogic());
        Assert.assertFalse("<> negative", Eel.compile(context, "$( 1 <> 1 )").evaluate().asLogic());

        Assert.assertTrue("!= positive", Eel.compile(context, "$( 1 != 2 )").evaluate().asLogic());
        Assert.assertFalse("!= negative", Eel.compile(context, "$( 1 != 1 )").evaluate().asLogic());

        Assert.assertTrue("> positive", Eel.compile(context, "$( 1 > -1 )").evaluate().asLogic());
        Assert.assertFalse("> negative", Eel.compile(context, "$( 1 > +1 )").evaluate().asLogic());

        Assert.assertTrue(">= positive", Eel.compile(context, "$( 1 >= 1 )").evaluate().asLogic());
        Assert.assertFalse(">= negative", Eel.compile(context, "$( 1 >= 1.1 )").evaluate().asLogic());

        Assert.assertTrue("< positive", Eel.compile(context, "$( 1 < 1.1 )").evaluate().asLogic());
        Assert.assertFalse("< negative", Eel.compile(context, "$( 1 < 0.9 )").evaluate().asLogic());

        Assert.assertTrue("<= positive", Eel.compile(context, "$( 1 <= 1 )").evaluate().asLogic());
        Assert.assertFalse("<= negative", Eel.compile(context, "$( 1 <= 0.9995 )").evaluate().asLogic());

        Assert.assertTrue("isBefore positive", Eel.compile(context, "$( date.utc() isBefore date.utc() + 5 )").evaluate().asLogic());
        Assert.assertFalse("isBefore negative", Eel.compile(context, "$( date.utc() + 5 isBefore date.utc() )").evaluate().asLogic());

        Assert.assertTrue("isAfter positive", Eel.compile(context, "$( date.utc() + 5 isAfter date.utc() )").evaluate().asLogic());
        Assert.assertFalse("isAfter negative", Eel.compile(context, "$( date.utc() isAfter date.utc() + 5 )").evaluate().asLogic());

        Assert.assertTrue("in positive", Eel.compile(context, "$( 1 in { 2, random(5, 9), (2 - 1) } )").evaluate().asLogic());
        Assert.assertFalse("in negative", Eel.compile(context,"$( 1 in { 2, random(5, 9), number.e(), false } )").evaluate().asLogic());
        Assert.assertTrue("in nested", Eel.compile(context, "$( true in { 1 in { 1 }, 99 } )").evaluate().asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitShifts_positive() {
        Assert.assertEquals("left 1", 0x0020, Eel.compile(context, "$( 16 << 1 )").evaluate().asInt());
        Assert.assertEquals("left 2", 0x0040, Eel.compile(context, "$( 16 << 2 )").evaluate().asInt());
        Assert.assertEquals("left 5", 0x0200, Eel.compile(context, "$( 16 << 5 )").evaluate().asInt());

        Assert.assertEquals("right 1", 0x0008, Eel.compile(context, "$( 16 >> 1 )").evaluate().asInt());
        Assert.assertEquals("right 2", 0x0004, Eel.compile(context, "$( 16 >> 2 )").evaluate().asInt());
        Assert.assertEquals("right 5", 0x0000, Eel.compile(context, "$( 16 >> 5 )").evaluate().asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitShifts_largeValues() {
        BigDecimal large = new BigDecimal(Long.MAX_VALUE);
        String largeString = large.toPlainString();
        BigDecimal leftShift = Eel.compile(context, "$( " + largeString + " << 1 )").evaluate().asNumber();
        BigDecimal rightShift = Eel.compile(context, "$( " + largeString + " >> 1 )").evaluate().asNumber();

        // Check we don't wrap from negative to positive and vis-versa
        Assert.assertTrue("leftShift should get larger", gt(leftShift, large));
        Assert.assertFalse("rightShift should get smaller", gt(rightShift, large));

        Assert.assertEquals("bad values", rightShift, leftShift.divide(BigDecimal.valueOf(4), RoundingMode.DOWN));
    }

    private boolean gt(@Nonnull BigDecimal left, @Nonnull BigDecimal right) {
        int comparison = left.compareTo(right);
        boolean isGreater = comparison > 0;

        return isGreater;
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitShifts_negative() {
        Assert.assertEquals("left -1", -226, Eel.compile(context, "$( -113 << 1 )").evaluate().asInt());
        Assert.assertEquals("left -2", -452, Eel.compile(context, "$( -113 << 2 )").evaluate().asInt());
        Assert.assertEquals("left -5", -3616, Eel.compile(context, "$( -113 << 5 )").evaluate().asInt());

        Assert.assertEquals("right 1", -57, Eel.compile(context, "$( -113 >> 1 )").evaluate().asInt());
        Assert.assertEquals("right 2", -29, Eel.compile(context, "$( -113 >> 2 )").evaluate().asInt());
        Assert.assertEquals("right 5", -4, Eel.compile(context, "$( -113 >> 5 )").evaluate().asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        Assert.assertEquals("Plus", 26, Eel.compile(context, "$( 5 + 21 )").evaluate().asInt());
        Assert.assertEquals("Minus", -16, Eel.compile(context, "$( 5 - 21 )").evaluate().asInt());
        Assert.assertEquals("Multiply", 42, Eel.compile(context, "$( 6 * 7 )").evaluate().asInt());
        Assert.assertEquals("Divide", new BigDecimal("4.5"), Eel.compile(context, "$( 31.5 / 7 )").evaluate().asNumber());
        Assert.assertEquals("Divide Floor", new BigDecimal("4"), Eel.compile(context, "$( 31.5 // 7 )").evaluate().asNumber());
        Assert.assertEquals("Divide Truncate", new BigDecimal("4"), Eel.compile(context, "$( 31.5 -/ 7 )").evaluate().asNumber());
        Assert.assertEquals("Modulus", 3, Eel.compile(context, "$( 45 % 7 )").evaluate().asInt());
        Assert.assertEquals("Power", 4, Eel.compile(context, "$( 0.5 ** -2 )").evaluate().asInt());
        Assert.assertEquals("Root", new BigDecimal("8.000000000000000"), Eel.compile(context, "$( 64 ** 0.5 )").evaluate().asNumber());
        Assert.assertEquals("combined", 18, Eel.compile(context, "$( (8/2) ** (3-1) + 2)").evaluate().asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_parsePriorities() {
        Assert.assertEquals("- (true ? 1 : 0)", new BigDecimal("-1"), Eel.compile(context, "$( - (true ? 1 : 0) )").evaluate().asNumber());
        Assert.assertEquals("- ~ 5", new BigDecimal("6"), Eel.compile(context, "$( - ~ 5 )").evaluate().asNumber());
        Assert.assertEquals("~ -5", new BigDecimal("4"), Eel.compile(context, "$( ~ -5 )").evaluate().asNumber());
        Assert.assertEquals("1 - -5", new BigDecimal("6"), Eel.compile(context, "$( 1 - -5 )").evaluate().asNumber());
        Assert.assertEquals("2 * -10", new BigDecimal("-20"), Eel.compile(context, "$( 2 * -10 )").evaluate().asNumber());
        Assert.assertEquals("e ** -pi",
            new BigDecimal("0.04321391826377227"),
            Eel.compile(context, "$(number.e() ** -number.pi() )").evaluate().asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_division_divide() {
        divideHelper("$( 12.0 / 1 )", "12.0");
        divideHelper("$( 12.3 / 1 )", "12.3");
        divideHelper("$( 12.5 / 1 )", "12.5");
        divideHelper("$( 12.7 / 1 )", "12.7");

        divideHelper("$( -12.0 / 1 )", "-12.0");
        divideHelper("$( -12.3 / 1 )", "-12.3");
        divideHelper("$( -12.5 / 1 )", "-12.5");
        divideHelper("$( -12.7 / 1 )", "-12.7");

        divideHelper("$( 12.0 / -1 )", "-12.0");
        divideHelper("$( 12.3 / -1 )", "-12.3");
        divideHelper("$( 12.5 / -1 )", "-12.5");
        divideHelper("$( 12.7 / -1 )", "-12.7");

        divideHelper("$( -12.0 / -1 )", "12.0");
        divideHelper("$( -12.3 / -1 )", "12.3");
        divideHelper("$( -12.5 / -1 )", "12.5");
        divideHelper("$( -12.7 / -1 )", "12.7");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_division_divideFloor() {
        divideHelper("$( 12.0 // 1 )", "12");
        divideHelper("$( 12.3 // 1 )", "12");
        divideHelper("$( 12.5 // 1 )", "12");
        divideHelper("$( 12.7 // 1 )", "12");

        divideHelper("$( -12.0 // 1 )", "-12");
        divideHelper("$( -12.3 // 1 )", "-13");
        divideHelper("$( -12.5 // 1 )", "-13");
        divideHelper("$( -12.7 // 1 )", "-13");

        divideHelper("$( 12.0 // -1 )", "-12");
        divideHelper("$( 12.3 // -1 )", "-13");
        divideHelper("$( 12.5 // -1 )", "-13");
        divideHelper("$( 12.7 // -1 )", "-13");

        divideHelper("$( -12.0 // -1 )", "12");
        divideHelper("$( -12.3 // -1 )", "12");
        divideHelper("$( -12.5 // -1 )", "12");
        divideHelper("$( -12.7 // -1 )", "12");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_division_divideTruncate() {
        divideHelper("$( 12.0 -/ 1 )", "12");
        divideHelper("$( 12.3 -/ 1 )", "12");
        divideHelper("$( 12.5 -/ 1 )", "12");
        divideHelper("$( 12.7 -/ 1 )", "12");

        divideHelper("$( -12.0 -/ 1 )", "-12");
        divideHelper("$( -12.3 -/ 1 )", "-12");
        divideHelper("$( -12.5 -/ 1 )", "-12");
        divideHelper("$( -12.7 -/ 1 )", "-12");

        divideHelper("$( 12.0 -/ -1 )", "-12");
        divideHelper("$( 12.3 -/ -1 )", "-12");
        divideHelper("$( 12.5 -/ -1 )", "-12");
        divideHelper("$( 12.7 -/ -1 )", "-12");

        divideHelper("$( -12.0 -/ -1 )", "12");
        divideHelper("$( -12.3 -/ -1 )", "12");
        divideHelper("$( -12.5 -/ -1 )", "12");
        divideHelper("$( -12.7 -/ -1 )", "12");
    }

    private void divideHelper(@Nonnull String expression, @Nonnull String expected) {
        Result actual = Eel.compile(context, expression).evaluate();

        Assert.assertEquals(expression, new BigDecimal(expected), actual.asNumber());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined() {
        Result actual = Eel.compile(context, "$( key? )")
            .evaluate(Map.of("key", "value"));

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isUndefined() {
        Result actual = Eel.compile(context, "$( key? )")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertFalse("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined_custom() {
        Result actual = Eel.compile(context, "$( key? ? 'found' : 'notFound' )")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "notFound", actual.asText());
    }
}
