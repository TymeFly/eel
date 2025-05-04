package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;

/**
 * Unit test for {@link DateHelper}
 */
public class DateHelperTest {
    private ZonedDateTime date;
    private EelContext context;
    private EelContext tuesdayContext;


    @Before
    public void setUp() {
        context = spy(EelContext.factory().withStartOfWeek(DayOfWeek.SUNDAY).build());
        tuesdayContext = spy(EelContext.factory().withStartOfWeek(DayOfWeek.TUESDAY).build());

        date = ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC);
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
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(1997, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "-4y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(1999, 11, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "-15M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(2001, 1, 20, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "-2w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 2, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "-1d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 6, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "2h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 8, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "3m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 10, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "4s"));
        Assert.assertEquals("Milli",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 912_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123I"));
        Assert.assertEquals("Micro",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 790_110_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123U"));
        Assert.assertEquals("Nano",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_444, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123N"));

        Assert.assertEquals("MilliOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 912_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123i"));
        Assert.assertEquals("MicroOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 790_110_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123u"));
        Assert.assertEquals("NanoOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_444, ZoneOffset.UTC),
            DateHelper.plus(context, date, "123n"));

        Assert.assertEquals("Over 24 hours",
            ZonedDateTime.of(2001, 2, 10, 22, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "186h"));
    }

    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus_snap() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@s"));
        Assert.assertEquals("Millis",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@I"));
        Assert.assertEquals("Micro",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@U"));
        Assert.assertEquals("Nano",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@N"));

        Assert.assertEquals("MilliOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@i"));
        Assert.assertEquals("MicroOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@u"));
        Assert.assertEquals("NanoOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.plus(context, date, "@n"));
    }


    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus_invalid() {
        Assert.assertThrows("No digits",
            DateTimeException.class,
            () -> DateHelper.plus(context, date, "s"));
        Assert.assertThrows("No units",
            DateTimeException.class,
            () -> DateHelper.plus(context, date, "123"));
        Assert.assertThrows("Bad units",
            DateTimeException.class,
            () -> DateHelper.plus(context, date, "123X"));
        Assert.assertThrows("Bad snap unit",
            DateTimeException.class,
            () -> DateHelper.plus(context, date, "@X"));

        Assert.assertThrows("Out of range",
            DateTimeException.class,
            () -> DateHelper.plus(context, date, "-9999999999999y"));
    }


    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(2005, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "-4y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2002, 5, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "-15M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(2001, 2, 17, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "-2w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 4, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "-1d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 2, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "2h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 2, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "3m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 2, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "4s"));
        Assert.assertEquals("Milli",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 666_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "123I"));
        Assert.assertEquals("Micro",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_864_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "123U"));
        Assert.assertEquals("Nano",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_201, ZoneOffset.UTC),
            DateHelper.minus(context, date, "120N"));

        Assert.assertEquals("MilliOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 666_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "123i"));
        Assert.assertEquals("MicroOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_864_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "123u"));
        Assert.assertEquals("NanoOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_201, ZoneOffset.UTC),
            DateHelper.minus(context, date, "120n"));

        Assert.assertEquals("Over 24 hours",
            ZonedDateTime.of(2001, 1, 26, 10, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "186h"));
    }

    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus_snap() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@s"));
        Assert.assertEquals("Millis",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@I"));
        Assert.assertEquals("Micros",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@U"));
        Assert.assertEquals("Nanos",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@N"));

        Assert.assertEquals("MilliOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@i"));
        Assert.assertEquals("MicrosOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@u"));
        Assert.assertEquals("NanosOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.minus(context, date, "@n"));
    }


    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus_invalid() {
        Assert.assertThrows("No digits",
            DateTimeException.class,
            () -> DateHelper.minus(context, date, "s"));
        Assert.assertThrows("No units",
            DateTimeException.class,
            () -> DateHelper.minus(context, date, "123"));
        Assert.assertThrows("Bad units",
            DateTimeException.class,
            () -> DateHelper.minus(context, date, "123X"));
        Assert.assertThrows("Bad snap unit",
            DateTimeException.class,
            () -> DateHelper.minus(context, date, "@X"));

        Assert.assertThrows("Out of range",
            DateTimeException.class,
            () -> DateHelper.minus(context, date, "-99999999999y"));
    }


    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(-4, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "-4y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2001, 10, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "10M"));
        Assert.assertEquals("Weeks",
            ZonedDateTime.of(2001, 1, 6, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "1w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 1, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "1d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 0, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "0h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 0, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "0m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "0s"));
        Assert.assertEquals("Milli",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 999_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "999I"));
        Assert.assertEquals("Micro",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_999_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "999U"));
        Assert.assertEquals("Nano",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_999, ZoneOffset.UTC),
            DateHelper.setField(context, date, "999N"));

        Assert.assertEquals("MilliOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 999_000_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "999i"));
        Assert.assertEquals("MicroOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 9_999_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "9_999u"));
        Assert.assertEquals("NanoOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 9_999, ZoneOffset.UTC),
            DateHelper.setField(context, date, "9_999n"));
    }

    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField_snap() {
        Assert.assertEquals("Years",
            ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@y"));
        Assert.assertEquals("Months",
            ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@M"));
        Assert.assertEquals("Weeks - sunday",
            ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@w"));
        Assert.assertEquals("Weeks - tuesday",
            ZonedDateTime.of(2001, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(tuesdayContext, date, "@w"));
        Assert.assertEquals("Days",
            ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@d"));
        Assert.assertEquals("Hours",
            ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@h"));
        Assert.assertEquals("Minutes",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@m"));
        Assert.assertEquals("Seconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@s"));
        Assert.assertEquals("Millis",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@I"));
        Assert.assertEquals("Micro",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@U"));
        Assert.assertEquals("Nano",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@N"));

        Assert.assertEquals("MilliOfSeconds",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@i"));
        Assert.assertEquals("MicroOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@u"));
        Assert.assertEquals("NanoOfSecond",
            ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC),
            DateHelper.setField(context, date, "@n"));
    }

    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField_invalid() {
        Assert.assertThrows("Negative weeks are unsupported",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, "-2w"));
        Assert.assertThrows("Bad relative units",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, "-2X"));
        Assert.assertThrows("Bad snap unit",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, "@X"));
    }

    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_setField_range() {
        Assert.assertThrows("Years",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, "9999999999y"));
        Assert.assertThrows("Years",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, "-99999999999y"));

        setField_rangeHelper(Period.YEAR, -999999999, 999999999);
        setField_rangeHelper(Period.MONTH, 1, 12);
        setField_rangeHelper(Period.DAY, 1, 28);                        // 28 as Feb 2001 is not a leap year
        setField_rangeHelper(Period.HOUR, 0, 23);
        setField_rangeHelper(Period.MINUTE, 0, 59);
        setField_rangeHelper(Period.SECOND, 0, 59);
        setField_rangeHelper(Period.MILLI, 0, 999);
        setField_rangeHelper(Period.MICRO, 0, 999);
        setField_rangeHelper(Period.NANO, 0, 999);

        setField_rangeHelper(Period.MILLI_OF_SECONDS, 0, 999);
        setField_rangeHelper(Period.MICRO_OF_SECONDS, 0, 999_999);
        setField_rangeHelper(Period.NANO_OF_SECONDS, 0, 999_999_999);
    }


    private void setField_rangeHelper(@Nonnull Period period, long minValue, long maxValue) {
        String minRange = minValue + period.shortForm();
        String underRange = (minValue - 1) + period.shortForm();
        String maxRange = maxValue + period.shortForm();
        String overRange = (maxValue + 1) + period.shortForm();

        DateHelper.setField(context, date, minRange);                      // Just make sure there are no exceptions
        DateHelper.setField(context, date, maxRange);

        Assert.assertThrows(period.name() + " under range",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, underRange));

        Assert.assertThrows(period.name() + " over range",
            DateTimeException.class,
            () -> DateHelper.setField(context, date, overRange));
    }
}