package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Convert}
 */
public class ConvertTest {
    private static final BigDecimal LARGE_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MASSIVE_VALUE = LARGE_VALUE.multiply(BigDecimal.valueOf(97));
    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final ZoneId UTC_PLUS8 = ZoneOffset.ofHours(8);
    private static final ZonedDateTime DATE_STAMP = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC);
    private static final ZonedDateTime DATE_STAMP_MILLIS = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 600_000_000, UTC);
    private static final ZonedDateTime DATE_STAMP_MICROS = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 600_007_000, UTC);
    private static final ZonedDateTime DATE_STAMP_NANOS = ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 600_007_908, UTC);
    private static final ZonedDateTime DATE_STAMP_EARLY = ZonedDateTime.of(1963, 11, 23, 17, 15, 0, 0, UTC);
    private static final ZonedDateTime DATE_STAMP_EARLY_NANOS = ZonedDateTime.of(1963, 11, 23, 17, 15, 0, 600_700_890, UTC);
    private static final ZonedDateTime DATE_STAMP_BC = ZonedDateTime.of(-1234, 11, 23, 17, 15, 0, 0, UTC);


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
        Assert.assertEquals("Date to Seconds", "2022-01-02T03:04:05Z", Convert.toText(DATE_STAMP));
        Assert.assertEquals("Date to Millis", "2022-01-02T03:04:05.600Z", Convert.toText(DATE_STAMP_MILLIS));
        Assert.assertEquals("Date to Micros", "2022-01-02T03:04:05.600007Z", Convert.toText(DATE_STAMP_MICROS));
        Assert.assertEquals("Date to Nanos", "2022-01-02T03:04:05.600007908Z", Convert.toText(DATE_STAMP_NANOS));
        Assert.assertEquals("Early Date to Seconds", "1963-11-23T17:15:00Z", Convert.toText(DATE_STAMP_EARLY));
        Assert.assertEquals("Early Date to Nano", "1963-11-23T17:15:00.600700890Z", Convert.toText(DATE_STAMP_EARLY_NANOS));
        Assert.assertEquals("BC Date", "-1234-11-23T17:15:00Z", Convert.toText(DATE_STAMP_BC));
    }


    /**
     * Unit test {@link Convert#toLogic(String)}
     */
    @Test
    public void test_toLogic_fromText() {
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
        Assert.assertTrue("Milli-Date", Convert.toLogic(DATE_STAMP_NANOS));
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
    public void test_toNumber_fromText() {
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
        Assert.assertEquals("Date to Seconds", new BigDecimal(1641092645), Convert.toNumber(DATE_STAMP));
        Assert.assertEquals("Date to Millis", new BigDecimal("1641092645.6"), Convert.toNumber(DATE_STAMP_MILLIS));
        Assert.assertEquals("Date to Micros", new BigDecimal("1641092645.600007"), Convert.toNumber(DATE_STAMP_MICROS));
        Assert.assertEquals("Date to Nanos", new BigDecimal("1641092645.600007908"), Convert.toNumber(DATE_STAMP_NANOS));
        Assert.assertEquals("Early Date to Seconds", new BigDecimal("-192696300"), Convert.toNumber(DATE_STAMP_EARLY));
        Assert.assertEquals("Early Date to Nano", new BigDecimal("-192696299.39929911"), Convert.toNumber(DATE_STAMP_EARLY_NANOS));
    }


    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText() {
        Assert.assertEquals("string - short format",
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(1641092645), UTC),
            Convert.toDate("20220102T030405Z"));
        Assert.assertEquals("string - long format",
            ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC),
            Convert.toDate("2022-01-02T03:04:05Z"));
        Assert.assertEquals("string - long format with nanos",
            ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6_007_008, UTC),
            Convert.toDate("2022-01-02T03:04:05.006007008Z"));
        Assert.assertEquals("string - early",
            DATE_STAMP_EARLY,
            Convert.toDate("1963-11-23T17:15:00Z"));
        Assert.assertEquals("string - BC",
            DATE_STAMP_BC,
            Convert.toDate("-1234-11-23T17:15:00Z"));
    }


    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_reducedPrecision() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_008_009, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05:06.700008009+08"));
        Assert.assertEquals("No Nanos",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_008_000, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05:06.700008+08"));
        Assert.assertEquals("No Micros",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05:06.700+08"));
        Assert.assertEquals("No Millis",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05:06+08"));
        Assert.assertEquals("No seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04:05+08"));
        Assert.assertEquals("No minutes",
            ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03T04+08"));
        Assert.assertEquals("No hours",
            ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02-03+08"));
        Assert.assertEquals("No day",
            ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8),
            Convert.toDate("2021-02+08"));
        Assert.assertEquals("No month",
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
    public void test_toDate_fromText_reducedPrecision_compactFormat() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8),
            Convert.toDate("20210203040506.7+08"));
        Assert.assertEquals("No Millis",
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
    public void test_toDate_fromText_spaceSeparator() {
        Assert.assertEquals("Full",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8),
            Convert.toDate("2021 02 03 04 05 06.7 +08"));
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_FractionsOfSeconds() {
        Assert.assertEquals("No Millis",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC),
            Convert.toDate("2021-02-03T04:05:06Z"));
        Assert.assertEquals("1 digit Millis",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 100_000_000, UTC),
            Convert.toDate("2021-02-03T04:05:06.1Z"));
        Assert.assertEquals("2 digit Millis",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 120_000_000, UTC),
            Convert.toDate("2021-02-03T04:05:06.12Z"));
        Assert.assertEquals("3 digit Millis",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 123_000_000, UTC),
            Convert.toDate("2021-02-03T04:05:06.123Z"));
        Assert.assertEquals("lots of fractional seconds",
            ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 123_555_555, UTC),
            Convert.toDate("2021-02-03T04:05:06.123555555Z"));
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_NoLeading0() {
        Assert.assertThrows("string - with separators. No leading zeros",
            DateTimeParseException.class,
            () -> Convert.toDate("2022-1-2T3:4:5Z"));
        Assert.assertThrows("string - with spaces. No leading zeros",
            DateTimeParseException.class,
            () -> Convert.toDate("2022 1 2 3 4 5Z"));
        Assert.assertThrows("string - no separators. No leading zeros",
            DateTimeParseException.class,
            () -> Convert.toDate("202212345Z"));
    }


    /**
     * Unit test {@link Convert#toDate(BigDecimal)}
     */
    @Test
    public void test_toDate_fromNumber() {
        Assert.assertEquals("Date no Millis",
            DATE_STAMP,
            Convert.toDate(new BigDecimal("1641092645")));
        Assert.assertEquals("Date with Millis",
            DATE_STAMP.with(ChronoField.MILLI_OF_SECOND, 600),
            Convert.toDate(new BigDecimal("1641092645.600")));
        Assert.assertEquals("Date with Micros",
            DATE_STAMP.with(ChronoField.MICRO_OF_SECOND, 700),
            Convert.toDate(new BigDecimal("1641092645.00070")));
        Assert.assertEquals("Date with Nanos",
            DATE_STAMP.with(ChronoField.NANO_OF_SECOND, 800),
            Convert.toDate(new BigDecimal("1641092645.0000008")));
        Assert.assertEquals("Early Date no Millis",
            DATE_STAMP_EARLY,
            Convert.toDate(new BigDecimal("-192696300")));
        Assert.assertEquals("Early Date with Millis",
            DATE_STAMP_EARLY_NANOS.with(ChronoField.MILLI_OF_SECOND, 600),         // truncate Micros seconds
            Convert.toDate(new BigDecimal("-192696299.400")));
    }


    /**
     * Test conversions between Dates account for timeZones
     */
    @Test
    public void test_toDate_fromText_Timezone() {
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