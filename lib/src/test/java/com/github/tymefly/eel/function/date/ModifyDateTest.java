package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ModifyDate}
 */
public class ModifyDateTest {
    private ZonedDateTime date;


    @Before
    public void setUp() {
        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
    }



    /**
     * Unit test {@link ModifyDate#set(ZonedDateTime, String...)}
     */
    @Test
    public void test_set() {
        Assert.assertEquals("Years",
           ZonedDateTime.of(-4, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "-4year"));
        Assert.assertEquals("Months",
           ZonedDateTime.of(2000, 12, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "12M"));
        Assert.assertEquals("Days",
           ZonedDateTime.of(2000, 2, 1, 4, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "1day"));
        Assert.assertEquals("Hours",
           ZonedDateTime.of(2000, 2, 3, 23, 5, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "23h"));
        Assert.assertEquals("Minutes",
           ZonedDateTime.of(2000, 2, 3, 4, 59, 6, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "59m"));
        Assert.assertEquals("Seconds",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 12, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "12s"));

        Assert.assertEquals("multiple fields",
           ZonedDateTime.of(1999, 2, 3, 0, 5, 56, 123_456_789, ZoneOffset.UTC),
           new ModifyDate().set(date, "1999y", "0h", "56s"));
    }

    /**
     * Unit test {@link ModifyDate#set(ZonedDateTime, String...)}
     */
    @Test
    public void test_set_unsupportedSpec() {
        Assert.assertThrows("Unsupported Unit", DateTimeException.class, () -> new ModifyDate().set(date, "1w"));
        Assert.assertThrows("Bad Unit", DateTimeException.class, () -> new ModifyDate().set(date, "1x"));
        Assert.assertThrows("Only Unit", DateTimeException.class, () -> new ModifyDate().set(date, "h"));
        Assert.assertThrows("Only Offset", DateTimeException.class, () -> new ModifyDate().set(date, "1"));
    }

    /**
     * Unit test {@link ModifyDate#set(ZonedDateTime, String...)}
     */
    @Test
    public void test_set_outOfRange() {
        Assert.assertThrows("negative Offset", DateTimeException.class, () -> new ModifyDate().set(date, "-1h"));
        Assert.assertThrows("25 hour day", DateTimeException.class, () -> new ModifyDate().set(date, "25h"));
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


    /**
     * Unit test {@link ModifyDate#truncate(ZonedDateTime, String)}
     */
    @Test
    public void test_truncate() {
        Assert.assertEquals("Years",
           ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "year"));
        Assert.assertEquals("Months",
           ZonedDateTime.of(2000, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "M"));
        Assert.assertEquals("Days",
           ZonedDateTime.of(2000, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "day"));
        Assert.assertEquals("Hours",
           ZonedDateTime.of(2000, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "h"));
        Assert.assertEquals("Minutes",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "m"));
        Assert.assertEquals("Seconds",
           ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC),
           new ModifyDate().truncate(date, "s"));
    }

    /**
     * Unit test {@link ModifyDate#truncate(ZonedDateTime, String)}
     */
    @Test
    public void test_truncate_unsupported() {
        Assert.assertThrows("w", DateTimeException.class, () -> new ModifyDate().truncate(date, "w"));
        Assert.assertThrows("?", DateTimeException.class, () -> new ModifyDate().truncate(date, "?"));
    }
}