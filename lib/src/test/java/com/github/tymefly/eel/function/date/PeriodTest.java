package com.github.tymefly.eel.function.date;

import java.time.temporal.ChronoUnit;

import com.github.tymefly.eel.exception.EelFunctionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Period}
 */
public class PeriodTest {

    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_shortForm() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("y"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("w"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("M"));
        Assert.assertEquals("MINUTE", Period.MINUTE, Period.lookup("m"));
    }

    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("year"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("week"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("month"));
        Assert.assertEquals("MINUTE", Period.MINUTE, Period.lookup("minute"));
    }


    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_longForm_plural() {
        Assert.assertEquals("Year", Period.YEAR, Period.lookup("years"));
        Assert.assertEquals("Week", Period.WEEK, Period.lookup("weeks"));
        Assert.assertEquals("Month", Period.MONTH, Period.lookup("months"));
        Assert.assertEquals("MINUTE", Period.MINUTE, Period.lookup("minutes"));
    }

    /**
     * Unit test {@link Period#lookup(String)}
     */
    @Test
    public void test_lookup_unexpected() {
        Assert.assertThrows("Y", EelFunctionException.class, () -> Period.lookup("Y"));         // short case check
        Assert.assertThrows("Year", EelFunctionException.class, () -> Period.lookup("Year"));   // long case check
        Assert.assertThrows("Years", EelFunctionException.class, () -> Period.lookup("Years")); // long case plural check
        Assert.assertThrows("?", EelFunctionException.class, () -> Period.lookup("?"));         // unexpected
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
    }
}