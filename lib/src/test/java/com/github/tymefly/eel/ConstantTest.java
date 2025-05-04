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
        Assert.assertSame("Empty String", Value.BLANK, Constant.of(""));

        Assert.assertNotSame("Blank String", Value.BLANK, Constant.of(" "));
        Assert.assertNotSame("String with text", Value.BLANK, Constant.of("SomeValue"));

        String stamp = Long.toString(System.currentTimeMillis());

        Assert.assertSame("Check Cache", Constant.of(stamp), Constant.of(stamp));
    }

    /**
     * Unit test {@link Constant#of(Number)}
     */
    @Test
    public void test_of_Number() {
        Assert.assertEquals("Int(0)", Value.ZERO, Constant.of(0));
        Assert.assertEquals("Int(1)", Value.ONE, Constant.of(1));
        Assert.assertEquals("Long(1)", Value.ONE, Constant.of(1L));
        Assert.assertEquals("BigDecimal(1)", Value.ONE, Constant.of(BigDecimal.ONE));
        Assert.assertEquals("Double(0.0)", Value.ZERO, Constant.of(0.0));

        Long stamp = System.currentTimeMillis();

        Assert.assertSame("Check Cache", Constant.of(stamp), Constant.of(stamp));
    }

    /**
     * Unit test {@link Constant#of(boolean)}
     */
    @Test
    public void test_of_Bool() {
        Assert.assertSame("false", Value.FALSE, Constant.of(false));
        Assert.assertSame("Boolean.FALSE", Value.FALSE, Constant.of(Boolean.FALSE));

        Assert.assertSame("true", Value.TRUE, Constant.of(true));
        Assert.assertSame("Boolean.TRUE", Value.TRUE, Constant.of(Boolean.TRUE));
    }

    /**
     * Unit test {@link Constant#of(ZonedDateTime)}
     */
    @Test
    public void test_of_Date() {
        Assert.assertEquals("date", date, Constant.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 678_000_009, ZoneOffset.UTC)));

        Assert.assertNotSame("other year", date, Constant.of(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 6, ZoneOffset.UTC)));
        Assert.assertNotSame("other zone", date, Constant.of(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, ZoneOffset.ofHours(8))));

        ZonedDateTime stamp = ZonedDateTime.now();

        Assert.assertSame("Check Cache", Constant.of(stamp), Constant.of(stamp));
    }

    /**
     * Unit test {@link Constant#getType()}
     */
    @Test
    public void test_getType() {
        Assert.assertEquals("Unexpected type for text", Type.TEXT, text.getType());
        Assert.assertEquals("Unexpected type for number", Type.NUMBER, number.getType());
        Assert.assertEquals("Unexpected type for logic", Type.LOGIC, logic.getType());
        Assert.assertEquals("Unexpected type for date", Type.DATE, date.getType());
    }

    /**
     * Unit test {@link Constant#asText()}
     */
    @Test
    public void test_asText() {
        Assert.assertEquals("Unexpected text for text", "123", text.asText());
        Assert.assertEquals("Unexpected text for number", "12.3", number.asText());
        Assert.assertEquals("Unexpected text for logic", "true", logic.asText());
        Assert.assertEquals("Unexpected text for date", "2022-01-02T03:04:05.678000009Z", date.asText());
    }

    /**
     * Unit test {@link Constant#asNumber()}
     */
    @Test
    public void test_asNumber() {
        Assert.assertEquals("Unexpected number for text", new BigDecimal("123"), text.asNumber());
        Assert.assertEquals("Unexpected number for number", new BigDecimal("12.3"), number.asNumber());
        Assert.assertEquals("Unexpected number for logic", new BigDecimal("1"), logic.asNumber());
        Assert.assertEquals("Unexpected number for date", new BigDecimal("1641092645.678000009"), date.asNumber());

        Assert.assertEquals("Decimal integer", new BigDecimal("1234"), Constant.of("1234").asNumber());
        Assert.assertEquals("Hexadecimal integer", new BigDecimal("35243"), Constant.of("0x89ab").asNumber());
        Assert.assertEquals("Decimal with a fractional part", new BigDecimal("123.456"), Constant.of("123.456").asNumber());
        Assert.assertEquals("Scientific format", new BigDecimal("2.997e8"), Constant.of("2.997e8").asNumber());
    }

    /**
     * Unit test {@link Constant#asNumber()}
     */
    @Test
    public void test_asNumeric_invalid() {
        Assert.assertThrows("text", EelConvertException.class, () -> Constant.of("some Text").asNumber());
        Assert.assertThrows("Bad Hex", EelConvertException.class, () -> Constant.of("0x12.34").asNumber());
        Assert.assertThrows("Bad scientific", EelConvertException.class, () -> Constant.of("12e3f4").asNumber());
    }


    /**
     * Unit test {@link Constant#asLogic()}
     */
    @Test
    public void test_asLogic() {
        ZonedDateTime TRUE_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1_000), ZoneOffset.UTC);

        Assert.assertFalse("Unexpected logic for text", Constant.of("False").asLogic());
        Assert.assertFalse("Unexpected logic for number", Constant.of(0).asLogic());
        Assert.assertTrue("Unexpected logic for logic", logic.asLogic());

        Assert.assertFalse("negative", Constant.of(-0.01).asLogic());
        Assert.assertFalse("Zero", Constant.of(0).asLogic());
        Assert.assertTrue("positive", Constant.of(+.01).asLogic());

        Assert.assertFalse("False Date", Constant.of(EelContext.FALSE_DATE).asLogic());
        Assert.assertFalse("False Date other Zone", Constant.of(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic());
        Assert.assertTrue("Other Date", Constant.of(TRUE_DATE).asLogic());
        Assert.assertTrue("Other Date", Constant.of(TRUE_DATE.withZoneSameInstant(ZoneId.of("+2"))).asLogic());
    }

    /**
     * Unit test {@link Constant#asLogic()}
     */
    @Test
    public void test_asLogic_invalid() {
        Assert.assertThrows(EelConvertException.class, () -> Constant.of("some Text").asLogic());
    }

    /**
     * Unit test {@link Constant#asDate()}
     */
    @Test
    public void test_asDate() {
        Assert.assertEquals("Unexpected text for text", DATE_STAMP0, Constant.of("2022-01-02T03:04:05Z").asDate());
        Assert.assertEquals("Unexpected text for number", DATE_STAMP0, Constant.of(1641092645).asDate());
        Assert.assertEquals("TRUE", DATE_TRUE, Constant.of(true).asDate());
        Assert.assertEquals("FALSE", EelContext.FALSE_DATE, Constant.of(false).asDate());
    }

    /**
     * Unit test {@link Constant#asDate()}
     */
    @Test
    public void test_asDate_invalid() {
        Assert.assertThrows("Bad Text", EelConvertException.class, () -> Constant.of("some text").asDate());
    }


    /**
     * Unit test {@link Constant#equals(Object)}
     */
    @Test
    public void test_equals() {
        Constant sameNumber = Constant.of(new BigDecimal("12.300"));

        Assert.assertFalse("text to null", text.equals(null));
        Assert.assertTrue("text to text", text.equals(text));
        Assert.assertFalse("text to String", text.equals("123"));
        Assert.assertTrue("text to same value", text.equals(Constant.of("123")));
        Assert.assertFalse("text to different value", text.equals(Constant.of("1234")));

        Assert.assertTrue("number to same value", number.equals(Constant.of(12.3)));
        Assert.assertFalse("number to different value", number.equals(Constant.of(12.34)));
        Assert.assertTrue("number to same value, different precision", number.equals(sameNumber));

        Assert.assertTrue("logic to same value", logic.equals(Constant.of(true)));
        Assert.assertFalse("logic to different value", logic.equals(Constant.of(false)));

        Assert.assertTrue("date to same value", date.equals(Constant.of(DATE_STAMP1)));
        Assert.assertFalse("date to different value", date.equals(Constant.of(DATE_STAMP2)));

        Assert.assertFalse("number to text", number.equals(Constant.of(number.asText())));
    }


    /**
     * Unit test {@link Constant#hashCode()}
     */
    @Test
    public void test_hashCode() {
        Constant sameNumber = Constant.of(new BigDecimal("12.300"));

        Assert.assertEquals("text to same value", text.hashCode(), Constant.of("123").hashCode());
        Assert.assertNotEquals("text to different value", text.hashCode(), Constant.of("1234").hashCode());

        Assert.assertEquals("number to same value", number.hashCode(), Constant.of(12.3).hashCode());
        Assert.assertNotEquals("number to different value", number.hashCode(), Constant.of(-12.34).hashCode());
        Assert.assertNotEquals("number to same value, different precision", number.hashCode(), sameNumber.hashCode());

        Assert.assertEquals("logic to same value", logic.hashCode(), Constant.of(true).hashCode());
        Assert.assertNotEquals("logic to different value", logic.hashCode(), Constant.of(false).hashCode());

        Assert.assertEquals("date to same value", date.hashCode(), Constant.of(DATE_STAMP1).hashCode());
        Assert.assertNotEquals("date to different value", date.hashCode(), Constant.of(DATE_STAMP2).hashCode());
    }

    /**
     * Unit test {@link AbstractValue#asBigInteger}
     */
    @Test
    public void test_asBigInteger() {
        Assert.assertEquals("Unexpected number for text", new BigInteger("123"), text.asBigInteger());
        Assert.assertEquals("Unexpected number for number", new BigInteger("12"), number.asBigInteger());
        Assert.assertEquals("Unexpected number for logic", new BigInteger("1"), logic.asBigInteger());
        Assert.assertEquals("Unexpected number for date", new BigInteger("1641092645"), date.asBigInteger());
    }

    /**
     * Unit test {@link AbstractValue#asDouble}
     */
    @Test
    public void test_asDouble() {
        Assert.assertEquals("Unexpected number for text", 123.0, text.asDouble(), 0.001);
        Assert.assertEquals("Unexpected number for number", 12.3, number.asDouble(), 0.001);
        Assert.assertEquals("Unexpected number for logic", 1.0, logic.asDouble(), 0.001);
        Assert.assertEquals("Unexpected number for date", 1641092645.678, date.asDouble(), 0.001);
    }

    /**
     * Unit test {@link AbstractValue#asLong}
     */
    @Test
    public void test_asLong() {
        Assert.assertEquals("Unexpected number for text", 123L, text.asLong());
        Assert.assertEquals("Unexpected number for number", 12L, number.asLong());
        Assert.assertEquals("Unexpected number for logic", 1L, logic.asLong());
        Assert.assertEquals("Unexpected number for date", 1641092645L, date.asLong());
    }

    /**
     * Unit test {@link AbstractValue#asInt}
     */
    @Test
    public void test_asInt() {
        Assert.assertEquals("Unexpected number for text", 123, text.asInt());
        Assert.assertEquals("Unexpected number for number", 12, number.asInt());
        Assert.assertEquals("Unexpected number for logic", 1, logic.asInt());
        Assert.assertEquals("Unexpected number for date", 1641092645, date.asInt());
    }

    /**
     * Unit test {@link Constant#asChar()}
     */
    @Test
    public void test_asChar() {
        Assert.assertEquals("From String of 1 character", 'X', Constant.of("X").asChar());
        Assert.assertEquals("From String with multiple characters", 'a', Constant.of("abc").asChar());
        Assert.assertEquals("From Number", '1', Constant.of(12.34).asChar());
        Assert.assertEquals("From Logic", 't', Constant.of(true).asChar());
        Assert.assertEquals("From Date", '1', Constant.of(EelContext.FALSE_DATE).asChar());

        Assert.assertThrows("Empty String", EelConvertException.class, () -> Constant.of("").asChar());
    }

    /**
     * Unit test {@link Constant#asFile()}
     */
    @Test
    public void test_asFile() throws Exception {
        Assert.assertEquals("Happy path", "123", text.asFile().getName());
    }
}