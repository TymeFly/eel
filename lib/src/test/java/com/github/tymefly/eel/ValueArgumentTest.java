package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ValueArgument}
 */
public class ValueArgumentTest {
    private SymbolsTable symbolsTable;

    private ValueArgument empty;
    private ValueArgument text;
    private ValueArgument number;
    private ValueArgument logic;
    private ValueArgument date;



    @Before
    public void setUp() {
        symbolsTable = mock();

        empty = new ValueArgument(s -> Value.of(""), symbolsTable);
        text = new ValueArgument(s -> Value.of("1234"), symbolsTable);
        number = new ValueArgument(s -> Value.of(456.789), symbolsTable);
        logic = new ValueArgument(s -> Value.TRUE, symbolsTable);
        date = new ValueArgument(s -> Value.EPOCH_START_UTC, symbolsTable);
    }


    /**
     * Unit test {@link ValueArgument#getType}
     */
    @Test
    public void test_getType() {
        Assert.assertEquals("empty", Type.TEXT, empty.getType());
        Assert.assertEquals("text", Type.TEXT, text.getType());
        Assert.assertEquals("number", Type.NUMBER, number.getType());
        Assert.assertEquals("logic", Type.LOGIC, logic.getType());
        Assert.assertEquals("date", Type.DATE, date.getType());
    }


    /**
     * Unit test {@link ValueArgument#evaluate(SymbolsTable)}
     */
    @Test
    public void test_evaluate() {
        Assert.assertEquals("empty", "", empty.evaluate(symbolsTable).asText());
        Assert.assertEquals("text", "1234", text.evaluate(symbolsTable).asText());
        Assert.assertEquals("number", new BigDecimal("456.789"), number.evaluate(symbolsTable).asNumber());
        Assert.assertEquals("logic", true, logic.evaluate(symbolsTable).asLogic());
        Assert.assertEquals("date", 0, date.evaluate(symbolsTable).asDate().toEpochSecond());
    }

    /**
     * Unit test {@link ValueArgument#evaluate(SymbolsTable)}
     */
    @Test
    public void test_evaluate_cached() {
        Term term = mock();
        Value expected = Value.TEN;

        when(term.evaluate(any(SymbolsTable.class)))
            .thenReturn(expected);

        ValueArgument value = new ValueArgument(term, symbolsTable);
        Value actual = value.evaluate(symbolsTable);

        Assert.assertSame("#1 Unexpected Value", expected, actual);
        verify(term, times(1)).evaluate(symbolsTable);

        actual = value.evaluate(symbolsTable);

        Assert.assertSame("#2 Unexpected Value", expected, actual);
        verify(term, times(1)).evaluate(symbolsTable);
    }


    /**
     * Unit test {@link ValueArgument#asText}
     */
    @Test
    public void test_asText() {
        Assert.assertEquals("empty", "", empty.evaluate(symbolsTable).asText());
        Assert.assertEquals("text", "1234", text.evaluate(symbolsTable).asText());
        Assert.assertEquals("number", "456.789", number.evaluate(symbolsTable).asText());
        Assert.assertEquals("logic", "true", logic.evaluate(symbolsTable).asText());
        Assert.assertEquals("date", "1970-01-01T00:00:00Z", date.evaluate(symbolsTable).asText());
    }

    /**
     * Unit test {@link ValueArgument#asNumber}
     */
    @Test
    public void test_asNumber() {
        Assert.assertEquals("text", new BigDecimal("1234"), text.evaluate(symbolsTable).asNumber());
        Assert.assertEquals("number", new BigDecimal("456.789"), number.evaluate(symbolsTable).asNumber());
        Assert.assertEquals("logic", BigDecimal.ONE, logic.evaluate(symbolsTable).asNumber());
        Assert.assertEquals("date", BigDecimal.ZERO, date.evaluate(symbolsTable).asNumber());
    }

