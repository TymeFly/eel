package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelValue}
 */
public class EelValueTest {
    private static final ZonedDateTime DATE_1 = EelContext.FALSE_DATE.plusSeconds(1);
    private static final ZonedDateTime DATE_10 = EelContext.FALSE_DATE.plusSeconds(10);

    /**
     * Unit test {@link EelValue#BLANK}
     */
    @Test
    public void test_BLANK() {
        Value actual = Value.of("");

        Assert.assertSame("Unexpected Constant", EelValue.BLANK, actual);
        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "", actual.asText());
        Assert.assertThrows("Unexpected Number", EelConvertException.class, actual::asNumber);
        Assert.assertThrows("Unexpected BigInteger", EelConvertException.class, actual::asBigInteger);
        Assert.assertThrows("Unexpected Integer", EelConvertException.class, actual::asInteger);
        Assert.assertThrows("Unexpected Date", EelConvertException.class, actual::asDate);

        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#ZERO}
     */
    @Test
    public void test_ZERO() {
        Value actual = Value.of(0.0f);

        Assert.assertSame("Unexpected Constant", EelValue.ZERO, actual);
        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "0", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#ONE}
     */
    @Test
    public void test_ONE() {
        Value actual = Value.of(1.0);

        Assert.assertSame("Unexpected Constant", EelValue.ONE, actual);
        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "1", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ONE, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 1, actual.asInteger());
        Assert.assertEquals("Unexpected Date", DATE_1, actual.asDate());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#TEN}
     */
    @Test
    public void test_TEN() {
        Value actual = Value.of(10L);

        Assert.assertSame("Unexpected Constant", EelValue.TEN, actual);
        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "10", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.TEN, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.TEN, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 10, actual.asInteger());
        Assert.assertEquals("Unexpected Date", DATE_10, actual.asDate());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#TRUE}
     */
    @Test
    public void test_TRUE() {
        Value actual = Value.of(true);

        Assert.assertSame("Unexpected Constant", EelValue.TRUE, actual);
        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "true", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ONE, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 1, actual.asInteger());
        Assert.assertEquals("Unexpected Date", DATE_1, actual.asDate());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#FALSE}
     */
    @Test
    public void test_FALSE() {
        Value actual = Value.of(Boolean.FALSE);

        Assert.assertSame("Unexpected Constant", EelValue.FALSE, actual);
        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "false", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_UTC() {
        Value actual = Value.of(EelContext.FALSE_DATE);

        Assert.assertSame("Unexpected Constant", EelValue.EPOCH_START_UTC, actual);
        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Text", "1970-01-01T00:00:00Z", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_otherZone() {
        ZonedDateTime date = EelContext.FALSE_DATE.withZoneSameInstant(ZoneOffset.of("-5"));
        Value actual = Value.of(date);

        Assert.assertNotSame("Unexpected Constant", EelValue.EPOCH_START_UTC, actual);
        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Offset", -18000, actual.asDate().getOffset().getTotalSeconds());    // Keep Zone
    }



    /**
     * Unit test {@link EelValue#of(String)} 
     */
    @Test
    public void test_of_String() {
        EelValue actual = EelValue.of("0");

        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "0", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertThrows("Unexpected Date", EelConvertException.class, actual::asDate);
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(Number)} 
     */
    @Test
    public void test_of_Number() {
        EelValue actual = EelValue.of(1);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "1", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 1, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(boolean)}
     */
    @Test
    public void test_of_bool() {
        EelValue actual = EelValue.of(true);

        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "true", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 1, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(ZonedDateTime)} 
     */
    @Test
    public void test_of_time() {
        // Nanos will be ignored
        ZonedDateTime start = ZonedDateTime.of(1970, 1, 1, 0, 0, 10, 1234, ZoneId.of("UTC"));
        EelValue actual = EelValue.of(start);

        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Text", "1970-01-01T00:00:10Z", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.TEN, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 10, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }
}