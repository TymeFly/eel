package com.github.tymefly.eel.function.date;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Offset}
 */
public class OffsetTest {
    private ZonedDateTime date;
    private EelContext context;    

    @BeforeEach
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
        assertEquals(date, new Offset().plus(context, date), "no offsets");
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_plus_snap() {
        assertEquals(date.withMinute(0).withSecond(0).withNano(0), new Offset().plus(context, date, "@h"), "snap");
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_offset_singleOffset() {
        assertEquals(date.plusHours(1), new Offset().plus(context, date, "1h"), "single offset");
    }

    /**
     * Unit test {@link Offset#plus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_plus_multipleOffsets() {
        assertEquals(date.plusMinutes(57), new Offset().plus(context, date, "1h", "-3m"), "multiple offsets");
    }




    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_noOffsets() {
        assertEquals(date, new Offset().minus(context, date), "no offsets");
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_snap() {
        assertEquals(date.withMinute(0).withSecond(0).withNano(0), new Offset().minus(context, date, "@h"), "snap");
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_singleOffset() {
        assertEquals(date.minusHours(1), new Offset().minus(context, date, "1h"), "single offsets");
    }

    /**
     * Unit test {@link Offset#minus(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_minus_multipleOffsets() {
        assertEquals(date.minusMinutes(57), new Offset().minus(context, date, "1h", "-3m"), "multiple offsets");
    }
}