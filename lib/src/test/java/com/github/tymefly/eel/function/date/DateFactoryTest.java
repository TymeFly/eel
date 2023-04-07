package com.github.tymefly.eel.function.date;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.exception.EelFunctionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DateFactory}
 */
public class DateFactoryTest {
    private static final long TOLERANCE = 25;

    private EelContext context;
    private ZonedDateTime now;
    private ZonedDateTime date;

    @Before
    public void setUp() {
        context = mock(EelContext.class);
        now = ZonedDateTime.now(ZoneId.of("UTC"));
        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);

        when(context.getStartTime())
            .thenReturn(date);
    }



    /**
     * Unit test {@link DateFactory#start}
     */
    @Test
    public void test_start() {
        ZonedDateTime first = new DateFactory().start(context, "UTC");

        Assert.assertEquals("UTC Instant", date.toInstant(), first.toInstant());
        Assert.assertEquals("UTC Zone", ZoneOffset.ofHours(0), first.getOffset());

        Assert.assertEquals("#2 Unexpected time returned", first, new DateFactory().start(context, "UTC")); // Check it's fixed
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
     * Unit test {@link DateFactory#utc(String...)}
     */
    @Test
    public void test_utc_NoOffsets() {
        ZonedDateTime actual = new DateFactory().utc();
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#utc(String...)}
     */
    @Test
    public void test_utc_singleOffset() {
        ZonedDateTime actual = new DateFactory().utc("2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#utc(String...)}
     */
    @Test
    public void test_utc_multipleOffsets() {
        ZonedDateTime actual = new DateFactory().utc("1h", "37m");
        long difference = difference(actual, 97, ChronoUnit.MINUTES);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }


    /**
     * Unit test {@link DateFactory#local(String...)}
     */
    @Test
    public void test_local_NoOffsets() {
        ZonedDateTime actual = new DateFactory().local();
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#local(String...)}
     */
    @Test
    public void test_local_singleOffset() {
        ZonedDateTime actual = new DateFactory().local("2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#local(String...)}
     */
    @Test
    public void test_local_multipleOffsets() {
        ZonedDateTime actual = new DateFactory().local("1h", "37m");
        long difference = difference(actual, 97, ChronoUnit.MINUTES);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }


    /**
     * Unit test {@link DateFactory#at(String, String...)}
     */
    @Test
    public void test_at_NoOffsets() {
        ZonedDateTime actual = new DateFactory().at("+02");
        long difference = difference(actual, 0, ChronoUnit.MILLIS);

        Assert.assertTrue("Unexpected date " + actual,(difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#at(String, String...)}
     */
    @Test
    public void test_at_singleOffset() {
        ZonedDateTime actual = new DateFactory().at("+02", "2h");
        long difference = difference(actual, 2, ChronoUnit.HOURS);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#at(String, String...)}
     */
    @Test
    public void test_at_multipleOffsets() {
        ZonedDateTime actual = new DateFactory().at("+02", "1h", "37m");
        long difference = difference(actual, 97, ChronoUnit.MINUTES);

        Assert.assertTrue("Unexpected date " + actual + ". Out by " + difference + "mS", (difference <= TOLERANCE));
    }

    /**
     * Unit test {@link DateFactory#at(String, String...)}
     */
    @Test
    public void test_at_badZone() {
        EelFunctionException actual =
            Assert.assertThrows(EelFunctionException.class, () -> new DateFactory().at("badZone"));

        Assert.assertEquals("Unexpected message", "Invalid Zone 'badZone'", actual.getMessage());
    }


    private long difference(@Nonnull ZonedDateTime actual, int expectedDifference, @Nonnull ChronoUnit timeUnit) {
        Duration duration = Duration.of(expectedDifference, timeUnit);
        long difference = now.plus(duration).until(actual, ChronoUnit.MILLIS);

        return difference;
    }
}