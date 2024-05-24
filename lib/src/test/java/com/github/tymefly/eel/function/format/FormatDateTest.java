package com.github.tymefly.eel.function.format;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.function.date.DateFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FormatDate}
 */
public class FormatDateTest {
    private EelContext context;

    private FormatDate formatDate;



    @Before
    public void setUp() {
        context = EelContext.factory().build();

        DateFactory dateFactory = mock();

        when(dateFactory.start(any(EelContext.class), anyString(), any(String[].class)))
            .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1196702100), ZoneId.of("UTC")));
        when(dateFactory.utc(any(String[].class)))
            .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1196702100), ZoneId.of("UTC")));
        when(dateFactory.local(any(String[].class)))
            .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1196702100), ZoneId.of("+1")));
        when(dateFactory.at(anyString(), any(String[].class)))
            .thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(1196702100), ZoneId.of("America/New_York")));

        formatDate = new FormatDate(dateFactory);
    }


    /**
     * Unit test {@link FormatDate#formatStart(EelContext, String, String, String...)}
     */
    @Test
    public void test_FormatStart() {
        Assert.assertEquals("UTC Format",
            "12/03/2007 17:15 +0000",
            formatDate.formatStart(context, "MM/dd/yyy HH:mm Z", "UTC"));
    }

    /**
     * Unit test {@link FormatDate#formatDate(String, ZonedDateTime, String...)}
     */
    @Test
    public void test_FormatDate() {
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1196702100), ZoneId.of("America/New_York"));

        Assert.assertEquals("US Format",
            "12/03/2007 12:15 -0500",
            formatDate.formatDate("MM/dd/yyy HH:mm Z", date));
    }

    /**
     * Unit test {@link FormatDate#formatUtc(String, String...)}
     */
    @Test
    public void test_FormatUtc() {
        Assert.assertEquals("UTC Format",
            "12/03/2007 17:15 +0000",
            formatDate.formatUtc("MM/dd/yyy HH:mm Z"));
    }

    /**
     * Unit test {@link FormatDate#formatLocal(String, String...)}
     */
    @Test
    public void test_FormatLocal() {
        Assert.assertEquals("Local Format",
            "12/03/2007 18:15 +0100",
            formatDate.formatLocal("MM/dd/yyy HH:mm Z"));
    }

    /**
     * Unit test {@link FormatDate#formatAt(String, String, String...)}
     */
    @Test
    public void test_FormatAt() {
        Assert.assertEquals("Custom TimeZone Format",
            "12/03/2007 12:15 -0500",
            formatDate.formatAt("America/New_York", "MM/dd/yyy HH:mm Z"));
    }
}