package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ModifyDate}
 */
public class ModifyDateTest {
    private ZonedDateTime date;
    private EelContext context;


    @Before
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
        Assert.assertEquals("Years",
           ZonedDateTime.of(-4, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "-4year"));
        Assert.assertEquals("Months",
           ZonedDateTime.of(2000, 12, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "12M"));
        Assert.assertEquals("Days",
           ZonedDateTime.of(2000, 2, 1, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "1day"));
        Assert.assertEquals("Hours",
           ZonedDateTime.of(2000, 2, 3, 23, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "23h"));
        Assert.assertEquals("Minutes",
           ZonedDateTime.of(2000, 2, 3, 4, 59, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "59m"));
        Assert.assertEquals("Seconds",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 12, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "12s"));
        Assert.assertEquals("Millis",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 15_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "15I"));
        Assert.assertEquals("Micros",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_019_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "19U"));
        Assert.assertEquals("Nanos",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_023, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "23N"));

        Assert.assertEquals("MilliOfSecond",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_000_000, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "123i"));
        Assert.assertEquals("MicrosOfSecond",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 1_234_000, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "1234u"));
        Assert.assertEquals("Nanos",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 1_234_567, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "1234567n"));

        Assert.assertEquals("Snap",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "@m"));

        Assert.assertEquals("multiple fields",
           ZonedDateTime.of(1999, 2, 3, 0, 5, 56, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(context, date, "1999y", "0h", "56s"));
    }

    /**
     * Unit test {@link ModifyDate#set(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_set_unsupportedSpec() {
        Assert.assertThrows("Bad Unit", DateTimeException.class, () -> new ModifyDate().set(context, date, "1x"));
        Assert.assertThrows("Only Unit", DateTimeException.class, () -> new ModifyDate().set(context, date, "h"));
        Assert.assertThrows("Only Offset", DateTimeException.class, () -> new ModifyDate().set(context, date, "1"));
    }

    /**
     * Unit test {@link ModifyDate#set(EelContext, ZonedDateTime, String...)}
     */
    @Test
    public void test_set_outOfRange() {
        Assert.assertThrows("negative Offset", DateTimeException.class, () -> new ModifyDate().set(context, date, "-1h"));
        Assert.assertThrows("25 hour day", DateTimeException.class, () -> new ModifyDate().set(context, date, "25h"));
    }


    /**
     * Unit test {@link ModifyDate#moveZone(ZonedDateTime, String)}
     */
    @Test
    public void test_moveZone() {
        Assert.assertEquals("Unexpected DATE",
            ZonedDateTime.of(2000, 2, 3, 5, 5, 6, 123_456_789, ZoneOffset.of("+01:00")),
            new ModifyDate().moveZone(date, "+1"));
    }

    /**
     * Unit test {@link ModifyDate#moveZone(ZonedDateTime, String)}
     */
    @Test
    public void test_moveZone_badZone() {
        Assert.assertThrows(DateTimeException.class, () -> new ModifyDate().moveZone(date, "???"));
    }


    /**
     * Unit test {@link ModifyDate#setZone(ZonedDateTime, String)}
     */
    @Test
    public void test_setZone() {
        Assert.assertEquals("Unexpected DATE",
            ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.of("+01:00")),
            new ModifyDate().setZone(date, "+1"));
    }

    /**
     * Unit test {@link ModifyDate#setZone(ZonedDateTime, String)}
     */
    @Test
    public void test_setZone_badZone() {
        Assert.assertThrows(DateTimeException.class, () -> new ModifyDate().setZone(date, "???"));
    }
}