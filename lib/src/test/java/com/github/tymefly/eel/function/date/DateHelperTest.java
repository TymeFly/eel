package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

/**
 * Unit test for {@link DateHelper}
 */
public class DateHelperTest {
    private ZonedDateTime date;
    private EelContext context;
    private EelContext tuesdayContext;


    @BeforeEach
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
        assertEquals(ZoneId.of("UTC"), DateHelper.toZone("UTC"), "UTC");
        assertEquals(ZoneId.of("+1"), DateHelper.toZone("+1"), "+1");
        assertEquals(ZoneId.of("-5"), DateHelper.toZone("-5"), "-5");
        assertEquals(ZoneId.of("Europe/Paris"), DateHelper.toZone("Europe/Paris"), "Paris");

        assertThrows(DateTimeException.class, () -> DateHelper.toZone("unknown"));
    }


    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus() {
        assertEquals(ZonedDateTime.of(1997, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "-4y"), "Years");
        assertEquals(ZonedDateTime.of(1999, 11, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "-15M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 1, 20, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "-2w"), "Weeks");
        assertEquals(ZonedDateTime.of(2001, 2, 2, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "-1d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 6, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "2h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 8, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "3m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 10, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "4s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 912_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "123I"), "Milli");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 790_110_321, ZoneOffset.UTC), DateHelper.plus(context, date, "123U"), "Micro");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_444, ZoneOffset.UTC), DateHelper.plus(context, date, "123N"), "Nano");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 912_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "123i"), "MilliOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 790_110_321, ZoneOffset.UTC), DateHelper.plus(context, date, "123u"), "MicroOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_444, ZoneOffset.UTC), DateHelper.plus(context, date, "123n"), "NanoOfSecond");

        assertEquals(ZonedDateTime.of(2001, 2, 10, 22, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "186h"), "Over 24 hours");
    }

    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus_snap() {
        assertEquals(ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@y"), "Years");
        assertEquals(ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@w"), "Weeks");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC), DateHelper.plus(context, date, "@s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.plus(context, date, "@I"), "Millis");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.plus(context, date, "@U"), "Micro");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "@N"), "Nano");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.plus(context, date, "@i"), "MilliOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.plus(context, date, "@u"), "MicroOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.plus(context, date, "@n"), "NanoOfSeconds");
    }


    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_plus_invalid() {
        assertThrows(DateTimeException.class,
            () -> DateHelper.plus(context, date, "s"),
            "No digits");
        assertThrows(DateTimeException.class,
            () -> DateHelper.plus(context, date, "123"),
            "No units");
        assertThrows(DateTimeException.class,
            () -> DateHelper.plus(context, date, "123X"),
            "Bad units");
        assertThrows(DateTimeException.class,
            () -> DateHelper.plus(context, date, "@X"),
            "Bad snap unit");

        assertThrows(DateTimeException.class,
            () -> DateHelper.plus(context, date, "-9999999999999y"),
            "Out of range");
    }


    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus() {
        assertEquals(ZonedDateTime.of(2005, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "-4y"), "Years");
        assertEquals(ZonedDateTime.of(2002, 5, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "-15M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 2, 17, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "-2w"), "Weeks");
        assertEquals(ZonedDateTime.of(2001, 2, 4, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "-1d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 2, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "2h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 2, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "3m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 2, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "4s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 666_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "123I"), "Milli");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_864_321, ZoneOffset.UTC), DateHelper.minus(context, date, "123U"), "Micro");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_201, ZoneOffset.UTC), DateHelper.minus(context, date, "120N"), "Nano");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 666_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "123i"), "MilliOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_864_321, ZoneOffset.UTC), DateHelper.minus(context, date, "123u"), "MicroOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_201, ZoneOffset.UTC), DateHelper.minus(context, date, "120n"), "NanoOfSecond");

        assertEquals(ZonedDateTime.of(2001, 1, 26, 10, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "186h"), "Over 24 hours");
    }

    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus_snap() {
        assertEquals(ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@y"), "Years");
        assertEquals(ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@w"), "Weeks");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC), DateHelper.minus(context, date, "@s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.minus(context, date, "@I"), "Millis");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.minus(context, date, "@U"), "Micros");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "@N"), "Nanos");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.minus(context, date, "@i"), "MilliOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.minus(context, date, "@u"), "MicrosOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.minus(context, date, "@n"), "NanosOfSecond");
    }


    /**
     * Unit test {@link DateHelper#minus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_minus_invalid() {
        assertThrows(DateTimeException.class,
            () -> DateHelper.minus(context, date, "s"),
            "No digits");
        assertThrows(DateTimeException.class,
            () -> DateHelper.minus(context, date, "123"),
            "No units");
        assertThrows(DateTimeException.class,
            () -> DateHelper.minus(context, date, "123X"),
            "Bad units");
        assertThrows(DateTimeException.class,
            () -> DateHelper.minus(context, date, "@X"),
            "Bad snap unit");

        assertThrows(DateTimeException.class,
            () -> DateHelper.minus(context, date, "-99999999999y"),
            "Out of range");
    }


    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField() {
        assertEquals(ZonedDateTime.of(-4, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "-4y"), "Years");
        assertEquals(ZonedDateTime.of(2001, 10, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "10M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 1, 6, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "1w"), "Weeks");
        assertEquals(ZonedDateTime.of(2001, 2, 1, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "1d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 0, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "0h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 0, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "0m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "0s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 999_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "999I"), "Milli");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_999_321, ZoneOffset.UTC), DateHelper.setField(context, date, "999U"), "Micro");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_999, ZoneOffset.UTC), DateHelper.setField(context, date, "999N"), "Nano");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 999_000_000, ZoneOffset.UTC), DateHelper.setField(context, date, "999i"), "MilliOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 9_999_000, ZoneOffset.UTC), DateHelper.setField(context, date, "9_999u"), "MicroOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 9_999, ZoneOffset.UTC), DateHelper.setField(context, date, "9_999n"), "NanoOfSecond");
    }

    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField_snap() {
        assertEquals(ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@y"), "Years");
        assertEquals(ZonedDateTime.of(2001, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@M"), "Months");
        assertEquals(ZonedDateTime.of(2001, 1, 28, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@w"), "Weeks - sunday");
        assertEquals(ZonedDateTime.of(2001, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(tuesdayContext, date, "@w"), "Weeks - tuesday");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 0, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@d"), "Days");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 0, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@h"), "Hours");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 0, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@m"), "Minutes");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC), DateHelper.setField(context, date, "@s"), "Seconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.setField(context, date, "@I"), "Millis");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.setField(context, date, "@U"), "Micro");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "@N"), "Nano");

        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_000_000, ZoneOffset.UTC), DateHelper.setField(context, date, "@i"), "MilliOfSeconds");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_000, ZoneOffset.UTC), DateHelper.setField(context, date, "@u"), "MicroOfSecond");
        assertEquals(ZonedDateTime.of(2001, 2, 3, 4, 5, 6, 789_987_321, ZoneOffset.UTC), DateHelper.setField(context, date, "@n"), "NanoOfSecond");
    }

    /**
     * Unit test {@link DateHelper#setField(EelContext, ZonedDateTime, String)} 
     */
    @Test
    public void test_setField_invalid() {
        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, "-2w"),
            "Negative weeks are unsupported");
        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, "-2X"),
            "Bad relative units");
        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, "@X"),
            "Bad snap unit");
    }

    /**
     * Unit test {@link DateHelper#plus(EelContext, ZonedDateTime, String)}
     */
    @Test
    public void test_setField_range() {
        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, "9999999999y"),
            "Positive Years");
        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, "-99999999999y"),
            "Negative Years");

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

        DateHelper.setField(context, date, minRange);                      // make sure there are no exceptions
        DateHelper.setField(context, date, maxRange);

        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, underRange),
            period.name() + " under range");

        assertThrows(DateTimeException.class,
            () -> DateHelper.setField(context, date, overRange),
            period.name() + " over range");
    }
}