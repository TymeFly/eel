package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Convert}
 */
public class ConvertTest {
    public static final BigDecimal LARGE_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MASSIVE_VALUE = LARGE_VALUE.multiply(BigDecimal.valueOf(97));
    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final ZoneId UTC_PLUS8 = ZoneOffset.ofHours(8);
    private static final ZonedDateTime DATE_STAMP = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6, UTC);


    /**
     * Unit test {@link Convert#toText(BigDecimal)}
     */
    @Test
    public void test_toText_fromNumber() {
        Assert.assertEquals("ZERO", "0", Convert.toText(BigDecimal.ZERO));
        Assert.assertEquals("ONE", "1", Convert.toText(BigDecimal.ONE));
        Assert.assertEquals("TEN", "10", Convert.toText(BigDecimal.TEN));

        Assert.assertEquals("negative", "-1", Convert.toText(new BigDecimal("-1")));
        Assert.assertEquals("fractional", "-1.5", Convert.toText(new BigDecimal("-1.5")));

        // Check we are not using scientific notation
        Assert.assertEquals("Large value", "9223372036854775807", Convert.toText(LARGE_VALUE));
        Assert.assertEquals("Massive value", "894667087574913253279", Convert.toText(MASSIVE_VALUE));
    }

    /**
     * Unit test {@link Convert#toText(ZonedDateTime)}
     */
    @Test
    public void test_toText_fromDate() {
        Assert.assertEquals("Date", "2022-01-02T03:04:05Z", Convert.toText(DATE_STAMP));
    }


    /**
     * Unit test {@link Convert#toLogic(String)}
     */
    @Test
    public void test_toLogic_fromString() {
        Assert.assertFalse("0", Convert.toLogic("0"));
        Assert.assertFalse("false", Convert.toLogic("false"));
        Assert.assertFalse("False", Convert.toLogic("False"));
        Assert.assertFalse("FALSE", Convert.toLogic("FALSE"));
        Assert.assertFalse("Empty string", Convert.toLogic(""));

        Assert.assertTrue("1", Convert.toLogic("1"));
        Assert.assertTrue("true", Convert.toLogic("true"));
        Assert.assertTrue("True", Convert.toLogic("True"));
        Assert.assertTrue("TRUE", Convert.toLogic("TRUE"));

        Assert.assertThrows("bad String", RuntimeException.class, () -> Convert.toLogic("unknown"));
        Assert.assertThrows("-1", RuntimeException.class, () -> Convert.toLogic("-1"));
        Assert.assertThrows("0.0", RuntimeException.class, () -> Convert.toLogic("0.0"));
    }


    /**
     * Unit test {@link Convert#toLogic(BigDecimal)}
     */
    @Test
    public void test_toLogic_fromNumber() {
        Assert.assertFalse("ZERO", Convert.toLogic(BigDecimal.ZERO));
        Assert.assertTrue("ONE", Convert.toLogic(BigDecimal.ONE));

        Assert.assertFalse("0.000", Convert.toLogic(new BigDecimal("0.000")));
        Assert.assertTrue("1.000", Convert.toLogic(new BigDecimal("1.000")));

        Assert.assertFalse("-2", Convert.toLogic(BigDecimal.valueOf(-2)));
        Assert.assertFalse("-1", Convert.toLogic(BigDecimal.valueOf(-1)));
        Assert.assertFalse("0", Convert.toLogic(BigDecimal.valueOf(0)));
        Assert.assertTrue("1", Convert.toLogic(BigDecimal.valueOf(1)));
        Assert.assertTrue("2", Convert.toLogic(BigDecimal.valueOf(3)));
    }


    /**
     * Unit test {@link Convert#toLogic(ZonedDateTime)}
     */
    @Test
    public void test_toLogic_fromDate() {
        ZonedDateTime dateMinus1 = EelContext.FALSE_DATE.minusSeconds(1);
        ZonedDateTime datePlus1 = EelContext.FALSE_DATE.plusSeconds(1);

        Assert.assertTrue("Date", Convert.toLogic(DATE_STAMP));
        Assert.assertFalse("Zero Date", Convert.toLogic(EelContext.FALSE_DATE));
        Assert.assertFalse("Zero Date in Zone +1", Convert.toLogic(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+01"))));
        Assert.assertFalse("Zero Date in Zone -1", Convert.toLogic(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("-01"))));

        Assert.assertFalse("-1 Date", Convert.toLogic(dateMinus1));
        Assert.assertFalse("-1 Date in Zone +1", Convert.toLogic(dateMinus1.withZoneSameInstant(ZoneId.of("+01"))));
        Assert.assertFalse("-1 Date in Zone -1", Convert.toLogic(dateMinus1.withZoneSameInstant(ZoneId.of("-01"))));
        Assert.assertTrue("+1 Date", Convert.toLogic(datePlus1));
        Assert.assertTrue("+1 Date in Zone +1", Convert.toLogic(datePlus1.withZoneSameInstant(ZoneId.of("+01"))));
        Assert.assertTrue("+1 Date in Zone -1", Convert.toLogic(datePlus1.withZoneSameInstant(ZoneId.of("-01"))));
    }

    /**
     * Unit test {@link Convert#toNumber(String)}
     */
    @Test
    public void test_toNumber_fromString() {
        Assert.assertEquals("string", new BigDecimal(93), Convert.toNumber("93"));
        Assert.assertEquals("String with pad", new BigDecimal(72), Convert.toNumber("  72  "));
        Assert.assertEquals("Hex String",new BigDecimal(18), Convert.toNumber("0x12"));
        Assert.assertEquals("with _", new BigDecimal(123), Convert.toNumber("1_23"));

        Assert.assertThrows("with leading _", EelConvertException.class, () -> Convert.toNumber("_23"));
        Assert.assertThrows("Hex with leading _", EelConvertException.class, () -> Convert.toNumber("0x_23"));

        Assert.assertThrows("text", RuntimeException.class, () -> Convert.toNumber("text"));
    }


    /**
     * Unit test {@link Convert#toNumber(ZonedDateTime)}
     */
    @Test
    public void test_toNumber_fromDate() {
        Assert.assertEquals("Date", new BigDecimal(1641092645), Convert.toNumber(DATE_STAMP));
    }


    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromString() {
        Assert.assertEquals("string - long format",
            ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC),
            Convert.toDate("2022-01-02T03:04:05Z"));
        Assert.assertEquals("string - short format",
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(1641092645), UTC),
            Convert.toDate("20220102T030405Z"));
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromString_reducedPrecision() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05:06+08"));
        Assert.assertEquals("no seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05+08"));
        Assert.assertEquals("no minutes",
            ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04+08"));
        Assert.assertEquals("no hours",
            ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03+08"));
        Assert.assertEquals("no day",
            ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02+08"));
        Assert.assertEquals("no month",
            ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021+08"));

        Assert.assertEquals("NoZone",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC),
            Convert.toDate("2021-02-03T04:05:06"));
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromString_reducedPrecision_compactFormat() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.toDate("20210203040506+08"));
        Assert.assertEquals("no seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8),
            Convert.toDate("202102030405+08"));
        Assert.assertEquals("no minutes",
            ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021020304+08"));
        Assert.assertEquals("no hours",
            ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("20210203+08"));
        Assert.assertEquals("no day",
            ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("202102+08"));
        Assert.assertEquals("no month",
            ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021+08"));

        Assert.assertEquals("NoZone",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC),      // Zone defaulted to UTC
            Convert.toDate("20210203040506"));
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromString_spaceSeparator() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.toDate("2021 02 03 04 05 06 +08"));
    }


    /**
     * Unit test {@link Convert#toDate(BigDecimal)}
     */
    @Test
    public void test_toDate_fromNumber() {
        Assert.assertEquals("BigDecimal",
            ZonedDateTime.of(2002, 4, 5, 14, 15, 16, 0, UTC),
            Convert.toDate(BigDecimal.valueOf(1018016116)));
    }


    /**
     * Test conversions between Dates account for timeZones
     */
    @Test
    public void test_toDate_fromString_Timezone() {
        String dateTime = "2022-01-02T03:04:05";
        ZonedDateTime utc = Convert.toDate(dateTime + "Z");
        BigDecimal utcUnix = Convert.toNumber(utc);
        ZonedDateTime utcPlus1 = Convert.toDate(dateTime + "+01");
        BigDecimal utcPlus1Unix = Convert.toNumber(utcPlus1);

        Assert.assertEquals("Time Zone not accounted for", utcUnix.longValue(), utcPlus1Unix.longValue() + 3600);
    }


    /**
     * Ensure equal dates are generated from strings and numbers that represent the same instant
     */
    @Test
    public void test_to_Date_transitive() {
        Assert.assertEquals("same date",
            Convert.toDate("2022-01-02T03:04:05Z"),
            Convert.toDate(BigDecimal.valueOf(1641092645L)));
    }
}