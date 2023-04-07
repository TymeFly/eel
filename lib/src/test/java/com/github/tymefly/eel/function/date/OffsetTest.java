package com.github.tymefly.eel.function.date;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Offset}
 */
public class OffsetTest {
    private ZonedDateTime date;

    @Before
    public void setUp() {
        ZoneId zone = ZoneId.of("UTC");
        Instant instant = Instant.ofEpochSecond(1196702100);

        date = ZonedDateTime.ofInstant(instant, zone);
    }

    /**
     * Unit test {@link Offset#offset(ZonedDateTime, String...)}
     */
    @Test
    public void test_offset_noOffsets() {
        Assert.assertEquals("no offsets",
            date,
            new Offset().offset(date));
    }

    /**
     * Unit test {@link Offset#offset(ZonedDateTime, String...)}
     */
    @Test
    public void test_offset_singleOffset() {
        Assert.assertEquals("no offsets",
            date.plusHours(1),
            new Offset().offset(date, "1h"));
    }

    /**
     * Unit test {@link Offset#offset(ZonedDateTime, String...)}
     */
    @Test
    public void test_offset_multipleOffsets() {
        Assert.assertEquals("no offsets",
            date.plusMinutes(57),
            new Offset().offset(date, "1h", "-3m"));
    }
}