package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link DateFactory}
 */
public class DateFactoryTest {
    private static final long TOLERANCE = 100;

    private static final TimeZone LOCAL_TIME_ZONE = TimeZone.getDefault();

    private EelContext context;
    private ZonedDateTime now;
    private ZonedDateTime date;


    @BeforeEach
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));      // Something that isn't UTC

        context = spy(EelContext.factory().build());
        now = ZonedDateTime.now(ZoneId.of("UTC"));
        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);

        when(context.getStartTime())
            .thenReturn(date);
        when(context.getWeek())
            .thenReturn(WeekFields.of(DayOfWeek.SUNDAY, 1));
    }


    /** Restore the time zone */
    @AfterEach
    public void tearDown() {
        TimeZone.setDefault(LOCAL_TIME_ZONE);
    }



    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_fullDateWithNoTime() {
        ZonedDateTime actual = new DateFactory().parse(context, "dd/MM/yyyy", "03/02/2001");

        assertEquals(2001, actual.getYear(), "Year failed");
        assertEquals(2, actual.getMonthValue(), "Month failed");
        assertEquals(3, actual.getDayOfMonth(), "Day failed");
        assertEquals(0, actual.getHour(), "Hour failed");
        assertEquals(0, actual.getMinute(), "Minute failed");
        assertEquals(0, actual.getSecond(), "Second failed");
        assertEquals(0, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_fullDateWithNoTimeAndOffset() {
        ZonedDateTime actual = new DateFactory().parse(context, "dd/MM/yyyy", "03/02/2001", "12h", "15m");

        assertEquals(2001, actual.getYear(), "Year failed");
        assertEquals(2, actual.getMonthValue(), "Month failed");
        assertEquals(3, actual.getDayOfMonth(), "Day failed");
        assertEquals(12, actual.getHour(), "Hour failed");
        assertEquals(15, actual.getMinute(), "Minute failed");
        assertEquals(0, actual.getSecond(), "Second failed");
        assertEquals(0, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_partialDate_MonthYearOnly() {
        ZonedDateTime actual = new DateFactory().parse(context, "MM/yyyy", "11/2026");

        assertEquals(2026, actual.getYear(), "Year failed");
        assertEquals(11, actual.getMonthValue(), "Month failed");
        assertEquals(1, actual.getDayOfMonth(), "Day failed");
        assertEquals(0, actual.getHour(), "Hour failed");
        assertEquals(0, actual.getMinute(), "Minute failed");
        assertEquals(0, actual.getSecond(), "Second failed");
        assertEquals(0, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_yearOnly() {
        ZonedDateTime actual = new DateFactory().parse(context, "yyyy", "1999");

        assertEquals(1999, actual.getYear(), "Year failed");
        assertEquals(1, actual.getMonthValue(), "Month failed");
        assertEquals(1, actual.getDayOfMonth(), "Day failed");
        assertEquals(0, actual.getHour(), "Hour failed");
        assertEquals(0, actual.getMinute(), "Minute failed");
        assertEquals(0, actual.getSecond(), "Second failed");
        assertEquals(0, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_dateTimeWithTime() {
        ZonedDateTime actual = new DateFactory().parse(context, "yyyy-MM-dd HH:mm:ss.SSS", "2001-02-03 04:05:06.123");

        assertEquals(2001, actual.getYear(), "Year failed");
        assertEquals(2, actual.getMonthValue(), "Month failed");
        assertEquals(3, actual.getDayOfMonth(), "Day failed");
        assertEquals(4, actual.getHour(), "Hour failed");
        assertEquals(5, actual.getMinute(), "Minute failed");
        assertEquals(6, actual.getSecond(), "Second failed");
        assertEquals(123_000_000, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }


    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_eraAndYearDefaults() {
        ZonedDateTime actual = new DateFactory().parse(context, "dd-MM", "01-02");

        assertEquals(Year.now().getValue(), actual.getYear(), "Year failed");
        assertEquals(Month.FEBRUARY, actual.getMonth(), "Month failed");
        assertEquals(1, actual.getDayOfMonth(), "Day failed");
        assertEquals(0, actual.getHour(), "Hour failed");
        assertEquals(0, actual.getMinute(), "Minute failed");
        assertEquals(0, actual.getSecond(), "Second failed");
        assertEquals(0, actual.getNano(), "Nanos failed");
        assertEquals(ZoneOffset.UTC, actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_dateTimeWithZone() {
        ZonedDateTime actual = new DateFactory().parse(context, "yyyy-MM-dd HH:mm:ss.SSS z", "2001-02-03 04:05:06.789 GMT");

        assertEquals(2001, actual.getYear(), "Year failed");
        assertEquals(2, actual.getMonthValue(), "Month failed");
        assertEquals(3, actual.getDayOfMonth(), "Day failed");
        assertEquals(4, actual.getHour(), "Hour failed");
        assertEquals(5, actual.getMinute(), "Minute failed");
        assertEquals(6, actual.getSecond(), "Second failed");
        assertEquals(789_000_000, actual.getNano(), "Nanos failed");
        assertEquals(ZoneId.of("GMT"), actual.getZone(), "Zone failed");
    }

    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_SingleAndDoubleDigitDateTimeFields() {
        String pattern = "d/M/yyyy H:m:s";
        ZonedDateTime expected = ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC);

        assertEquals(
            expected,
            new DateFactory().parse(context, pattern, "3/2/2001 4:5:6"),
            "Failed to parse date-time for single-digit fields"
        );

        assertEquals(
            expected,
            new DateFactory().parse(context, pattern, "03/02/2001 04:05:06"),
            "Failed to parse date-time for double-digit fields"
        );
    }


    /**
     * Unit test {@link DateFactory#parse(EelContext, String, String, String...)}
     */
    @Test
    public void test_parse_invalidDate() {
        assertThrows(DateTimeException.class, () -> new DateFactory().parse(context, "dd/MM/yyyy", "31/02/2000"));
    }



    /**
     * Unit test {@link DateFactory#start}
     */
    @Test
    public void test_start() {
        ZonedDateTime first = new DateFactory().start(context, "UTC");
        ZonedDateTime second = new DateFactory().start(context, "UTC", "3days");

        assertEquals(date.toInstant(), first.toInstant(), "UTC Instant");
        assertEquals(ZoneOffset.ofHours(0), first.getOffset(), "UTC Zone");
        assertEquals(date.plusDays(3).toInstant(), second.toInstant(), "-5 Instant");

         // Check it's always the same
        assertEquals(first, new DateFactory().start(context, "UTC"), "#2 Unexpected time returned");
    }

    /**
     * Unit test {@link DateFactory#start}
     */
    @Test
    public void test_start_zone() {
        ZonedDateTime utc = new DateFactory().start(context, "UTC");
        ZonedDateTime plus1 = new DateFactory().start(context, "+1");
        ZonedDateTime minus5 = new DateFactory().start(context, "-5");

        assertEquals(date.toInstant(), utc.toInstant(), "UTC Instant");
        assertEquals(date.toInstant(), plus1.toInstant(), "+1 Instant");
        assertEquals(date.toInstant(), minus5.toInstant(), "-5 Instant");

        assertEquals(ZoneOffset.ofHours(0), utc.getOffset(), "UTC Zone");
        assertEquals(ZoneOffset.ofHours(1), plus1.getOffset(), "+1 Zone");
        assertEquals(ZoneOffset.ofHours(-5), minus5.getOffset(), "-5 Zone");
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_start_snap() {
        ZonedDateTime expected = ZonedDateTime.of(date.getYear(),
            date.getMonthValue(),
            date.getDayOfMonth(),
            0,
            0,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().start(context, "UTC", "@d");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_start_singleOffset() {
        ZonedDateTime actual = new DateFactory().start(context, "UTC", "2h");
        Duration duration = Duration.ofHours(2);
        long difference = date.plus(duration).until(actual, ChronoUnit.MILLIS);

        difference = Math.abs(difference);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_start_multipleOffsets() {
        ZonedDateTime expected = ZonedDateTime.of(date.getYear(),
            date.getMonthValue(),
            date.getDayOfMonth(),
            1,
            37,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().start(context, "UTC", "@d", "1h", "37m");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }


    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_NoOffsets() {
        ZonedDateTime actual = new DateFactory().utc(context);
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual);
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_snap() {
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            0,
            0,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().utc(context, "@d");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_singleOffset() {
        ZonedDateTime actual = new DateFactory().utc(context, "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_multipleOffsets() {
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            1,
            37,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().utc(context, "@d", "1h", "37m");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }


    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_NoOffsets() {
        ZonedDateTime actual = new DateFactory().local(context);
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual);
    }

    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_snap() {
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            0,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().local(context, "@h");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_singleOffset() {
        ZonedDateTime actual = new DateFactory().local(context, "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_multipleOffsets() {
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            37,
            0,
            0,
            ZoneOffset.UTC);
        ZonedDateTime actual = new DateFactory().local(context, "@h", "37m");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }


    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_NoOffsets() {
        ZonedDateTime actual = new DateFactory().at(context, "+02");
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual);
    }

    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_snap() {
        ZonedDateTime nowInZone = now.withZoneSameInstant(ZoneId.of("+02"));
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            nowInZone.getMonthValue(),
            nowInZone.getDayOfMonth(),
            nowInZone.getHour(),
            0,
            0,
            0,
            ZoneOffset.ofHours(2));
        ZonedDateTime actual = new DateFactory().at(context, "+02", "@h");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_singleOffset() {
        ZonedDateTime actual = new DateFactory().at(context, "+02", "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }

    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_multipleOffsets() {
        ZonedDateTime nowInZone = now.withZoneSameInstant(ZoneId.of("+02"));
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            nowInZone.getMonthValue(),
            nowInZone.getDayOfMonth(),
            1,
            37,
            0,
            0,
            ZoneOffset.ofHours(2));
        ZonedDateTime actual = new DateFactory().at(context, "+02", "@d", "1h", "37m");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }


    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_local_multipleOffsets_fractionalZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");           // Kolkata has a fractional offset from UTC
        TimeZone.setDefault(timeZone);

        now = ZonedDateTime.now();

        ZonedDateTime expected = ZonedDateTime.of(now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            1,
            37,
            0,
            0,
            timeZone.toZoneId().getRules().getOffset(now.toInstant()));
        ZonedDateTime actual = new DateFactory().local(context, "@d", "1h", "37m");
        Duration duration = Duration.between(expected, actual);
        long difference = Math.abs(duration.toMillis());

        assertTrue((difference <= TOLERANCE), "Unexpected date " + actual + ". Out by " + difference + "mS");
    }


    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_badZone() {
        assertThrows(DateTimeException.class, () -> new DateFactory().at(context, "badZone"));
    }


    private long difference(@Nonnull ZonedDateTime actual, int expectedDifference, @Nonnull ChronoUnit timeUnit) {
        Duration duration = Duration.of(expectedDifference, timeUnit);
        long difference = now.plus(duration).until(actual, ChronoUnit.MILLIS);

        return Math.abs(difference);
    }
}