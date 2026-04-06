package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional testing for the EEL operators
 */
@ExtendWith(SystemStubsExtension.class)
public class OperatorsIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    private EelContext context;


    @BeforeEach
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
        assertEquals((byte) 0xf5, Eel.compile(context, "$( ~ 0xA )").evaluate().asNumber().byteValue(), "not");
        assertEquals((byte) 0x0a, Eel.compile(context, "$( ~ ~ 0xA )").evaluate().asNumber().byteValue(), "Double not");
        assertEquals(0x8, Eel.compile(context, "$( 0xc & 0xA )").evaluate().asNumber().byteValue(), "and");
        assertEquals(0xe, Eel.compile(context, "$( 0xc | 0xA )").evaluate().asNumber().byteValue(), "or");
        assertEquals(0x6, Eel.compile(context, "$( 0xc ^ 0xA )").evaluate().asNumber().byteValue(), "xor");
        assertEquals((byte) 0x5,
            Eel.compile(context, "$( (0xc & ~ 0xA) | 0x1 )").evaluate().asNumber().byteValue(),
            "Combined");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logic() {
        assertTrue(Eel.compile(context, "$( not false )").evaluate().asLogic(), "not");
        assertFalse(Eel.compile(context, "$( not not false )").evaluate().asLogic(), "Double not");
        assertFalse(Eel.compile(context, "$( true and false )").evaluate().asLogic(), "and");
        assertTrue(Eel.compile(context, "$( true or false )").evaluate().asLogic(), "or");
        assertTrue(Eel.compile(context, "$( true xor false )").evaluate().asLogic(), "xor");
        assertTrue(Eel.compile(context, "$( not false or false)").evaluate().asLogic(), "Combined");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ternary() {
        Result actual = Eel.compile(context, "$( (true != false) ? 'first' ~> '!' : 'second' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("first!", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_relations() {
        assertTrue(Eel.compile(context, "$( 1 = 1 )").evaluate().asLogic(), "= positive");
        assertFalse(Eel.compile(context, "$( 1 = 2 )").evaluate().asLogic(), "= negative");

        assertTrue(Eel.compile(context, "$( 1 <> 2 )").evaluate().asLogic(), "<> positive");
        assertFalse(Eel.compile(context, "$( 1 <> 1 )").evaluate().asLogic(),"<> negative");

        assertTrue(Eel.compile(context, "$( 1 != 2 )").evaluate().asLogic(), "!= positive");
        assertFalse(Eel.compile(context, "$( 1 != 1 )").evaluate().asLogic(), "!= negative");

        assertTrue(Eel.compile(context, "$( 1 > -1 )").evaluate().asLogic(), "> positive");
        assertFalse(Eel.compile(context, "$( 1 > +1 )").evaluate().asLogic(), "> negative");

        assertTrue(Eel.compile(context, "$( 1 >= 1 )").evaluate().asLogic(), ">= positive");
        assertFalse(Eel.compile(context, "$( 1 >= 1.1 )").evaluate().asLogic(), ">= negative");

        assertTrue(Eel.compile(context, "$( 1 < 1.1 )").evaluate().asLogic(), "< positive");
        assertFalse(Eel.compile(context, "$( 1 < 0.9 )").evaluate().asLogic(), "< negative");

        assertTrue(Eel.compile(context, "$( 1 <= 1 )").evaluate().asLogic(), "<= positive");
        assertFalse(Eel.compile(context, "$( 1 <= 0.9995 )").evaluate().asLogic(), "<= negative");

        assertTrue(Eel.compile(context, "$( date.utc() isBefore date.utc() + 5 )").evaluate().asLogic(), "isBefore positive");
        assertFalse(Eel.compile(context, "$( date.utc() + 5 isBefore date.utc() )").evaluate().asLogic(), "isBefore negative");

        assertTrue(Eel.compile(context, "$( date.utc() + 5 isAfter date.utc() )").evaluate().asLogic(), "isAfter positive");
        assertFalse(Eel.compile(context, "$( date.utc() isAfter date.utc() + 5 )").evaluate().asLogic(), "isAfter negative");

        assertTrue(Eel.compile(context, "$( 1 in { 2, random(5, 9), (2 - 1) } )").evaluate().asLogic(), "in positive");
        assertFalse(Eel.compile(context,"$( 1 in { 2, random(5, 9), number.e(), false } )").evaluate().asLogic(), "in negative");
        assertTrue(Eel.compile(context, "$( true in { 1 in { 1 }, 99 } )").evaluate().asLogic(), "in nested");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitShifts_positive() {
        assertEquals(0x0020, Eel.compile(context, "$( 16 << 1 )").evaluate().asInt(), "left 1");
        assertEquals(0x0040, Eel.compile(context, "$( 16 << 2 )").evaluate().asInt(), "left 2");
        assertEquals(0x0200, Eel.compile(context, "$( 16 << 5 )").evaluate().asInt(), "left 5");

        assertEquals(0x0008, Eel.compile(context, "$( 16 >> 1 )").evaluate().asInt(), "right 1");
        assertEquals(0x0004, Eel.compile(context, "$( 16 >> 2 )").evaluate().asInt(), "right 2");
        assertEquals(0x0000, Eel.compile(context, "$( 16 >> 5 )").evaluate().asInt(), "right 5");
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
        assertTrue(gt(leftShift, large), "leftShift should get larger");
        assertFalse(gt(rightShift, large), "rightShift should get smaller");

        assertEquals(rightShift, leftShift.divide(BigDecimal.valueOf(4), RoundingMode.DOWN), "bad values");
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
        assertEquals(-226, Eel.compile(context, "$( -113 << 1 )").evaluate().asInt(), "left -1");
        assertEquals(-452, Eel.compile(context, "$( -113 << 2 )").evaluate().asInt(), "left -2");
        assertEquals(-3616, Eel.compile(context, "$( -113 << 5 )").evaluate().asInt(), "left -5");

        assertEquals(-57, Eel.compile(context, "$( -113 >> 1 )").evaluate().asInt(), "right 1");
        assertEquals(-29, Eel.compile(context, "$( -113 >> 2 )").evaluate().asInt(), "right 2");
        assertEquals(-4, Eel.compile(context, "$( -113 >> 5 )").evaluate().asInt(), "right 5");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        assertEquals(26, Eel.compile(context, "$( 5 + 21 )").evaluate().asInt(), "Plus");
        assertEquals(-16, Eel.compile(context, "$( 5 - 21 )").evaluate().asInt(), "Minus");
        assertEquals(42, Eel.compile(context, "$( 6 * 7 )").evaluate().asInt(), "Multiply");
        assertEquals(new BigDecimal("4.5"), Eel.compile(context, "$( 31.5 / 7 )").evaluate().asNumber(), "Divide");
        assertEquals(new BigDecimal("4"), Eel.compile(context, "$( 31.5 // 7 )").evaluate().asNumber(), "Divide Floor");
        assertEquals(new BigDecimal("4"), Eel.compile(context, "$( 31.5 -/ 7 )").evaluate().asNumber(), "Divide Truncate");
        assertEquals(3, Eel.compile(context, "$( 45 % 7 )").evaluate().asInt(), "Modulus");
        assertEquals(4, Eel.compile(context, "$( 0.5 ** -2 )").evaluate().asInt(), "Power");
        assertEquals(new BigDecimal("8.000000000000000"), Eel.compile(context, "$( 64 ** 0.5 )").evaluate().asNumber(), "Root");
        assertEquals(18, Eel.compile(context, "$( (8/2) ** (3-1) + 2)").evaluate().asInt(), "combined");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_parsePriorities() {
        assertEquals(new BigDecimal("-1"), Eel.compile(context, "$( - (true ? 1 : 0) )").evaluate().asNumber(), "- (true ? 1 : 0)");
        assertEquals(new BigDecimal("6"), Eel.compile(context, "$( - ~ 5 )").evaluate().asNumber(), "- ~ 5");
        assertEquals(new BigDecimal("4"), Eel.compile(context, "$( ~ -5 )").evaluate().asNumber(), "~ -5");
        assertEquals(new BigDecimal("6"), Eel.compile(context, "$( 1 - -5 )").evaluate().asNumber(), "1 - -5");
        assertEquals(new BigDecimal("-20"), Eel.compile(context, "$( 2 * -10 )").evaluate().asNumber(), "2 * -10");
        assertEquals(new BigDecimal("0.04321391826377227"),
            Eel.compile(context, "$(number.e() ** -number.pi() )").evaluate().asNumber(),
            "e ** -pi");
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

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertEquals(new BigDecimal(expected), actual.asNumber(), expression);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined() {
        Result actual = Eel.compile(context, "$( key? )")
            .evaluate(Map.of("key", "value"));

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isUndefined() {
        Result actual = Eel.compile(context, "$( key? )")
            .evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertFalse(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined_custom() {
        Result actual = Eel.compile(context, "$( key? ? 'found' : 'notFound' )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("notFound", actual.asText(), "Unexpected value");
    }
}
