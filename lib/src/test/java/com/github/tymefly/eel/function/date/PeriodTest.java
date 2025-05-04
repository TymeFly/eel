package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Period}
 */
public class PeriodTest {

    /**
     * Unit test {@link Period#shortForm()}
     */
    @Test
    public void test_shortForm() {
        Assert.assertEquals("Year", "y", Period.YEAR.shortForm());
        Assert.assertEquals("Week", "w", Period.WEEK.shortForm());
        Assert.assertEquals("Month", "M", Period.MONTH.shortForm());
        Assert.assertEquals("Minute", "m", Period.MINUTE.shortForm());
        Assert.assertEquals("Milli", "I", Period.MILLI.shortForm());
        Assert.assertEquals("Micro", "U", Period.MICRO.shortForm());
        Assert.assertEquals("Nano", "N", Period.NANO.shortForm());

        Assert.assertEquals("MilliOfSeconds", "i", Period.MILLI_OF_SECONDS.shortForm());
        Assert.assertEquals("MicroOfSeconds", "u", Period.MICRO_OF_SECONDS.shortForm());
        Assert.assertEquals("NanoOfSecond", "n", Period.NANO_OF_SECONDS.shortForm());
    }
    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_shortForm() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("y"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("w"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("M"));
        Assert.assertEquals("Minute", Period.MINUTE, Period.lookup("m"));
        Assert.assertEquals("Milli", Period.MILLI, Period.lookup("I"));
        Assert.assertEquals("Micro", Period.MICRO, Period.lookup("U"));
        Assert.assertEquals("Nano", Period.NANO, Period.lookup("N"));

        Assert.assertEquals("MilliOfSeconds", Period.MILLI_OF_SECONDS, Period.lookup("i"));
        Assert.assertEquals("MicroOfSeconds", Period.MICRO_OF_SECONDS, Period.lookup("u"));
        Assert.assertEquals("NanoOfSecond", Period.NANO_OF_SECONDS, Period.lookup("n"));
    }

    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("year"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("week"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("month"));
        Assert.assertEquals("Minute", Period.MINUTE, Period.lookup("minute"));
        Assert.assertEquals("Milli", Period.MILLI, Period.lookup("milli"));
        Assert.assertEquals("Micro", Period.MICRO, Period.lookup("micro"));
        Assert.assertEquals("Nano", Period.NANO, Period.lookup("nano"));

        Assert.assertEquals("MilliOfSeconds", Period.MILLI_OF_SECONDS, Period.lookup("milliOfSecond"));
        Assert.assertEquals("MicroOfSecond", Period.MICRO_OF_SECONDS, Period.lookup("microOfSecond"));
        Assert.assertEquals("NanoOfSecond", Period.NANO_OF_SECONDS, Period.lookup("nanoOfSecond"));
    }


    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm_plural() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("years"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("weeks"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("months"));
        Assert.assertEquals("Minute", Period.MINUTE, Period.lookup("minutes"));
        Assert.assertEquals("Milli", Period.MILLI, Period.lookup("millis"));
        Assert.assertEquals("Micro", Period.MICRO, Period.lookup("micros"));
        Assert.assertEquals("Nano", Period.NANO, Period.lookup("nanos"));

        Assert.assertEquals("MilliOfSeconds", Period.MILLI_OF_SECONDS, Period.lookup("millisOfSecond"));
        Assert.assertEquals("MicroSecond", Period.MICRO_OF_SECONDS, Period.lookup("microsOfSecond"));
        Assert.assertEquals("NanoSecond", Period.NANO_OF_SECONDS, Period.lookup("nanosOfSecond"));
    }


    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_unexpected() {
        Assert.assertThrows("Y", DateTimeException.class, () -> Period.lookup("Y"));         // short case check
        Assert.assertThrows("Year", DateTimeException.class, () -> Period.lookup("Year"));   // long case check
        Assert.assertThrows("Years", DateTimeException.class, () -> Period.lookup("Years")); // long case plural check
        Assert.assertThrows("?", DateTimeException.class, () -> Period.lookup("?"));         // unexpected
    }

    /**
     * Unit test {@link Period#getChronoUnit()}
     */
    @Test
    public void test_getChronoUnit() {
        Assert.assertEquals("YEAR", ChronoUnit.YEARS, Period.YEAR.getChronoUnit());
        Assert.assertEquals("MONTH", ChronoUnit.MONTHS, Period.MONTH.getChronoUnit());
        Assert.assertEquals("WEEK", ChronoUnit.WEEKS, Period.WEEK.getChronoUnit());
        Assert.assertEquals("DAY", ChronoUnit.DAYS, Period.DAY.getChronoUnit());
        Assert.assertEquals("HOUR", ChronoUnit.HOURS, Period.HOUR.getChronoUnit());
        Assert.assertEquals("SECOND", ChronoUnit.SECONDS, Period.SECOND.getChronoUnit());
        Assert.assertEquals("MILLI", ChronoUnit.MILLIS, Period.MILLI.getChronoUnit());
        Assert.assertEquals("MICRO", ChronoUnit.MICROS, Period.MICRO.getChronoUnit());
        Assert.assertEquals("NANO", ChronoUnit.NANOS, Period.NANO.getChronoUnit());

        Assert.assertEquals("MILLI_OF_SECONDS", ChronoUnit.MILLIS, Period.MILLI_OF_SECONDS.getChronoUnit());
        Assert.assertEquals("MICRO_OF_SECONDS", ChronoUnit.MICROS, Period.MICRO_OF_SECONDS.getChronoUnit());
        Assert.assertEquals("NANO_OF_SECONDS", ChronoUnit.NANOS, Period.NANO_OF_SECONDS.getChronoUnit());
    }
}