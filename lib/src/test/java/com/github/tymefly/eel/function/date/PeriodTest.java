package com.github.tymefly.eel.function.date;

import java.time.DateTimeException;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link Period}
 */
public class PeriodTest {

    /**
     * Unit test {@link Period#shortForm()}
     */
    @Test
    public void test_shortForm() {
        assertEquals("y", Period.YEAR.shortForm(), "Year");
        assertEquals("w", Period.WEEK.shortForm(), "Week");
        assertEquals("M", Period.MONTH.shortForm(), "Month");
        assertEquals("m", Period.MINUTE.shortForm(), "Minute");
        assertEquals("I", Period.MILLI.shortForm(), "Milli");
        assertEquals("U", Period.MICRO.shortForm(), "Micro");
        assertEquals("N", Period.NANO.shortForm(), "Nano");

        assertEquals("i", Period.MILLI_OF_SECONDS.shortForm(), "MilliOfSeconds");
        assertEquals("u", Period.MICRO_OF_SECONDS.shortForm(), "MicroOfSeconds");
        assertEquals("n", Period.NANO_OF_SECONDS.shortForm(), "NanoOfSecond");
    }
    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_shortForm() {
        assertEquals(Period.YEAR, Period.lookup("y"), "Year");
        assertEquals(Period.WEEK, Period.lookup("w"), "Week");
        assertEquals(Period.MONTH, Period.lookup("M"), "Month");
        assertEquals(Period.MINUTE, Period.lookup("m"), "Minute");
        assertEquals(Period.MILLI, Period.lookup("I"), "Milli");
        assertEquals(Period.MICRO, Period.lookup("U"), "Micro");
        assertEquals(Period.NANO, Period.lookup("N"), "Nano");

        assertEquals(Period.MILLI_OF_SECONDS, Period.lookup("i"), "MilliOfSeconds");
        assertEquals(Period.MICRO_OF_SECONDS, Period.lookup("u"), "MicroOfSeconds");
        assertEquals(Period.NANO_OF_SECONDS, Period.lookup("n"), "NanoOfSecond");
    }

    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm() {
        assertEquals(Period.YEAR, Period.lookup("year"), "Year");
        assertEquals(Period.WEEK, Period.lookup("week"), "Week");
        assertEquals(Period.MONTH, Period.lookup("month"), "Month");
        assertEquals(Period.MINUTE, Period.lookup("minute"), "Minute");
        assertEquals(Period.MILLI, Period.lookup("milli"), "Milli");
        assertEquals(Period.MICRO, Period.lookup("micro"), "Micro");
        assertEquals(Period.NANO, Period.lookup("nano"), "Nano");

        assertEquals(Period.MILLI_OF_SECONDS, Period.lookup("milliOfSecond"), "MilliOfSeconds");
        assertEquals(Period.MICRO_OF_SECONDS, Period.lookup("microOfSecond"), "MicroOfSecond");
        assertEquals(Period.NANO_OF_SECONDS, Period.lookup("nanoOfSecond"), "NanoOfSecond");
    }


    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm_plural() {
        assertEquals(Period.YEAR, Period.lookup("years"), "Year");
        assertEquals(Period.WEEK, Period.lookup("weeks"), "Week");
        assertEquals(Period.MONTH, Period.lookup("months"), "Month");
        assertEquals(Period.MINUTE, Period.lookup("minutes"), "Minute");
        assertEquals(Period.MILLI, Period.lookup("millis"), "Milli");
        assertEquals(Period.MICRO, Period.lookup("micros"), "Micro");
        assertEquals(Period.NANO, Period.lookup("nanos"), "Nano");

        assertEquals(Period.MILLI_OF_SECONDS, Period.lookup("millisOfSecond"), "MilliOfSeconds");
        assertEquals(Period.MICRO_OF_SECONDS, Period.lookup("microsOfSecond"), "MicroSecond");
        assertEquals(Period.NANO_OF_SECONDS, Period.lookup("nanosOfSecond"), "NanoSecond");
    }


    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_unexpected() {
        assertThrows(DateTimeException.class, () -> Period.lookup("Y"), "Y");         // short case check
        assertThrows(DateTimeException.class, () -> Period.lookup("Year"), "Year");   // long case check
        assertThrows(DateTimeException.class, () -> Period.lookup("Years"), "Years"); // long case plural check
        assertThrows(DateTimeException.class, () -> Period.lookup("?"), "?");         // unexpected
    }

    /**
     * Unit test {@link Period#getChronoUnit()}
     */
    @Test
    public void test_getChronoUnit() {
        assertEquals(ChronoUnit.YEARS, Period.YEAR.getChronoUnit(), "YEAR");
        assertEquals(ChronoUnit.MONTHS, Period.MONTH.getChronoUnit(), "MONTH");
        assertEquals(ChronoUnit.WEEKS, Period.WEEK.getChronoUnit(), "WEEK");
        assertEquals(ChronoUnit.DAYS, Period.DAY.getChronoUnit(), "DAY");
        assertEquals(ChronoUnit.HOURS, Period.HOUR.getChronoUnit(), "HOUR");
        assertEquals(ChronoUnit.SECONDS, Period.SECOND.getChronoUnit(), "SECOND");
        assertEquals(ChronoUnit.MILLIS, Period.MILLI.getChronoUnit(), "MILLI");
        assertEquals(ChronoUnit.MICROS, Period.MICRO.getChronoUnit(), "MICRO");
        assertEquals(ChronoUnit.NANOS, Period.NANO.getChronoUnit(), "NANO");

        assertEquals(ChronoUnit.MILLIS, Period.MILLI_OF_SECONDS.getChronoUnit(), "MILLI_OF_SECONDS");
        assertEquals(ChronoUnit.MICROS, Period.MICRO_OF_SECONDS.getChronoUnit(), "MICRO_OF_SECONDS");
        assertEquals(ChronoUnit.NANOS, Period.NANO_OF_SECONDS.getChronoUnit(), "NANO_OF_SECONDS");
    }
}