    /**
     * Unit test {@link ValueArgument#asLogic}
     */
    @Test
    public void test_asLogic() {
        Assert.assertFalse("empty", empty.evaluate(symbolsTable).asLogic());
        Assert.assertTrue("number", number.evaluate(symbolsTable).asLogic());
        Assert.assertTrue("logic", logic.evaluate(symbolsTable).asLogic());
        Assert.assertFalse("date", date.evaluate(symbolsTable).asLogic());
    }

    /**
     * Unit test {@link ValueArgument#asDate}
     */
    @Test
    public void test_asDate() {
        Assert.assertEquals("text",
            ZonedDateTime.of(1234, 1, 1, 0, 0, 0, 0, ZoneOffset.of("Z")),
            text.evaluate(symbolsTable).asDate());
        Assert.assertEquals("number",
            ZonedDateTime.of(1970, 1, 1, 0, 7, 36, 789_000_000, ZoneOffset.of("Z")),
            number.evaluate(symbolsTable).asDate());
        Assert.assertEquals("logic",
            ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.of("Z")),
            logic.evaluate(symbolsTable).asDate());
        Assert.assertEquals("date",
            ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.of("Z")),
            date.evaluate(symbolsTable).asDate());
    }


    /**
     * Unit test {@link AbstractValue#asBigInteger}
     */
    @Test
    public void test_asBigInteger() {
        Assert.assertEquals("text", new BigInteger("1234"), text.evaluate(symbolsTable).asBigInteger());
        Assert.assertEquals("number", new BigInteger("456"), number.evaluate(symbolsTable).asBigInteger());
        Assert.assertEquals("logic", BigInteger.ONE, logic.evaluate(symbolsTable).asBigInteger());
        Assert.assertEquals("date", BigInteger.ZERO, date.evaluate(symbolsTable).asBigInteger());
    }

    /**
     * Unit test {@link AbstractValue#asDouble}
     */
    @Test
    public void test_asDouble() {
        Assert.assertEquals("text", 1234.0, text.evaluate(symbolsTable).asDouble(), 0.001);
        Assert.assertEquals("number", 456.789, number.evaluate(symbolsTable).asDouble(), 0.001);
        Assert.assertEquals("logic", 1.0, logic.evaluate(symbolsTable).asDouble(), 0.001);
        Assert.assertEquals("date", 0.0, date.evaluate(symbolsTable).asDouble(), 0.001);
    }

    /**
     * Unit test {@link AbstractValue#asLong}
     */
    @Test
    public void test_asLong() {
        Assert.assertEquals("text", 1234L, text.evaluate(symbolsTable).asLong());
        Assert.assertEquals("number", 456L, number.evaluate(symbolsTable).asLong());
        Assert.assertEquals("logic", 1L, logic.evaluate(symbolsTable).asLong());
        Assert.assertEquals("date", 0L, date.evaluate(symbolsTable).asLong());
    }

    /**
     * Unit test {@link AbstractValue#asInt}
     */
    @Test
    public void test_asInt() {
        Assert.assertEquals("text", 1234, text.evaluate(symbolsTable).asInt());
        Assert.assertEquals("number", 456, number.evaluate(symbolsTable).asInt());
        Assert.assertEquals("logic", 1, logic.evaluate(symbolsTable).asInt());
        Assert.assertEquals("date", 0, date.evaluate(symbolsTable).asInt());
    }

    /**
     * Unit test {@link Constant#asChar()}
     */
    @Test
    public void test_asChar() {
        Assert.assertEquals("text", '1', text.evaluate(symbolsTable).asChar());
        Assert.assertEquals("number", '4', number.evaluate(symbolsTable).asChar());
        Assert.assertEquals("logic", 't', logic.evaluate(symbolsTable).asChar());
        Assert.assertEquals("date", '1', date.evaluate(symbolsTable).asChar());

        Assert.assertThrows("empty", EelConvertException.class, () -> empty.evaluate(symbolsTable).asChar());
    }


    /**
     * Unit test {@link Constant#asFile()}
     */
    @Test
    public void test_asFile() throws Exception {
        Assert.assertEquals("Happy path",
            "myFile.txt",
            new ValueArgument(s -> Value.of("myFile.txt"), symbolsTable).asFile().getName());
    }
}