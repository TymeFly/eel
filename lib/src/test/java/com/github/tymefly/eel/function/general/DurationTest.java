package com.github.tymefly.eel.function.general;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Duration}
 */
public class DurationTest {
    private final Duration ops = new Duration();

    /**
     * Unit test {@link Duration#duration(ZonedDateTime, ZonedDateTime, String)}
     */
    @Test
    public void test_Duration() {
        ZonedDateTime from = ZonedDateTime.of(2000, 12, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
        ZonedDateTime to = ZonedDateTime.of(2002, 3, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);

        Assert.assertEquals("In years", 1, ops.duration(from, to, "years"));
        Assert.assertEquals("In months", 15, ops.duration(from, to, "months"));
        Assert.assertEquals("In weeks", 65, ops.duration(from, to, "weeks"));
        Assert.assertEquals("In days", 455, ops.duration(from, to, "days"));
        Assert.assertEquals("In hours", 10920, ops.duration(from, to, "hours"));
        Assert.assertEquals("In minutes", 655200, ops.duration(from, to, "minutes"));
        Assert.assertEquals("In seconds", 39312000, ops.duration(from, to, "seconds"));
    }
}