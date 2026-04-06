package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Constant}
 */
public class ConstantTest {
    private static final ZonedDateTime DATE_STAMP0 = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_STAMP1 = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 678_000_009, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_STAMP2 = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 678_000_009, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_TRUE = ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);

    private final Constant text = Constant.of("123");
    private final Constant number = Constant.of(12.3);
    private final Constant logic = Constant.of(true);
    private final Constant date = Constant.of(DATE_STAMP1);



    /**
     * Unit test {@link Constant#of(String)}
     */
    @Test
    public void test_of_String() {
        assertSame(Value.BLANK, Constant.of(""), "Empty String");

        assertNotSame(Value.BLANK, Constant.of(" "), "Blank String");
        assertNotSame(Value.BLANK, Constant.of("SomeValue"), "String with text");

        String stamp = Long.toString(System.currentTimeMillis());

        assertSame(Constant.of(stamp), Constant.of(stamp), "Check Cache");
    }

    /**
     * Unit test {@link Constant#of(Number)}
     */
    @Test
    public void test_of_Number() {
        assertEquals(Value.ZERO, Constant.of(0), "Int(0)");
        assertEquals(Value.ONE, Constant.of(1), "Int(1)");
        assertEquals(Value.ONE, Constant.of(1L), "Long(1)");
        assertEquals(Value.ONE, Constant.of(BigDecimal.ONE), "BigDecimal(1)");
        assertEquals(Value.ZERO, Constant.of(0.0), "Double(0.0)");

        Long stamp = System.currentTimeMillis();

        assertSame(Constant.of(stamp), Constant.of(stamp), "Check Cache");
    }

    /**
     * Unit test {@link Constant#of(boolean)}
     */
    @Test
    public void test_of_Bool() {
        assertSame(Value.FALSE, Constant.of(false), "false");
        assertSame(Value.FALSE, Constant.of(Boolean.FALSE), "Boolean.FALSE");

        assertSame(Value.TRUE, Constant.of(true), "true");
        assertSame(Value.TRUE, Constant.of(Boolean.TRUE), "Boolean.TRUE");
    }

    /**
     * Unit test {@link Constant#of(ZonedDateTime)}
     */
    @Test
    public void test_of_Date() {
        assertEquals(date, Constant.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 678_000_009, ZoneOffset.UTC)), "date");

        assertNotSame(date, Constant.of(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)), "other year");
        assertNotSame(date, Constant.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.ofHours(8))), "other zone");

        ZonedDateTime stamp = ZonedDateTime.now();

        assertSame(Constant.of(stamp), Constant.of(stamp), "Check Cache");
    }

    /**
     * Unit test {@link Constant#getType()}
     */
    @Test
    public void test_getType() {
        assertEquals(Type.TEXT, text.getType(), "Unexpected type for text");
        assertEquals(Type.NUMBER, number.getType(), "Unexpected type for number");
        assertEquals(Type.LOGIC, logic.getType(), "Unexpected type for logic");
        assertEquals(Type.DATE, date.getType(), "Unexpected type for date");
    }

    /**
     * Unit test {@link Constant#asText()}
     */
    @Test
    public void test_asText() {
        assertEquals("123", text.asText(), "Unexpected text for text");
        assertEquals("12.3", number.asText(), "Unexpected text for number");
        assertEquals("true", logic.asText(), "Unexpected text for logic");
        assertEquals("2022-01-02T03:04:05.678000009Z", date.asText(), "Unexpected text for date");
    }

    /**
     * Unit test {@link Constant#asNumber()}
     */
    @Test
    public void test_asNumber() {
        assertEquals(new BigDecimal("123"), text.asNumber(), "Unexpected number for text");
        assertEquals(new BigDecimal("12.3"), number.asNumber(), "Unexpected number for number");
        assertEquals(new BigDecimal("1"), logic.asNumber(), "Unexpected number for logic");
        assertEquals(new BigDecimal("1641092645.678000009"), date.asNumber(), "Unexpected number for date");

        assertEquals(new BigDecimal("1234"), Constant.of("1234").asNumber(), "Decimal integer");
        assertEquals(new BigDecimal("35243"), Constant.of("0x89ab").asNumber(), "Hexadecimal integer");
        assertEquals(new BigDecimal("123.456"), Constant.of("123.456").asNumber(), "Decimal with a fractional part");
        assertEquals(new BigDecimal("2.997e8"), Constant.of("2.997e8").asNumber(), "Scientific format");
    }

    /**
     * Unit test {@link Constant#asNumber()}
     */
    @Test
    public void test_asNumeric_invalid() {
        assertThrows(EelConvertException.class, () -> Constant.of("some Text").asNumber(), "text");
        assertThrows(EelConvertException.class, () -> Constant.of("0x12.34").asNumber(), "Bad Hex");
        assertThrows(EelConvertException.class, () -> Constant.of("12e3f4").asNumber(), "Bad scientific");
    }


    /**
     * Unit test {@link Constant#asLogic()}
     */
    @Test
    public void test_asLogic() {
        ZonedDateTime TRUE_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1_000), ZoneOffset.UTC);

        assertFalse(Constant.of("False").asLogic(), "Unexpected logic for text");
        assertFalse(Constant.of(0).asLogic(), "Unexpected logic for number");
        assertTrue(logic.asLogic(), "Unexpected logic for logic");

        assertFalse(Constant.of(-0.01).asLogic(), "negative");
        assertFalse(Constant.of(0).asLogic(), "Zero");
        assertTrue(Constant.of(+.01).asLogic(), "positive");

        assertFalse(Constant.of(EelContext.FALSE_DATE).asLogic(), "False Date");
        assertFalse(Constant.of(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic(), "False Date other Zone");
        assertTrue(Constant.of(TRUE_DATE).asLogic(), "Other Date");
        assertTrue(Constant.of(TRUE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic(), "Other Date");
    }

    /**
     * Unit test {@link Constant#asLogic()}
     */
    @Test
    public void test_asLogic_invalid() {
        assertThrows(EelConvertException.class, () -> Constant.of("some Text").asLogic());
    }

    /**
     * Unit test {@link Constant#asDate()}
     */
    @Test
    public void test_asDate() {
        assertEquals(DATE_STAMP0, Constant.of("2022-01-02T03:04:05Z").asDate(), "Unexpected text for text");
        assertEquals(DATE_STAMP0, Constant.of(1641092645).asDate(), "Unexpected text for number");
        assertEquals(DATE_TRUE, Constant.of(true).asDate(), "TRUE");
        assertEquals(EelContext.FALSE_DATE, Constant.of(false).asDate(), "FALSE");
    }

    /**
     * Unit test {@link Constant#asDate()}
     */
    @Test
    public void test_asDate_invalid() {
        assertThrows(EelConvertException.class, () -> Constant.of("some text").asDate(), "Bad Text");
    }


    /**
     * Unit test {@link Constant#equals(Object)}
     */
    @Test
    public void test_equals() {
        Constant sameNumber = Constant.of(new BigDecimal("12.300"));

        assertFalse(text.equals(null), "text to null");
        assertTrue(text.equals(text), "text to text");
        assertFalse(text.equals("123"), "text to String");
        assertTrue(text.equals(Constant.of("123")), "text to same value");
        assertFalse(text.equals(Constant.of("1234")), "text to different value");

        assertTrue(number.equals(Constant.of(12.3)), "number to same value");
        assertFalse(number.equals(Constant.of(12.34)), "number to different value");
        assertTrue(number.equals(sameNumber), "number to same value, different precision");

        assertTrue(logic.equals(Constant.of(true)), "logic to same value");
        assertFalse(logic.equals(Constant.of(false)), "logic to different value");

        assertTrue(date.equals(Constant.of(DATE_STAMP1)), "date to same value");
        assertFalse(date.equals(Constant.of(DATE_STAMP2)), "date to different value");

        assertFalse(number.equals(Constant.of(number.asText())), "number to text");
    }


    /**
     * Unit test {@link Constant#hashCode()}
     */
    @Test
    public void test_hashCode() {
        Constant sameNumber = Constant.of(new BigDecimal("12.300"));

        assertEquals(text.hashCode(), Constant.of("123").hashCode(), "text to same value");
        assertNotEquals(text.hashCode(), Constant.of("1234").hashCode(), "text to different value");

        assertEquals(number.hashCode(), Constant.of(12.3).hashCode(), "number to same value");
        assertNotEquals(number.hashCode(), Constant.of(-12.34).hashCode(), "number to different value");
        assertNotEquals(number.hashCode(), sameNumber.hashCode(), "number to same value, different precision");

        assertEquals(logic.hashCode(), Constant.of(true).hashCode(), "logic to same value");
        assertNotEquals(logic.hashCode(), Constant.of(false).hashCode(), "logic to different value");

        assertEquals(date.hashCode(), Constant.of(DATE_STAMP1).hashCode(), "date to same value");
        assertNotEquals(date.hashCode(), Constant.of(DATE_STAMP2).hashCode(), "date to different value");
    }

    /**
     * Unit test {@link AbstractValue#asBigInteger}
     */
    @Test
    public void test_asBigInteger() {
        assertEquals(new BigInteger("123"), text.asBigInteger(), "Unexpected number for text");
        assertEquals(new BigInteger("12"), number.asBigInteger(), "Unexpected number for number");
        assertEquals(new BigInteger("1"), logic.asBigInteger(), "Unexpected number for logic");
        assertEquals(new BigInteger("1641092645"), date.asBigInteger(), "Unexpected number for date");
    }

    /**
     * Unit test {@link AbstractValue#asDouble}
     */
    @Test
    public void test_asDouble() {
        assertEquals(123.0, text.asDouble(), 0.001, "Unexpected number for text");
        assertEquals(12.3, number.asDouble(), 0.001, "Unexpected number for number");
        assertEquals(1.0, logic.asDouble(), 0.001, "Unexpected number for logic");
        assertEquals(1641092645.678, date.asDouble(), 0.001, "Unexpected number for date");
    }

    /**
     * Unit test {@link AbstractValue#asLong}
     */
    @Test
    public void test_asLong() {
        assertEquals(123L, text.asLong(), "Unexpected number for text");
        assertEquals(12L, number.asLong(), "Unexpected number for number");
        assertEquals(1L, logic.asLong(), "Unexpected number for logic");
        assertEquals(1641092645L, date.asLong(), "Unexpected number for date");
    }

    /**
     * Unit test {@link AbstractValue#asInt}
     */
    @Test
    public void test_asInt() {
        assertEquals(123, text.asInt(), "Unexpected number for text");
        assertEquals(12, number.asInt(), "Unexpected number for number");
        assertEquals(1, logic.asInt(), "Unexpected number for logic");
        assertEquals(1641092645, date.asInt(), "Unexpected number for date");
    }

    /**
     * Unit test {@link Constant#asChar()}
     */
    @Test
    public void test_asChar() {
        assertEquals('X', Constant.of("X").asChar(), "From String of 1 character");
        assertEquals('a', Constant.of("abc").asChar(), "From String with multiple characters");
        assertEquals('1', Constant.of(12.34).asChar(), "From Number");
        assertEquals('t', Constant.of(true).asChar(), "From Logic");
        assertEquals('1', Constant.of(EelContext.FALSE_DATE).asChar(), "From Date");

        assertThrows(EelConvertException.class, () -> Constant.of("").asChar(), "Empty String");
    }
}