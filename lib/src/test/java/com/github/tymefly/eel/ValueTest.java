package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Value}
 */
public class ValueTest {
    private static final ZonedDateTime DATE_STAMP0 = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_STAMP1 = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_STAMP2 = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC);
    private static final ZonedDateTime DATE_TRUE = ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);

    private final Value text = Value.of("123");
    private final Value number = Value.of(12.3);
    private final Value logic = Value.of(true);
    private final Value date = Value.of(DATE_STAMP1);


    /**
     * Unit test {@link Value#BLANK}
     */
    @Test
    public void test_BLANK() {
        Value actual = Value.of("");

        Assert.assertSame("Unexpected Constant", Value.BLANK, actual);
        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "", actual.asText());
        Assert.assertThrows("Unexpected Number", EelConvertException.class, actual::asNumber);
        Assert.assertThrows("Unexpected BigInteger", EelConvertException.class, actual::asBigInteger);
        Assert.assertThrows("Unexpected Integer", EelConvertException.class, actual::asInteger);
        Assert.assertThrows("Unexpected Date", EelConvertException.class, actual::asDate);
        Assert.assertThrows("Unexpected Logic", EelConvertException.class, actual::asLogic);
    }

    /**
     * Unit test {@link Value#ZERO}
     */
    @Test
    public void test_ZERO() {
        Value actual = Value.of(0.0f);

        Assert.assertSame("Unexpected Constant", Value.ZERO, actual);
        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "0", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link Value#ZERO}
     */
    @Test
    public void test_ONE() {
        Value actual = Value.of(1.0);

        Assert.assertSame("Unexpected Constant", Value.ONE, actual);
        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "1", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ONE, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 1, actual.asInteger());
        Assert.assertEquals("Unexpected Date", DATE_TRUE, actual.asDate());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link Value#TRUE}
     */
    @Test
    public void test_TRUE() {
        Value actual = Value.of(true);

        Assert.assertSame("Unexpected Constant", Value.TRUE, actual);
        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "true", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ONE, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 1, actual.asInteger());
        Assert.assertEquals("Unexpected Date", DATE_TRUE, actual.asDate());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link Value#FALSE}
     */
    @Test
    public void test_FALSE() {
        Value actual = Value.of(Boolean.FALSE);

        Assert.assertSame("Unexpected Constant", Value.FALSE, actual);
        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "false", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link Value#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_UTC() {
        Value actual = Value.of(EelContext.FALSE_DATE);

        Assert.assertSame("Unexpected Constant", Value.EPOCH_START_UTC, actual);
        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Text", "1970-01-01T00:00:00Z", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertEquals("Unexpected BigInteger", BigInteger.ZERO, actual.asBigInteger());
        Assert.assertEquals("Unexpected Integer", 0, actual.asInteger());
        Assert.assertEquals("Unexpected Date", EelContext.FALSE_DATE, actual.asDate());
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link Value#EPOCH_START_UTC}
     */
    @Test
    public void test_EPOCH_START_otherZone() {
        ZonedDateTime date = EelContext.FALSE_DATE.withZoneSameInstant(ZoneOffset.of("-5"));
        Value actual = Value.of(date);

        Assert.assertNotSame("Unexpected Constant", Value.EPOCH_START_UTC, actual);
        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Offset", -18000, actual.asDate().getOffset().getTotalSeconds());    // Keep Zone
    }


    /**
     * Unit test {@link Value#of(String)}
     */
    @Test
    public void test_of_String() {
        Assert.assertSame("Empty String", Value.BLANK, Value.of(""));

        Assert.assertNotSame("Blank String", Value.BLANK, Value.of(" "));
        Assert.assertNotSame("String with text", Value.BLANK, Value.of("SomeValue"));

        String stamp = Long.toString(System.currentTimeMillis());

        Assert.assertSame("Check Cache", Value.of(stamp), Value.of(stamp));
    }

    /**
     * Unit test {@link Value#of(Number)}
     */
    @Test
    public void test_of_Number() {
        Assert.assertSame("Int(0)", Value.ZERO, Value.of(0));
        Assert.assertSame("Int(1)", Value.ONE, Value.of(1));
        Assert.assertSame("Long(1)", Value.ONE, Value.of(1L));
        Assert.assertSame("BigDecimal(1)", Value.ONE, Value.of(BigDecimal.ONE));

        Long stamp = System.currentTimeMillis();

        Assert.assertSame("Check Cache", Value.of(stamp), Value.of(stamp));
    }

    /**
     * Unit test {@link Value#of(boolean)}
     */
    @Test
    public void test_of_Bool() {
        Assert.assertSame("false", Value.FALSE, Value.of(false));
        Assert.assertSame("Boolean.FALSE", Value.FALSE, Value.of(Boolean.FALSE));

        Assert.assertSame("true", Value.TRUE, Value.of(true));
        Assert.assertSame("Boolean.TRUE", Value.TRUE, Value.of(Boolean.TRUE));
    }

    /**
     * Unit test {@link Value#of(ZonedDateTime)}
     */
    @Test
    public void test_of_Date() {
        Assert.assertEquals("date", date, Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)));

        Assert.assertNotSame("other year", date, Value.of(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)));
        Assert.assertNotSame("other zone", date, Value.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.ofHours(8))));

        ZonedDateTime stamp = ZonedDateTime.now();

        Assert.assertSame("Check Cache", Value.of(stamp), Value.of(stamp));
    }

    /**
     * Unit test {@link Value#getType()}
     */
    @Test
    public void test_getType() {
        Assert.assertEquals("Unexpected type for text", Type.TEXT, text.getType());
        Assert.assertEquals("Unexpected type for number", Type.NUMBER, number.getType());
        Assert.assertEquals("Unexpected type for logic", Type.LOGIC, logic.getType());
        Assert.assertEquals("Unexpected type for date", Type.DATE, date.getType());

        Assert.assertEquals("Unexpected type for BLANK", Type.TEXT, Value.BLANK.getType());
        Assert.assertEquals("Unexpected type for ZERO", Type.NUMBER, Value.ZERO.getType());
        Assert.assertEquals("Unexpected type for ONE", Type.NUMBER, Value.ONE.getType());
        Assert.assertEquals("Unexpected type for TRUE", Type.LOGIC, Value.TRUE.getType());
        Assert.assertEquals("Unexpected type for FALSE", Type.LOGIC, Value.FALSE.getType());
    }

    /**
     * Unit test {@link Value#asText()}
     */
    @Test
    public void test_asText() {
        Assert.assertEquals("Unexpected text for text", "123", text.asText());
        Assert.assertEquals("Unexpected text for number", "12.3", number.asText());
        Assert.assertEquals("Unexpected text for logic", "true", logic.asText());
        Assert.assertEquals("Unexpected text for date", "2022-01-02T03:04:05Z", date.asText());

        Assert.assertEquals("Unexpected text for BLANK", "", Value.BLANK.asText());
        Assert.assertEquals("Unexpected text for ZERO", "0", Value.ZERO.asText());
        Assert.assertEquals("Unexpected text for ONE", "1", Value.ONE.asText());
        Assert.assertEquals("Unexpected text for TRUE", "true", Value.TRUE.asText());
        Assert.assertEquals("Unexpected text for FALSE", "false", Value.FALSE.asText());
    }

    /**
     * Unit test {@link Value#asNumber()}
     */
    @Test
    public void test_asNumber() {
        Assert.assertEquals("Unexpected number for text", new BigDecimal("123"), text.asNumber());
        Assert.assertEquals("Unexpected number for number", new BigDecimal("12.3"), number.asNumber());
        Assert.assertEquals("Unexpected number for logic", new BigDecimal("1"), logic.asNumber());
        Assert.assertEquals("Unexpected number for date", new BigDecimal("1641092645"), date.asNumber());

        Assert.assertEquals("Decimal integer", new BigDecimal("1234"), Value.of("1234").asNumber());
        Assert.assertEquals("Hexadecimal integer", new BigDecimal("35243"), Value.of("0x89ab").asNumber());
        Assert.assertEquals("Decimal with a fractional part", new BigDecimal("123.456"), Value.of("123.456").asNumber());
        Assert.assertEquals("Scientific format", new BigDecimal("2.997e8"), Value.of("2.997e8").asNumber());

        Assert.assertEquals("Unexpected number for ZERO", BigDecimal.ZERO, Value.ZERO.asNumber());
        Assert.assertEquals("Unexpected number for ONE", BigDecimal.ONE, Value.ONE.asNumber());
        Assert.assertEquals("Unexpected number for TRUE", BigDecimal.ONE, Value.TRUE.asNumber());
        Assert.assertEquals("Unexpected number for FALSE", BigDecimal.ZERO, Value.FALSE.asNumber());
    }

    /**
     * Unit test {@link Value#asNumber()}
     */
    @Test
    public void test_asNumeric_invalid() {
        Assert.assertThrows("text", EelConvertException.class, () -> Value.of("some Text").asNumber());
        Assert.assertThrows("Bad Hex", EelConvertException.class, () -> Value.of("0x12.34").asNumber());
        Assert.assertThrows("Bad scientific", EelConvertException.class, () -> Value.of("12e3f4").asNumber());
    }


    /**
     * Unit test {@link Value#asBigInteger()}
     */
    @Test
    public void test_asBigInteger() {
        Assert.assertEquals("Unexpected number for text", new BigInteger("123"), text.asBigInteger());
        Assert.assertEquals("Unexpected number for number", new BigInteger("12"), number.asBigInteger());
        Assert.assertEquals("Unexpected number for logic", new BigInteger("1"), logic.asBigInteger());
        Assert.assertEquals("Unexpected number for date", new BigInteger("1641092645"), date.asBigInteger());

        Assert.assertEquals("Unexpected number for ZERO", BigInteger.ZERO, Value.ZERO.asBigInteger());
        Assert.assertEquals("Unexpected number for ONE", BigInteger.ONE, Value.ONE.asBigInteger());
        Assert.assertEquals("Unexpected number for TRUE", BigInteger.ONE, Value.TRUE.asBigInteger());
        Assert.assertEquals("Unexpected number for FALSE", BigInteger.ZERO, Value.FALSE.asBigInteger());
    }

    /**
     * Unit test {@link Value#asInteger()}
     */
    @Test
    public void test_asInteger() {
        Assert.assertEquals("Unexpected number for text", 123, text.asInteger());
        Assert.assertEquals("Unexpected number for number", 12, number.asInteger());
        Assert.assertEquals("Unexpected number for logic", 1, logic.asInteger());
        Assert.assertEquals("Unexpected number for date", 1641092645, date.asInteger());

        Assert.assertEquals("Unexpected number for ZERO", 0, Value.ZERO.asInteger());
        Assert.assertEquals("Unexpected number for ONE", 1, Value.ONE.asInteger());
        Assert.assertEquals("Unexpected number for TRUE", 1, Value.TRUE.asInteger());
        Assert.assertEquals("Unexpected number for FALSE", 0, Value.FALSE.asInteger());
    }


    /**
     * Unit test {@link Value#asLogic()}
     */
    @Test
    public void test_asLogic() {
        ZonedDateTime TRUE_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1_000), ZoneOffset.UTC);

        Assert.assertFalse("Unexpected logic for text", Value.of("False").asLogic());
        Assert.assertFalse("Unexpected logic for number", Value.of(0).asLogic());
        Assert.assertTrue("Unexpected logic for logic", logic.asLogic());

        Assert.assertFalse("Unexpected logic for ZERO", Value.ZERO.asLogic());
        Assert.assertTrue("Unexpected logic for ONE", Value.ONE.asLogic());
        Assert.assertTrue("Unexpected logic for TRUE", Value.TRUE.asLogic());
        Assert.assertFalse("Unexpected logic for FALSE", Value.FALSE.asLogic());

        Assert.assertFalse("negative", Value.of(-0.01).asLogic());
        Assert.assertFalse("Zero", Value.of(0).asLogic());
        Assert.assertTrue("positive", Value.of(+.01).asLogic());

        Assert.assertFalse("False Date", Value.of(EelContext.FALSE_DATE).asLogic());
        Assert.assertFalse("False Date other Zone", Value.of(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic());
        Assert.assertTrue("Other Date", Value.of(TRUE_DATE).asLogic());
        Assert.assertTrue("Other Date", Value.of(TRUE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic());
    }

    /**
     * Unit test {@link Value#asLogic()}
     */
    @Test
    public void test_asLogic_invalid() {
        Assert.assertThrows(EelConvertException.class, () -> Value.of("some Text").asLogic());
    }

    /**
     * Unit test {@link Value#asDate()}
     */
    @Test
    public void test_asDate() {
        Assert.assertEquals("Unexpected text for text", DATE_STAMP0, Value.of("2022-01-02T03:04:05Z").asDate());
        Assert.assertEquals("Unexpected text for number", DATE_STAMP0, Value.of(1641092645).asDate());
        Assert.assertEquals("TRUE", DATE_TRUE, Value.TRUE.asDate());
        Assert.assertEquals("FALSE", EelContext.FALSE_DATE, Value.FALSE.asDate());
    }

    /**
     * Unit test {@link Value#asDate()}
     */
    @Test
    public void test_asDate_invalid() {
        Assert.assertThrows("Bad Text", EelConvertException.class, () -> Value.of("some text").asDate());
    }

    /**
     * Unit test {@link Value#equals(Object)}
     */
    @Test
    public void test_equals() {
        Value sameNumber = Value.of(new BigDecimal("12.300"));

        Assert.assertFalse("text to null", text.equals(null));
        Assert.assertTrue("text to text", text.equals(text));
        Assert.assertFalse("text to String", text.equals("123"));
        Assert.assertTrue("text to same value", text.equals(Value.of("123")));
        Assert.assertFalse("text to different value", text.equals(Value.of("1234")));

        Assert.assertTrue("number to same value", number.equals(Value.of(12.3)));
        Assert.assertFalse("number to different value", number.equals(Value.of(12.34)));
        Assert.assertTrue("number to same value, different precision", number.equals(sameNumber));

        Assert.assertTrue("logic to same value", logic.equals(Value.TRUE));
        Assert.assertFalse("logic to different value", logic.equals(Value.FALSE));

        Assert.assertTrue("date to same value", date.equals(Value.of(DATE_STAMP1)));
        Assert.assertFalse("date to different value", date.equals(Value.of(DATE_STAMP2)));

        Assert.assertFalse("number to text", number.equals(Value.of(number.asText())));
    }


    /**
     * Unit test {@link Value#hashCode()}
     */
    @Test
    public void test_hashCode() {
        Value sameNumber = Value.of(new BigDecimal("12.300"));

        Assert.assertEquals("text to same value", text.hashCode(), Value.of("123").hashCode());
        Assert.assertNotEquals("text to different value", text.hashCode(), Value.of("1234").hashCode());

        Assert.assertEquals("number to same value", number.hashCode(), Value.of(12.3).hashCode());
        Assert.assertNotEquals("number to different value", number.hashCode(), Value.of(-12.34).hashCode());
        Assert.assertEquals("number to same value, different precision", number.hashCode(), sameNumber.hashCode());

        Assert.assertEquals("logic to same value", logic.hashCode(), Value.TRUE.hashCode());
        Assert.assertNotEquals("logic to different value", logic.hashCode(), Value.FALSE.hashCode());

        Assert.assertEquals("date to same value", date.hashCode(), Value.of(DATE_STAMP1).hashCode());
        Assert.assertNotEquals("date to different value", date.hashCode(), Value.of(DATE_STAMP2).hashCode());
    }
}