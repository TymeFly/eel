package com.github.tymefly.eel.function.general;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link Dates}
 */
public class DatesTest {
    private final Dates ops = new Dates();

    /**
     * Unit test {@link Dates#duration(ZonedDateTime, ZonedDateTime, String)}
     */
    @Test
    public void test_Duration() {
        ZonedDateTime from = ZonedDateTime.of(2000, 12, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
        ZonedDateTime to = ZonedDateTime.of(2002, 3, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);

        assertEquals(1, ops.duration(from, to, "years"), "In years");
        assertEquals(15, ops.duration(from, to, "months"), "In months");
        assertEquals(65, ops.duration(from, to, "weeks"), "In weeks");
        assertEquals(455, ops.duration(from, to, "days"), "In days");
        assertEquals(10920, ops.duration(from, to, "hours"), "In hours");
        assertEquals(655200, ops.duration(from, to, "minutes"), "In minutes");
        assertEquals(39312000, ops.duration(from, to, "seconds"), "In seconds");
    }
}