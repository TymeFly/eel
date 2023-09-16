package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link EelValue}
 */
public class EelValueTest {

    /**
     * Unit test {@link EelValue#of(String)} 
     */
    @Test
    public void test_of_String() {
        EelValue actual = EelValue.of("0");

        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "0", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ZERO, actual.asNumber());
        Assert.assertThrows("Unexpected Date", EelConvertException.class, actual::asDate);
        Assert.assertFalse("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(Number)} 
     */
    @Test
    public void test_of_Number() {
        EelValue actual = EelValue.of(1);

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Text", "1", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 1, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(boolean)}
     */
    @Test
    public void test_of_bool() {
        EelValue actual = EelValue.of(true);

        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected Text", "true", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.ONE, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 1, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }

    /**
     * Unit test {@link EelValue#of(ZonedDateTime)} 
     */
    @Test
    public void test_of_time() {
        // Nanos will be ignored
        ZonedDateTime start = ZonedDateTime.of(1970, 1, 1, 0, 0, 10, 1234, ZoneId.of("UTC"));
        EelValue actual = EelValue.of(start);

        Assert.assertEquals("Unexpected Type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected Text", "1970-01-01T00:00:10Z", actual.asText());
        Assert.assertEquals("Unexpected Number", BigDecimal.TEN, actual.asNumber());
        Assert.assertEquals("Unexpected Date", 10, actual.asDate().getSecond());
        Assert.assertTrue("Unexpected Logic", actual.asLogic());
    }
}