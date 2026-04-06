package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("0", Convert.toText(BigDecimal.ZERO), "ZERO");
        assertEquals("1", Convert.toText(BigDecimal.ONE), "ONE");
        assertEquals("10", Convert.toText(BigDecimal.TEN), "TEN");

        assertEquals("-1", Convert.toText(new BigDecimal("-1")), "negative");
        assertEquals("-1.5", Convert.toText(new BigDecimal("-1.5")), "fractional");

        // Check we are not using scientific notation
        assertEquals("9223372036854775807", Convert.toText(LARGE_VALUE), "Large value");
        assertEquals("894667087574913253279", Convert.toText(MASSIVE_VALUE), "Massive value");
    }

    /**
     * Unit test {@link Convert#toText(ZonedDateTime)}
     */
    @Test
    public void test_toText_fromDate() {
        assertEquals("2022-01-02T03:04:05Z", Convert.toText(DATE_STAMP), "Date to Seconds");
        assertEquals("2022-01-02T03:04:05.600Z", Convert.toText(DATE_STAMP_MILLIS), "Date to Millis");
        assertEquals("2022-01-02T03:04:05.600007Z", Convert.toText(DATE_STAMP_MICROS), "Date to Micros");
        assertEquals("2022-01-02T03:04:05.600007908Z", Convert.toText(DATE_STAMP_NANOS), "Date to Nanos");
        assertEquals("1963-11-23T17:15:00Z", Convert.toText(DATE_STAMP_EARLY), "Early Date to Seconds");
        assertEquals("1963-11-23T17:15:00.600700890Z", Convert.toText(DATE_STAMP_EARLY_NANOS), "Early Date to Nano");
        assertEquals("-1234-11-23T17:15:00Z", Convert.toText(DATE_STAMP_BC), "BC Date");
    }


    /**
     * Unit test {@link Convert#toLogic(String)}
     */
    @Test
    public void test_toLogic_fromText() {
        assertFalse(Convert.toLogic("0"), "0");
        assertFalse(Convert.toLogic("false"), "false");
        assertFalse(Convert.toLogic("False"), "False");
        assertFalse(Convert.toLogic("FALSE"), "FALSE");
        assertFalse(Convert.toLogic(""), "Empty string");

        assertTrue(Convert.toLogic("1"), "1");
        assertTrue(Convert.toLogic("true"), "true");
        assertTrue(Convert.toLogic("True"), "True");
        assertTrue(Convert.toLogic("TRUE"), "TRUE");

        assertThrows(RuntimeException.class, () -> Convert.toLogic("unknown"), "bad String");
        assertThrows(RuntimeException.class, () -> Convert.toLogic("-1"), "-1");
        assertThrows(RuntimeException.class, () -> Convert.toLogic("0.0"), "0.0");
    }


    /**
     * Unit test {@link Convert#toLogic(BigDecimal)}
     */
    @Test
    public void test_toLogic_fromNumber() {
        assertFalse(Convert.toLogic(BigDecimal.ZERO), "ZERO");
        assertTrue(Convert.toLogic(BigDecimal.ONE), "ONE");

        assertFalse(Convert.toLogic(new BigDecimal("0.000")), "0.000");
        assertTrue(Convert.toLogic(new BigDecimal("1.000")), "1.000");

        assertFalse(Convert.toLogic(BigDecimal.valueOf(-2)), "-2");
        assertFalse(Convert.toLogic(BigDecimal.valueOf(-1)), "-1");
        assertFalse(Convert.toLogic(BigDecimal.valueOf(0)), "0");
        assertTrue(Convert.toLogic(BigDecimal.valueOf(1)), "1");
        assertTrue(Convert.toLogic(BigDecimal.valueOf(3)), "2");
    }


    /**
     * Unit test {@link Convert#toLogic(ZonedDateTime)}
     */
    @Test
    public void test_toLogic_fromDate() {
        ZonedDateTime dateMinus1 = EelContext.FALSE_DATE.minusSeconds(1);
        ZonedDateTime datePlus1 = EelContext.FALSE_DATE.plusSeconds(1);

        assertTrue(Convert.toLogic(DATE_STAMP), "Date");
        assertTrue(Convert.toLogic(DATE_STAMP_NANOS), "Milli-Date");
        assertFalse(Convert.toLogic(EelContext.FALSE_DATE), "Zero Date");
        assertFalse(Convert.toLogic(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("+01"))), "Zero Date in Zone +1");
        assertFalse(Convert.toLogic(EelContext.FALSE_DATE.withZoneSameInstant(ZoneId.of("-01"))), "Zero Date in Zone -1");

        assertFalse(Convert.toLogic(dateMinus1), "-1 Date");
        assertFalse(Convert.toLogic(dateMinus1.withZoneSameInstant(ZoneId.of("+01"))), "-1 Date in Zone +1");
        assertFalse(Convert.toLogic(dateMinus1.withZoneSameInstant(ZoneId.of("-01"))), "-1 Date in Zone -1");
        assertTrue(Convert.toLogic(datePlus1), "+1 Date");
        assertTrue(Convert.toLogic(datePlus1.withZoneSameInstant(ZoneId.of("+01"))), "+1 Date in Zone +1");
        assertTrue(Convert.toLogic(datePlus1.withZoneSameInstant(ZoneId.of("-01"))), "+1 Date in Zone -1");
    }

    /**
     * Unit test {@link Convert#toNumber(String)}
     */
    @Test
    public void test_toNumber_fromText() {
        assertEquals(new BigDecimal(93), Convert.toNumber("93"), "string");
        assertEquals(new BigDecimal(72), Convert.toNumber("  72  "), "String with pad");
        assertEquals(new BigDecimal(18), Convert.toNumber("0x12"), "Hex String");
        assertEquals(new BigDecimal(123), Convert.toNumber("1_23"), "with _");

        assertThrows(EelConvertException.class, () -> Convert.toNumber("_23"), "with leading _");
        assertThrows(EelConvertException.class, () -> Convert.toNumber("0x_23"), "Hex with leading _");

        assertThrows(RuntimeException.class, () -> Convert.toNumber("text"), "text");
    }


    /**
     * Unit test {@link Convert#toNumber(ZonedDateTime)}
     */
    @Test
    public void test_toNumber_fromDate() {
        assertEquals(new BigDecimal(1641092645), Convert.toNumber(DATE_STAMP), "Date to Seconds");
        assertEquals(new BigDecimal("1641092645.6"), Convert.toNumber(DATE_STAMP_MILLIS), "Date to Millis");
        assertEquals(new BigDecimal("1641092645.600007"), Convert.toNumber(DATE_STAMP_MICROS), "Date to Micros");
        assertEquals(new BigDecimal("1641092645.600007908"), Convert.toNumber(DATE_STAMP_NANOS), "Date to Nanos");
        assertEquals(new BigDecimal("-192696300"), Convert.toNumber(DATE_STAMP_EARLY), "Early Date to Seconds");
        assertEquals(new BigDecimal("-192696299.39929911"), Convert.toNumber(DATE_STAMP_EARLY_NANOS), "Early Date to Nano");
    }


    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText() {
        assertEquals(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1641092645), UTC), Convert.toDate("20220102T030405Z"), "string - short format");
        assertEquals(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 0, UTC), Convert.toDate("2022-01-02T03:04:05Z"), "string - long format");
        assertEquals(ZonedDateTime.of(2022, 1, 2, 3, 4, 5, 6_007_008, UTC), Convert.toDate("2022-01-02T03:04:05.006007008Z"), "string - long format with nanos");
        assertEquals(DATE_STAMP_EARLY, Convert.toDate("1963-11-23T17:15:00Z"), "string - early");
        assertEquals(DATE_STAMP_BC, Convert.toDate("-1234-11-23T17:15:00Z"), "string - BC");
    }


    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_reducedPrecision() {
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_008_009, UTC_PLUS8), Convert.toDate("2021-02-03T04:05:06.700008009+08"), "Full");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_008_000, UTC_PLUS8), Convert.toDate("2021-02-03T04:05:06.700008+08"), "No Nanos");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8), Convert.toDate("2021-02-03T04:05:06.700+08"), "No Micros");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8), Convert.toDate("2021-02-03T04:05:06+08"), "No Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8), Convert.toDate("2021-02-03T04:05+08"), "No seconds");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021-02-03T04+08"), "No minutes");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021-02-03+08"), "No hours");
        assertEquals(ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021-02+08"), "No day");
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021+08"), "No month");

        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC), Convert.toDate("2021-02-03T04:05:06"), "NoZone");
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_reducedPrecision_compactFormat() {
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8), Convert.toDate("20210203040506.7+08"), "Full");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC_PLUS8), Convert.toDate("20210203040506+08"), "No Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 0, 0, UTC_PLUS8), Convert.toDate("202102030405+08"), "no seconds");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021020304+08"), "no minutes");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("20210203+08"), "no hours");
        assertEquals(ZonedDateTime.of(2021, 2, 1, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("202102+08"), "no day");
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC_PLUS8), Convert.toDate("2021+08"), "no month");

        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC), Convert.toDate("20210203040506"), "NoZone");
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_spaceSeparator() {
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 700_000_000, UTC_PLUS8), Convert.toDate("2021 02 03 04 05 06.7 +08"), "Full");
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_FractionsOfSeconds() {
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 0, UTC), Convert.toDate("2021-02-03T04:05:06Z"), "No Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 100_000_000, UTC), Convert.toDate("2021-02-03T04:05:06.1Z"), "1 digit Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 120_000_000, UTC), Convert.toDate("2021-02-03T04:05:06.12Z"), "2 digit Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 123_000_000, UTC), Convert.toDate("2021-02-03T04:05:06.123Z"), "3 digit Millis");
        assertEquals(ZonedDateTime.of(2021, 2, 3, 4, 5, 6, 123_555_555, UTC), Convert.toDate("2021-02-03T04:05:06.123555555Z"), "lots of fractional seconds");
    }

    /**
     * Unit test {@link Convert#toDate(String)}
     */
    @Test
    public void test_toDate_fromText_NoLeading0() {
        assertThrows(DateTimeParseException.class,
            () -> Convert.toDate("2022-1-2T3:4:5Z"),
            "string - with separators. No leading zeros");
        assertThrows(DateTimeParseException.class,
            () -> Convert.toDate("2022 1 2 3 4 5Z"),
            "string - with spaces. No leading zeros");
        assertThrows(DateTimeParseException.class,
            () -> Convert.toDate("202212345Z"),
            "string - no separators. No leading zeros");
    }


    /**
     * Unit test {@link Convert#toDate(BigDecimal)}
     */
    @Test
    public void test_toDate_fromNumber() {
        assertEquals(DATE_STAMP, Convert.toDate(new BigDecimal("1641092645")), "Date no Millis");
        assertEquals(DATE_STAMP.with(ChronoField.MILLI_OF_SECOND, 600), Convert.toDate(new BigDecimal("1641092645.600")), "Date with Millis");
        assertEquals(DATE_STAMP.with(ChronoField.MICRO_OF_SECOND, 700), Convert.toDate(new BigDecimal("1641092645.00070")), "Date with Micros");
        assertEquals(DATE_STAMP.withNano(800), Convert.toDate(new BigDecimal("1641092645.0000008")), "Date with Nanos");
        assertEquals(DATE_STAMP_EARLY, Convert.toDate(new BigDecimal("-192696300")), "Early Date no Millis");
        assertEquals(DATE_STAMP_EARLY_NANOS.with(ChronoField.MILLI_OF_SECOND, 600), Convert.toDate(new BigDecimal("-192696299.400")), "Early Date with Millis");
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

        assertEquals(utcUnix.longValue(), utcPlus1Unix.longValue() + 3600, "Time Zone not accounted for");
    }


    /**
     * Ensure equal dates are generated from strings and numbers that represent the same instant
     */
    @Test
    public void test_to_Date_transitive() {
        assertEquals(Convert.toDate("2022-01-02T03:04:05Z"), Convert.toDate(BigDecimal.valueOf(1641092645L)), "same date");
    }
}