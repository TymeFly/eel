package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DateFactory}
 */
public class DateFactoryTest {
    private static final long TOLERANCE = 100;

    private EelContext context;
    private ZonedDateTime now;
    private ZonedDateTime date;

    @Before
    public void setUp() {
        context = spy(EelContext.factory().build());
        now = ZonedDateTime.now(ZoneId.of("UTC"));
        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);

        when(context.getStartTime())
            .thenReturn(date);
        when(context.getWeek())
            .thenReturn(WeekFields.of(DayOfWeek.SUNDAY, 1));
    }



    /**
     * Unit test {@link DateFactory#start}
     */
    @Test
    public void test_start() {
        ZonedDateTime first = new DateFactory().start(context, "UTC");
        ZonedDateTime second = new DateFactory().start(context, "UTC", "3days");

        Assert.assertEquals("UTC Instant", date.toInstant(), first.toInstant());
        Assert.assertEquals("UTC Zone", ZoneOffset.ofHours(0), first.getOffset());
        Assert.assertEquals("-5 Instant", date.plusDays(3).toInstant(), second.toInstant());

         // Check it's always the same
        Assert.assertEquals("#2 Unexpected time returned", first, new DateFactory().start(context, "UTC"));
    }

    /**
     * Unit test {@link DateFactory#start}
     */
    @Test
    public void test_start_zone() {
        ZonedDateTime utc = new DateFactory().start(context, "UTC");
        ZonedDateTime plus1 = new DateFactory().start(context, "+1");
        ZonedDateTime minus5 = new DateFactory().start(context, "-5");

        Assert.assertEquals("UTC Instant", date.toInstant(), utc.toInstant());
        Assert.assertEquals("+1 Instant", date.toInstant(), plus1.toInstant());
        Assert.assertEquals("-5 Instant", date.toInstant(), minus5.toInstant());

        Assert.assertEquals("UTC Zone", ZoneOffset.ofHours(0), utc.getOffset());
        Assert.assertEquals("+1 Zone", ZoneOffset.ofHours(1), plus1.getOffset());
        Assert.assertEquals("-5 Zone", ZoneOffset.ofHours(-5), minus5.getOffset());
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }


    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_NoOffsets() {
        ZonedDateTime actual = new DateFactory().utc(context);
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#utc(EelContext, String...)}
     */
    @Test
    public void test_utc_singleOffset() {
        ZonedDateTime actual = new DateFactory().utc(context, "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }


    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_NoOffsets() {
        ZonedDateTime actual = new DateFactory().local(context);
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#local(EelContext, String...)}
     */
    @Test
    public void test_local_singleOffset() {
        ZonedDateTime actual = new DateFactory().local(context, "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }


    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_NoOffsets() {
        ZonedDateTime actual = new DateFactory().at(context, "+02");
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_singleOffset() {
        ZonedDateTime actual = new DateFactory().at(context, "+02", "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
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

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#at(EelContext, String, String...)}
     */
    @Test
    public void test_at_badZone() {
        Assert.assertThrows(DateTimeException.class, () -> new DateFactory().at(context, "badZone"));
    }


    private long difference(@Nonnull ZonedDateTime actual, int expectedDifference, @Nonnull ChronoUnit timeUnit) {
        Duration duration = Duration.of(expectedDifference, timeUnit);
        long difference = now.plus(duration).until(actual, ChronoUnit.MILLIS);

        return Math.abs(difference);
    }
}