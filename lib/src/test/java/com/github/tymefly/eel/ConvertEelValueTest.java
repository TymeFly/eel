package com.github.tymefly.eel;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ConvertEelValue}
 */
public class ConvertEelValueTest {
    private static final ZonedDateTime DATE = ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);


    private ConvertEelValue convert;


    @Before
    public void setUp() {
        convert = new ConvertEelValue();
    }


    /**
     * Unit test {@link ConvertEelValue#toText(EelValue)}
     */
    @Test
    public void test_toText() {
        EelValue text = convert.toText(EelValue.of("Hello"));

        Assert.assertSame("Text to Text", text, text);
        Assert.assertEquals("Number to Text", Value.of("12.3"),  convert.toText(EelValue.of(12.3)));
        Assert.assertEquals("Logic to Text", Value.of("true"), convert.toText(EelValue.of(true)));
        Assert.assertEquals("Date to Text", Value.of("1970-01-01T00:00:00Z"), convert.toText(EelValue.EPOCH_START_UTC));
    }


    /**
     * Unit test {@link ConvertEelValue#toNumber(EelValue)}
     */
    @Test
    public void test_toNumber() {
        EelValue number = convert.toNumber(EelValue.of(12.3));

        Assert.assertEquals("Text to Number", Value.of(123.456), convert.toNumber(EelValue.of("123.456")));
        Assert.assertSame("Number to Number", number, number);
        Assert.assertEquals("Logic to Number", Value.of(1), convert.toNumber(EelValue.of(true)));
        Assert.assertEquals("Date to Number", EelValue.ZERO, convert.toNumber(EelValue.EPOCH_START_UTC));
    }

    /**
     * Unit test {@link ConvertEelValue#toLogic(EelValue)}
     */
    @Test
    public void test_toLogic() {
        EelValue logic = convert.toLogic(EelValue.of(true));

        Assert.assertEquals("Text to Logic", EelValue.FALSE, convert.toLogic(EelValue.of("false")));
        Assert.assertEquals("Number to Logic", EelValue.TRUE,  convert.toLogic(EelValue.of(1)));
        Assert.assertSame("Logic to Logic", logic, logic);
        Assert.assertEquals("Date to Logic", EelValue.FALSE, convert.toLogic(EelValue.EPOCH_START_UTC));
    }

    /**
     * Unit test {@link ConvertEelValue#toDate(EelValue)}
     */
    @Test
    public void test_toDate() {
        EelValue date = convert.toDate(EelValue.EPOCH_START_UTC);

        Assert.assertEquals("Text to Date", Value.of(DATE), convert.toDate(EelValue.of("2000-01-02T03:04:05Z")));
        Assert.assertEquals("Number to Date", Value.of(DATE),  convert.toDate(EelValue.of(946782245)));
        Assert.assertEquals("Logic to Date", EelValue.EPOCH_START_UTC, convert.toDate(EelValue.of(false)));
        Assert.assertSame("Date to Date", date, date);
    }
}