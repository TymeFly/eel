package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link DateHelper}
 */
public class DateHelperTest {
    private ZonedDateTime date;


    @Before
    public void setUp() {
        date = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC);
    }


    /**
     * Unit test {@link DateHelper#toZone(String)}
     */
    @Test
    public void test_toZone() {
        Assert.assertEquals("UTC", ZoneId.of("UTC"), DateHelper.toZone("UTC"));
        Assert.assertEquals("+1", ZoneId.of("+1"), DateHelper.toZone("+1"));
        Assert.assertEquals("-5", ZoneId.of("-5"), DateHelper.toZone("-5"));
        Assert.assertEquals("Paris", ZoneId.of("Europe/Paris"), DateHelper.toZone("Europe/Paris"));

        Assert.assertThrows(DateTimeException.class, () -> DateHelper.toZone("unknown"));
    }


    /**
     * Unit test {@link DateHelper#applyOffset(ZonedDateTime, String)}
     */
    @Test
    public void test_applyOffset() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(1996, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "-4y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(1999, 10, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "-3M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(1999, 12, 19, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "-2w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2000, 1, 1, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "-1d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2000, 1, 2, 5, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "2h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2000, 1, 2, 3, 7, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "3m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2000, 1, 2, 3, 4, 9, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "4s"));

        Assert.assertEquals("Over 24 hours",
            ZonedDateTime.of(2000, 1, 9, 21, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.applyOffset(date, "186h"));
    }

    /**
     * Unit test {@link DateHelper#applyOffset(ZonedDateTime, String)}
     */
    @Test
    public void test_applyOffset_invalid() {
        Assert.assertThrows("No digits",
            DateTimeException.class,
            () -> DateHelper.applyOffset(date, "s"));
        Assert.assertThrows("No units",
            DateTimeException.class,
            () -> DateHelper.applyOffset(date, "123"));
        Assert.assertThrows("Bad units",
            DateTimeException.class,
            () -> DateHelper.applyOffset(date, "123X"));

        Assert.assertThrows("Out of range",
            DateTimeException.class,
            () -> DateHelper.applyOffset(date, "-99999999999y"));
    }

    /**
     * Unit test {@link DateHelper#setField(ZonedDateTime, String)}
     */
    @Test
    public void test_setField() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(-4, 1, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "-4y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2000, 10, 2, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "10M"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2000, 1, 1, 3, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "1d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2000, 1, 2, 5, 4, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "5h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2000, 1, 2, 3, 7, 5, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "7m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2000, 1, 2, 3, 4, 9, 6_000_000, ZoneOffset.UTC),
            DateHelper.setField(date, "9s"));
    }

    /**
     * Unit test {@link DateHelper#setField(ZonedDateTime, String)}
     */
    @Test
    public void test_setField_invalid() {
        Assert.assertThrows("Weeks are unsupported",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-2w"));
        Assert.assertThrows("Bad units",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-2X"));
    }

    /**
     * Unit test {@link DateHelper#applyOffset(ZonedDateTime, String)}
     */
    @Test
    public void test_setField_rangeError_max() {
        Assert.assertThrows("Years",
            DateTimeException.class,
            () -> DateHelper.setField(date, "99999999999y"));
        Assert.assertThrows("Months",
            DateTimeException.class,
            () -> DateHelper.setField(date, "13M"));
        Assert.assertThrows("Days",
            DateTimeException.class,
            () -> DateHelper.setField(date, "32d"));
        Assert.assertThrows("Hours",
            DateTimeException.class,
            () -> DateHelper.setField(date, "24h"));
        Assert.assertThrows("Minutes",
            DateTimeException.class,
            () -> DateHelper.setField(date, "60m"));
        Assert.assertThrows("Seconds",
            DateTimeException.class,
            () -> DateHelper.setField(date, "60s"));
    }

    /**
     * Unit test {@link DateHelper#applyOffset(ZonedDateTime, String)}
     */
    @Test
    public void test_setField_rangeError_min() {
        Assert.assertThrows("Years",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-99999999999y"));
        Assert.assertThrows("Months",
            DateTimeException.class,
            () -> DateHelper.setField(date, "0M"));
        Assert.assertThrows("Days",
            DateTimeException.class,
            () -> DateHelper.setField(date, "0d"));
        Assert.assertThrows("Hours",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-1h"));
        Assert.assertThrows("Minutes",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-1m"));
        Assert.assertThrows("Seconds",
            DateTimeException.class,
            () -> DateHelper.setField(date, "-1s"));
    }
}