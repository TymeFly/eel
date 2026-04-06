package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Value}
 */
public class ValueTest {
    private static final ZonedDateTime DATE_1 = EelContext.FALSE_DATE.plusSeconds(1);
    private static final ZonedDateTime DATE_10 = EelContext.FALSE_DATE.plusSeconds(10);

    /**
     * Unit test {@link Value#BLANK}
     */
    @Test
    public void test_BLANK() {
        Value actual = Value.of("");

        assertSame(Value.BLANK, actual, "Unexpected Constant");
        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("", actual.asText(), "Unexpected Text");
        assertThrows(EelConvertException.class, actual::asNumber, "Unexpected Number");
        assertThrows(EelConvertException.class, actual::asBigInteger, "Unexpected BigInteger");
        assertThrows(EelConvertException.class, actual::asInt, "Unexpected Integer");
        assertThrows(EelConvertException.class, actual::asDate, "Unexpected Date");

        assertFalse(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#ZERO}
     */
    @Test
    public void test_ZERO() {
        Value actual = Value.of(0L);

        assertEquals(Value.ZERO, actual, "Unexpected Constant");
        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals("0", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ZERO, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.ZERO, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(0, actual.asInt(), "Unexpected Integer");
        assertEquals(EelContext.FALSE_DATE, actual.asDate(), "Unexpected Date");
        assertFalse(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#ONE}
     */
    @Test
    public void test_ONE() {
        Value actual = Value.of(1L);

        assertEquals(Value.ONE, actual, "Unexpected Constant");
        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals("1", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.ONE, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(1, actual.asInt(), "Unexpected Integer");
        assertEquals(DATE_1, actual.asDate(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#TEN}
     */
    @Test
    public void test_TEN() {
        Value actual = Value.of(10L);

        assertEquals(Value.TEN, actual, "Unexpected Constant");
        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals("10", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.TEN, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.TEN, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(10, actual.asInt(), "Unexpected Integer");
        assertEquals(DATE_10, actual.asDate(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#TRUE}
     */
    @Test
    public void test_TRUE() {
        Value actual = Value.of(true);

        assertSame(Value.TRUE, actual, "Unexpected Constant");
        assertEquals(Type.LOGIC, actual.getType(), "Unexpected Type");
        assertEquals("true", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.ONE, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(1, actual.asInt(), "Unexpected Integer");
        assertEquals(DATE_1, actual.asDate(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#FALSE}
     */
    @Test
    public void test_FALSE() {
        Value actual = Value.of(Boolean.FALSE);

        assertSame(Value.FALSE, actual, "Unexpected Constant");
        assertEquals(Type.LOGIC, actual.getType(), "Unexpected Type");
        assertEquals("false", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ZERO, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.ZERO, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(0, actual.asInt(), "Unexpected Integer");
        assertEquals(EelContext.FALSE_DATE, actual.asDate(), "Unexpected Date");
        assertFalse(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_UTC() {
        Value actual = Value.of(EelContext.FALSE_DATE);

        assertSame(Value.EPOCH_START_UTC, actual, "Unexpected Constant");
        assertEquals(Type.DATE, actual.getType(), "Unexpected Type");
        assertEquals("1970-01-01T00:00:00Z", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ZERO, actual.asNumber(), "Unexpected Number");
        assertEquals(BigInteger.ZERO, actual.asBigInteger(), "Unexpected BigInteger");
        assertEquals(0, actual.asInt(), "Unexpected Integer");
        assertEquals(EelContext.FALSE_DATE, actual.asDate(), "Unexpected Date");
        assertFalse(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_otherZone() {
        ZonedDateTime date = EelContext.FALSE_DATE.withZoneSameInstant(ZoneOffset.of("-5"));
        Value actual = Value.of(date);

        assertNotSame(Value.EPOCH_START_UTC, actual, "Unexpected Constant");
        assertEquals(Type.DATE, actual.getType(), "Unexpected Type");
        assertEquals(-18000, actual.asDate().getOffset().getTotalSeconds(), "Unexpected Offset");    // Keep Zone
    }



    /**
     * Unit test {@link Value#of(String)}
     */
    @Test
    public void test_of_String() {
        Value actual = Value.of("0");

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("0", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ZERO, actual.asNumber(), "Unexpected Number");
        assertThrows(EelConvertException.class, actual::asDate, "Unexpected Date");
        assertFalse(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#of(Number)}
     */
    @Test
    public void test_of_Number() {
        Value actual = Value.of(1);

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals("1", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Number");
        assertEquals(1, actual.asDate().getSecond(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#of(boolean)}
     */
    @Test
    public void test_of_bool() {
        Value actual = Value.of(true);

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected Type");
        assertEquals("true", actual.asText(), "Unexpected Text");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected Number");
        assertEquals(1, actual.asDate().getSecond(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }

    /**
     * Unit test {@link Value#of(ZonedDateTime)}
     */
    @Test
    public void test_of_time() {
        // Nanos will be ignored
        ZonedDateTime start = ZonedDateTime.of(1970, 1, 1, 0, 0, 10, 123_000_000, ZoneId.of("UTC"));
        Value actual = Value.of(start);

        assertEquals(Type.DATE, actual.getType(), "Unexpected Type");
        assertEquals("1970-01-01T00:00:10.123Z", actual.asText(), "Unexpected Text");
        assertEquals(new BigDecimal("10.123"), actual.asNumber(), "Unexpected Number");
        assertEquals(10, actual.asDate().getSecond(), "Unexpected Date");
        assertTrue(actual.asLogic(), "Unexpected Logic");
    }
}