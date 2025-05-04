package com.github.tymefly.eel.function.date;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Offset}
 */
public class OffsetTest {
    private ZonedDateTime date;
    private EelContext context;    

    @Before
    public void setUp() {
        context = spy(EelContext.factory().build());

        when(context.getWeek())
            .thenReturn(WeekFields.of(DayOfWeek.SUNDAY, 1));

        ZoneId zone = ZoneId.of("UTC");
        Instant instant = Instant.ofEpochSecond(1196702100);

        date = ZonedDateTime.ofInstant(instant, zone);
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_plus_noOffsets() {
        Assert.assertEquals("no offsets",
            date,
            new Offset().plus(context, date));
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_plus_snap() {
        Assert.assertEquals("snap",
            date.withMinute(0).withSecond(0).withNano(0),
            new Offset().plus(context, date, "@h"));
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_offset_singleOffset() {
        Assert.assertEquals("single offset",
            date.plusHours(1),
            new Offset().plus(context, date, "1h"));
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_plus_multipleOffsets() {
        Assert.assertEquals("multiple offsets",
            date.plusMinutes(57),
            new Offset().plus(context, date, "1h", "-3m"));
    }




    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_noOffsets() {
        Assert.assertEquals("no offsets",
            date,
            new Offset().minus(context, date));
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_snap() {
        Assert.assertEquals("snap",
            date.withMinute(0).withSecond(0).withNano(0),
            new Offset().minus(context, date, "@h"));
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_singleOffset() {
        Assert.assertEquals("single offsets",
            date.minusHours(1),
            new Offset().minus(context, date, "1h"));
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_multipleOffsets() {
        Assert.assertEquals("multiple offsets",
            date.minusMinutes(57),
            new Offset().minus(context, date, "1h", "-3m"));
    }
}