package com.github.tymefly.eel.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Convert}
 */
public class ConvertTest {
    private static final BigDecimal MASSIVE_VALUE = BigDecimal.valueOf(Long.MAX_VALUE)
        .multiply(BigDecimal.valueOf(97));
    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final ZoneId UTC_PLUS8 = ZoneOffset.ofHours(8);
    private static final ZonedDateTime DATE_STAMP = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, UTC);


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_String() {
        Assert.assertEquals("boolean", "true", Convert.to(true, String.class));
        Assert.assertEquals("string", "on", Convert.to("on", String.class));
        Assert.assertEquals("byte", "0", Convert.to((byte) 0, String.class));
        Assert.assertEquals("short", "1", Convert.to((short) 1, String.class));
        Assert.assertEquals("integer", "2", Convert.to(2, String.class));
        Assert.assertEquals("long", "3", Convert.to((long) 3, String.class));
        Assert.assertEquals("float", "3.4", Convert.to(3.4f, String.class));
        Assert.assertEquals("double", "-5.6", Convert.to(-5.6, String.class));
        Assert.assertEquals("BigDecimal", "7", Convert.to(BigDecimal.valueOf(7), String.class));
        Assert.assertEquals("BigInteger", "8", Convert.to(BigInteger.valueOf(8), String.class));
        Assert.assertEquals("Date", "2022-01-02T03:04:05Z", Convert.to(DATE_STAMP, String.class));

        // Check we are not using scientific notation
        Assert.assertEquals("Large value", "9223372036854775807", Convert.to(Long.MAX_VALUE, String.class));
        Assert.assertEquals("Massive value", "894667087574913253279", Convert.to(MASSIVE_VALUE, String.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_char() {
        Assert.assertEquals("boolean", 't', (char) Convert.to(true, char.class));
        Assert.assertEquals("string", 'o', (char) Convert.to("on", char.class));
        Assert.assertEquals("byte", '0', (char) Convert.to((byte) 0, char.class));
        Assert.assertEquals("short", '1', (char) Convert.to((short) 1, char.class));
        Assert.assertEquals("integer", '2', (char) Convert.to(2, char.class));
        Assert.assertEquals("long", '3', (char) Convert.to((long) 3, char.class));
        Assert.assertEquals("float", '3', (char) Convert.to(3.4f, char.class));
        Assert.assertEquals("double", '-', (char) Convert.to(-5.6, char.class));
        Assert.assertEquals("BigDecimal", '7', (char) Convert.to(BigDecimal.valueOf(7), char.class));
        Assert.assertEquals("BigInteger", '8', (char) Convert.to(BigInteger.valueOf(8), char.class));
        Assert.assertEquals("Date", '2', (char) Convert.to(DATE_STAMP, char.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_char_Boxed() {
        Assert.assertEquals("boolean", Character.valueOf('t'), Convert.to(true, Character.class));
        Assert.assertEquals("string", Character.valueOf('o'), Convert.to("on", Character.class));
        Assert.assertEquals("byte", Character.valueOf('0'), Convert.to((byte) 0, Character.class));
        Assert.assertEquals("short", Character.valueOf('1'), Convert.to((short) 1, Character.class));
        Assert.assertEquals("integer", Character.valueOf('2'), Convert.to(2, Character.class));
        Assert.assertEquals("long", Character.valueOf('3'), Convert.to((long) 3, Character.class));
        Assert.assertEquals("float", Character.valueOf('3'), Convert.to(3.4f, Character.class));
        Assert.assertEquals("double", Character.valueOf('-'), Convert.to(-5.6, Character.class));
        Assert.assertEquals("BigDecimal", Character.valueOf('7'), Convert.to(BigDecimal.valueOf(7), Character.class));
        Assert.assertEquals("BigInteger", Character.valueOf('8'), Convert.to(BigInteger.valueOf(8), Character.class));
        Assert.assertEquals("Date", Character.valueOf('2'), Convert.to(DATE_STAMP, Character.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Boolean() {
        ZonedDateTime dateMinus1 = EelContext.FALSE_DATE.minusSeconds(1);
        ZonedDateTime datePlus1 = EelContext.FALSE_DATE.plusSeconds(1);

        Assert.assertTrue("boolean", Convert.to(true, Boolean.class));
        Assert.assertFalse("string", Convert.to("0", Boolean.class));
        Assert.assertFalse("byte", Convert.to((byte) 0, Boolean.class));
        Assert.assertTrue("short", Convert.to((short) 1, Boolean.class));
        Assert.assertTrue("integer", Convert.to(2, Boolean.class));
        Assert.assertTrue("long", Convert.to((long) 3, Boolean.class));
        Assert.assertTrue("float", Convert.to(3.4f, Boolean.class));
        Assert.assertFalse("double", Convert.to(0.0, Boolean.class));
        Assert.assertFalse("BigDecimal", Convert.to(new BigDecimal("0.000"), Boolean.class));
        Assert.assertTrue("BigInteger", Convert.to(BigInteger.valueOf(8), Boolean.class));

        Assert.assertFalse("-2", Convert.to(-2, Boolean.class));
        Assert.assertFalse("-1", Convert.to(-1, Boolean.class));
        Assert.assertFalse("0", Convert.to(0, Boolean.class));
        Assert.assertTrue("1", Convert.to(1, Boolean.class));
        Assert.assertTrue("2", Convert.to(3, Boolean.class));

        Assert.assertTrue("Date", Convert.to(DATE_STAMP, Boolean.class));
        Assert.assertFalse("Zero Date", Convert.to(EelContext.FALSE_DATE, Boolean.class));
        Assert.assertFalse("Zero Date in Zone +1",
            Convert.to(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+01")), Boolean.class));
        Assert.assertFalse("Zero Date in Zone -1",
            Convert.to(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("-01")), Boolean.class));

        Assert.assertFalse("-1 Date", Convert.to(dateMinus1, Boolean.class));
        Assert.assertFalse("-1 Date in Zone +1",
            Convert.to(dateMinus1.withZoneSameInstant(ZoneId.of("+01")), Boolean.class));
        Assert.assertFalse("-1 Date in Zone -1",
            Convert.to(dateMinus1.withZoneSameInstant(ZoneId.of("-01")), Boolean.class));
        Assert.assertTrue("+1 Date", Convert.to(datePlus1, Boolean.class));
        Assert.assertTrue("+1 Date in Zone +1",
            Convert.to(datePlus1.withZoneSameInstant(ZoneId.of("+01")), Boolean.class));
        Assert.assertTrue("+1 Date in Zone -1",
            Convert.to(datePlus1.withZoneSameInstant(ZoneId.of("-01")), Boolean.class));

        Assert.assertThrows("bad String", RuntimeException.class, () -> Convert.to("unknown", Boolean.class));
        Assert.assertThrows("blank string", RuntimeException.class, () -> Convert.to("", Boolean.class));
        Assert.assertThrows("0.0", RuntimeException.class, () -> Convert.to("0.0", Boolean.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Byte() {
        Assert.assertEquals("boolean", Byte.valueOf((byte) 1), Convert.to(true, Byte.class));
        Assert.assertEquals("string", Byte.valueOf((byte) 93), Convert.to("93", Byte.class));
        Assert.assertEquals("byte", Byte.valueOf((byte) 0), Convert.to((byte) 0, Byte.class));
        Assert.assertEquals("short", Byte.valueOf((byte) 1), Convert.to((short) 1, Byte.class));
        Assert.assertEquals("integer", Byte.valueOf((byte) 2), Convert.to(2, Byte.class));
        Assert.assertEquals("long", Byte.valueOf((byte) 3), Convert.to((long) 3, Byte.class));
        Assert.assertEquals("float", Byte.valueOf((byte) 3), Convert.to(3.4f, Byte.class));
        Assert.assertEquals("double", Byte.valueOf((byte) -5), Convert.to(-5.6, Byte.class));
        Assert.assertEquals("BigDecimal", Byte.valueOf((byte) 7), Convert.to(BigDecimal.valueOf(7), Byte.class));
        Assert.assertEquals("BigInteger", Byte.valueOf((byte) 8), Convert.to(BigInteger.valueOf(8), Byte.class));
        Assert.assertEquals("Date", Byte.valueOf((byte) 37), Convert.to(DATE_STAMP, Byte.class));

        Assert.assertEquals("String with pad", Byte.valueOf((byte) 72), Convert.to("  72  ", Byte.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Byte.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Short() {
        Assert.assertEquals("boolean", Short.valueOf((short) 1), Convert.to(true, Short.class));
        Assert.assertEquals("string", Short.valueOf((short) 93), Convert.to("93", Short.class));
        Assert.assertEquals("byte", Short.valueOf((short) 0), Convert.to((byte) 0, Short.class));
        Assert.assertEquals("short", Short.valueOf((short) 1), Convert.to((short) 1, Short.class));
        Assert.assertEquals("integer", Short.valueOf((short) 2), Convert.to(2, Short.class));
        Assert.assertEquals("long", Short.valueOf((short) 3), Convert.to((long) 3, Short.class));
        Assert.assertEquals("float", Short.valueOf((short) 3), Convert.to(3.4f, Short.class));
        Assert.assertEquals("double", Short.valueOf((short) -5), Convert.to(-5.6, Short.class));
        Assert.assertEquals("BigDecimal", Short.valueOf((short) 7), Convert.to(BigDecimal.valueOf(7), Short.class));
        Assert.assertEquals("BigInteger", Short.valueOf((short) 8), Convert.to(BigInteger.valueOf(8), Short.class));
        Assert.assertEquals("Date", Short.valueOf((short) 5669), Convert.to(DATE_STAMP, Short.class));

        Assert.assertEquals("String with pad", Short.valueOf((short) 72), Convert.to("  72  ", Short.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Short.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Integer() {
        Assert.assertEquals("boolean", 1, (int) Convert.to(true, Integer.class));
        Assert.assertEquals("string", 93, (int) Convert.to("93", Integer.class));
        Assert.assertEquals("byte", 0, (int) Convert.to((byte) 0, Integer.class));
        Assert.assertEquals("short", 1, (int) Convert.to((short) 1, Integer.class));
        Assert.assertEquals("integer", 2, (int) Convert.to(2, Integer.class));
        Assert.assertEquals("long", 3, (int) Convert.to((long) 3, Integer.class));
        Assert.assertEquals("float", 3, (int) Convert.to(3.4f, Integer.class));
        Assert.assertEquals("double", -5, (int) Convert.to(-5.6, Integer.class));
        Assert.assertEquals("BigDecimal", 7, (int) Convert.to(BigDecimal.valueOf(7), Integer.class));
        Assert.assertEquals("BigInteger", 8, (int) Convert.to(BigInteger.valueOf(8), Integer.class));
        Assert.assertEquals("Date", 1641092645, (int) Convert.to(DATE_STAMP, Integer.class));

        Assert.assertEquals("String with pad", 72, (int) Convert.to("  72  ", Integer.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Integer.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Long() {
        Assert.assertEquals("boolean", Long.valueOf(1), Convert.to(true, Long.class));
        Assert.assertEquals("string", Long.valueOf(93), Convert.to("93", Long.class));
        Assert.assertEquals("byte", Long.valueOf( 0), Convert.to((byte) 0, Long.class));
        Assert.assertEquals("short", Long.valueOf(1), Convert.to((short) 1, Long.class));
        Assert.assertEquals("integer", Long.valueOf(2), Convert.to(2, Long.class));
        Assert.assertEquals("long", Long.valueOf(3), Convert.to((long) 3, Long.class));
        Assert.assertEquals("float", Long.valueOf(3), Convert.to(3.4f, Long.class));
        Assert.assertEquals("double", Long.valueOf(-5), Convert.to(-5.6, Long.class));
        Assert.assertEquals("BigDecimal", Long.valueOf(7), Convert.to(BigDecimal.valueOf(7), Long.class));
        Assert.assertEquals("BigInteger", Long.valueOf(8), Convert.to(BigInteger.valueOf(8), Long.class));
        Assert.assertEquals("Date", Long.valueOf(1641092645), Convert.to(DATE_STAMP, Long.class));

        Assert.assertEquals("String with pad", Long.valueOf(72), Convert.to("  72  ", Long.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Long.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Float() {
        Assert.assertEquals("boolean", Float.valueOf(1f), Convert.to(true, Float.class));
        Assert.assertEquals("string", Float.valueOf(93f), Convert.to("93", Float.class));
        Assert.assertEquals("byte", Float.valueOf(0f), Convert.to((byte) 0, Float.class));
        Assert.assertEquals("short", Float.valueOf(1f), Convert.to((short) 1, Float.class));
        Assert.assertEquals("integer", Float.valueOf(2f), Convert.to(2, Float.class));
        Assert.assertEquals("long", Float.valueOf(3f), Convert.to((long) 3, Float.class));
        Assert.assertEquals("float", Float.valueOf(3.4f), Convert.to(3.4f, Float.class));
        Assert.assertEquals("double", Float.valueOf(-5.6f), Convert.to(-5.6, Float.class));
        Assert.assertEquals("BigDecimal", Float.valueOf(7f), Convert.to(BigDecimal.valueOf(7), Float.class));
        Assert.assertEquals("BigInteger", Float.valueOf(8f), Convert.to(BigInteger.valueOf(8), Float.class));
        Assert.assertEquals("Date", Float.valueOf(1641092645f), Convert.to(DATE_STAMP, Float.class));

        Assert.assertEquals("String with pad", Float.valueOf(72f), Convert.to("  72  ", Float.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Float.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_Double() {
        Assert.assertEquals("boolean", Double.valueOf(1.0), Convert.to(true, Double.class));
        Assert.assertEquals("string", Double.valueOf(93.0), Convert.to("93", Double.class));
        Assert.assertEquals("byte", Double.valueOf(0.0), Convert.to((byte) 0, Double.class));
        Assert.assertEquals("short", Double.valueOf(1.0), Convert.to((short) 1, Double.class));
        Assert.assertEquals("integer", Double.valueOf(2.0), Convert.to(2, Double.class));
        Assert.assertEquals("long", Double.valueOf(3.0), Convert.to((long) 3, Double.class));
        Assert.assertEquals("float", 3.4, Convert.to(3.4f, Double.class), 0.001);
        Assert.assertEquals("double", -5.6, Convert.to(-5.6, Double.class), 0.001);
        Assert.assertEquals("BigDecimal", Double.valueOf(7.0), Convert.to(BigDecimal.valueOf(7), Double.class));
        Assert.assertEquals("BigInteger", Double.valueOf(8.0), Convert.to(BigInteger.valueOf(8), Double.class));
        Assert.assertEquals("Date", Double.valueOf(1641092645.0), Convert.to(DATE_STAMP, Double.class));

        Assert.assertEquals("String with pad", Double.valueOf(72f), Convert.to("  72  ", Double.class));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.to("text", Double.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_BigDecimal() {
        Assert.assertEquals("boolean", new BigDecimal(1), Convert.to(true, BigDecimal.class));
        Assert.assertEquals("string", new BigDecimal(93), Convert.to("93", BigDecimal.class));
        Assert.assertEquals("byte", new BigDecimal(0), Convert.to((byte) 0, BigDecimal.class));
        Assert.assertEquals("short", new BigDecimal(1), Convert.to((short) 1, BigDecimal.class));
        Assert.assertEquals("integer", new BigDecimal(2), Convert.to(2, BigDecimal.class));
        Assert.assertEquals("long", new BigDecimal(3), Convert.to((long) 3, BigDecimal.class));
        Assert.assertEquals("float", new BigDecimal("3.4"), Convert.to(3.4f, BigDecimal.class));
        Assert.assertEquals("double", new BigDecimal("-5.6"), Convert.to(-5.6, BigDecimal.class));
        Assert.assertEquals("BigDecimal", new BigDecimal(7), Convert.to(BigDecimal.valueOf(7), BigDecimal.class));
        Assert.assertEquals("BigInteger", new BigDecimal(8), Convert.to(BigDecimal.valueOf(8), BigDecimal.class));
        Assert.assertEquals("Date", new BigDecimal(1641092645), Convert.to(DATE_STAMP, BigDecimal.class));

        Assert.assertEquals("String with pad", new BigDecimal(72), Convert.to("  72  ", BigDecimal.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_BigInteger() {
        Assert.assertEquals("boolean", new BigInteger("1"), Convert.to(true, BigInteger.class));
        Assert.assertEquals("string", new BigInteger("93"), Convert.to("93", BigInteger.class));
        Assert.assertEquals("byte", new BigInteger("0"), Convert.to((byte) 0, BigInteger.class));
        Assert.assertEquals("short", new BigInteger("1"), Convert.to((short) 1, BigInteger.class));
        Assert.assertEquals("integer", new BigInteger("2"), Convert.to(2, BigInteger.class));
        Assert.assertEquals("long", new BigInteger("3"), Convert.to((long) 3, BigInteger.class));
        Assert.assertEquals("float", new BigInteger("3"), Convert.to(3.4f, BigInteger.class));
        Assert.assertEquals("double", new BigInteger("-5"), Convert.to(-5.6, BigInteger.class));
        Assert.assertEquals("BigDecimal", new BigInteger("7"), Convert.to(BigDecimal.valueOf(7), BigInteger.class));
        Assert.assertEquals("BigInteger", new BigInteger("8"), Convert.to(BigDecimal.valueOf(8), BigInteger.class));
        Assert.assertEquals("Date", new BigInteger("1641092645"), Convert.to(DATE_STAMP, BigInteger.class));

        Assert.assertEquals("String with pad", new BigInteger("72"), Convert.to("  72  ", BigInteger.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Date() {
        Assert.assertThrows("boolean", EelConvertException.class, () -> Convert.to(true, ZonedDateTime.class));
        Assert.assertEquals("string - long format",
            ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC),
            Convert.to("2022-01-02T03:04:05Z", ZonedDateTime.class));
        Assert.assertEquals("string - short format",
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(1641092645), UTC),
            Convert.to("20220102T030405Z", ZonedDateTime.class));
        Assert.assertEquals("byte",
            ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, UTC),
            Convert.to((byte) 0, ZonedDateTime.class));
        Assert.assertEquals("short",
            ZonedDateTime.of(1970, 1, 1, 9, 6, 7, 0, UTC),
            Convert.to(Short.MAX_VALUE, ZonedDateTime.class));
        Assert.assertEquals("integer",
            ZonedDateTime.of(2007, 12, 3, 9, 15, 30, 0, UTC),
            Convert.to(1196673330, ZonedDateTime.class));
        Assert.assertEquals("long",
            ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC),
            Convert.to(1641092645L, ZonedDateTime.class));
        Assert.assertEquals("float",
            ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, UTC),
            Convert.to(946684800.1f, ZonedDateTime.class));
        Assert.assertEquals("double",
            ZonedDateTime.of(2001, 6, 7, 8, 9, 10, 0, UTC),
            Convert.to(991901350.2, ZonedDateTime.class));
        Assert.assertEquals("BigDecimal",
            ZonedDateTime.of(2002, 4, 5, 14, 15, 16, 0, UTC),
            Convert.to(BigDecimal.valueOf(1018016116), ZonedDateTime.class));
        Assert.assertEquals("BigInteger",
            ZonedDateTime.of(2003, 5, 6, 16, 17, 18, 0, UTC),
            Convert.to(BigInteger.valueOf(1052237838), ZonedDateTime.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Date_StringPrecision_fullFormat() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.to("2021-02-03T04:05:06+08", ZonedDateTime.class));
        Assert.assertEquals("no seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8),
            Convert.to("2021-02-03T04:05+08", ZonedDateTime.class));
        Assert.assertEquals("no minutes",
            ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021-02-03T04+08", ZonedDateTime.class));
        Assert.assertEquals("no hours",
            ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021-02-03+08", ZonedDateTime.class));
        Assert.assertEquals("no day",
            ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021-02+08", ZonedDateTime.class));
        Assert.assertEquals("no month",
            ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021+08", ZonedDateTime.class));

        Assert.assertEquals("NoZone",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC),
            Convert.to("2021-02-03T04:05:06", ZonedDateTime.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Date_StringPrecision_compactFormat() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.to("20210203040506+08", ZonedDateTime.class));
        Assert.assertEquals("no seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8),
            Convert.to("202102030405+08", ZonedDateTime.class));
        Assert.assertEquals("no minutes",
            ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021020304+08", ZonedDateTime.class));
        Assert.assertEquals("no hours",
            ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("20210203+08", ZonedDateTime.class));
        Assert.assertEquals("no day",
            ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("202102+08", ZonedDateTime.class));
        Assert.assertEquals("no month",
            ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.to("2021+08", ZonedDateTime.class));

        Assert.assertEquals("NoZone",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC),      // Zone defaulted to UTC
            Convert.to("20210203040506", ZonedDateTime.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Date_SpaceSeparator() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.to("2021 02 03 04 05 06 +08", ZonedDateTime.class));
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     * Test conversions between Dates account for timeZones
     */
    @Test
    public void test_Date_to_Number_Timezone() {
        String dateTime = "2022-01-02T03:04:05";
        ZonedDateTime utc = Convert.to(dateTime + "Z", ZonedDateTime.class);
        long utcUnix = Convert.to(utc, Long.class);
        ZonedDateTime utcPlus1 = Convert.to(dateTime + "+01", ZonedDateTime.class);
        long utcPlus1Unix = Convert.to(utcPlus1, Long.class);

        Assert.assertEquals("Time Zone not accounted for", utcUnix, utcPlus1Unix + 3600);
    }

    /**
     * Unit test {@link Convert#to(Object, Class)}
     * Ensure equal dates are generated from strings and numbers that represent instant
     */
    @Test
    public void test_to_Date_transitive() {
        Assert.assertEquals("same date",
            Convert.to("2022-01-02T03:04:05Z", ZonedDateTime.class),
            Convert.to(1641092645L, ZonedDateTime.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Boxed() {
        Assert.assertEquals("boolean", true, Convert.to("true", boolean.class));
        Assert.assertEquals("byte", (byte) 1, (byte) Convert.to("1", byte.class));
        Assert.assertEquals("short", (short) 2, (short) Convert.to("2", short.class));
        Assert.assertEquals("int", 3, (int) Convert.to("3", int.class));
        Assert.assertEquals("long", 4, (long) Convert.to("4", long.class));
    }


    /**
     * Unit test {@link Convert#to(Object, Class)}
     */
    @Test
    public void test_to_Number_UnsupportedType() {
        Assert.assertThrows("From Number", RuntimeException.class, () -> Convert.to(1, AtomicInteger.class));
        Assert.assertThrows("From String", RuntimeException.class, () -> Convert.to("1", AtomicInteger.class));
    }


    /**
     * Unit test {@link Convert#toChar(EelValue)}
     */
    @Test
    public void test_toChar() {
        Assert.assertEquals("From String of 1 character", 'X', Convert.toChar(EelValue.of("X")));
        Assert.assertEquals("From String with multiple characters", 'a', Convert.toChar(EelValue.of("abc")));
        Assert.assertEquals("From Number", '1', Convert.toChar(EelValue.of(12.34)));
        Assert.assertEquals("From Logic", 't', Convert.toChar(EelValue.of(true)));
        Assert.assertEquals("From Date", '1', Convert.toChar(EelValue.of(EelContext.FALSE_DATE)));

        Assert.assertThrows("Empty String", EelConvertException.class, () -> Convert.toChar(EelValue.of("")));
    }
}