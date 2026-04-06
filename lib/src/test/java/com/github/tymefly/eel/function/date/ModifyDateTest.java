package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ModifyDate}
 */
public class ModifyDateTest {
    private ZonedDateTime date;
    private EelContext context;


    @BeforeEach
    public void setUp() {
        context = spy(EelContext.factory().build());

        when(context.getWeek())
            .thenReturn(WeekFields.of(DayOfWeek.SUNDAY, 1));

        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
    }



    /**
     * Unit test {@link ModifyDate#set(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_set() {
        assertEquals(ZonedDateTime.of(-4, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "-4year"), "Years");
        assertEquals(ZonedDateTime.of(2000, 12, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "12M"), "Months");
        assertEquals(ZonedDateTime.of(2000, 2, 1, 4, 5, 6, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "1day"), "Days");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 23, 5, 6, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "23h"), "Hours");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 59, 6, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "59m"), "Minutes");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 12, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "12s"), "Seconds");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 15_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "15I"), "Millis");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_019_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "19U"), "Micros");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_023, ZoneOffset.UTC), new ModifyDate().set(context, date, "23N"), "Nanos");

        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_000_000, ZoneOffset.UTC), new ModifyDate().set(context, date, "123i"), "MilliOfSecond");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 1_234_000, ZoneOffset.UTC), new ModifyDate().set(context, date, "1234u"), "MicrosOfSecond");
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 1_234_567, ZoneOffset.UTC), new ModifyDate().set(context, date, "1234567n"), "Nanos");

        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC), new ModifyDate().set(context, date, "@m"), "Snap");

        assertEquals(ZonedDateTime.of(1999, 2, 3, 0, 5, 56, 123_456_789, ZoneOffset.UTC), new ModifyDate().set(context, date, "1999y", "0h", "56s"), "multiple fields");
    }

    /**
     * Unit test {@link ModifyDate#set(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_set_unsupportedSpec() {
        assertThrows(DateTimeException.class, () -> new ModifyDate().set(context, date, "1x"), "Bad Unit");
        assertThrows(DateTimeException.class, () -> new ModifyDate().set(context, date, "h"), "Only Unit");
        assertThrows(DateTimeException.class, () -> new ModifyDate().set(context, date, "1"), "Only Offset");
    }

    /**
     * Unit test {@link ModifyDate#set(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_set_outOfRange() {
        assertThrows(DateTimeException.class, () -> new ModifyDate().set(context, date, "-1h"), "negative Offset");
        assertThrows(DateTimeException.class, () -> new ModifyDate().set(context, date, "25h"), "25 hour day");
    }


    /**
     * Unit test {@link ModifyDate#moveZone(ZonedDateTime, String)}
     */
    @Test
    public void test_moveZone() {
        assertEquals(ZonedDateTime.of(2000, 2, 3, 5, 5, 6, 123_456_789, ZoneOffset.of("+01:00")), new ModifyDate().moveZone(date, "+1"), "Unexpected DATE");
    }

    /**
     * Unit test {@link ModifyDate#moveZone(ZonedDateTime, String)}
     */
    @Test
    public void test_moveZone_badZone() {
        assertThrows(DateTimeException.class, () -> new ModifyDate().moveZone(date, "???"));
    }


    /**
     * Unit test {@link ModifyDate#setZone(ZonedDateTime, String)}
     */
    @Test
    public void test_setZone() {
        assertEquals(ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.of("+01:00")), new ModifyDate().setZone(date, "+1"), "Unexpected DATE");
    }

    /**
     * Unit test {@link ModifyDate#setZone(ZonedDateTime, String)}
     */
    @Test
    public void test_setZone_badZone() {
        assertThrows(DateTimeException.class, () -> new ModifyDate().setZone(date, "???"));
    }
}