package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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



    @BeforeEach
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
        assertEquals(Type.TEXT, empty.getType(), "empty");
        assertEquals(Type.TEXT, text.getType(), "text");
        assertEquals(Type.NUMBER, number.getType(), "number");
        assertEquals(Type.LOGIC, logic.getType(), "logic");
        assertEquals(Type.DATE, date.getType(), "date");
    }


    /**
     * Unit test {@link ValueArgument#evaluate(SymbolsTable)}
     */
    @Test
    public void test_evaluate() {
        assertEquals("", empty.evaluate(symbolsTable).asText(), "empty");
        assertEquals("1234", text.evaluate(symbolsTable).asText(), "text");
        assertEquals(new BigDecimal("456.789"), number.evaluate(symbolsTable).asNumber(), "number");
        assertEquals(true, logic.evaluate(symbolsTable).asLogic(), "logic");
        assertEquals(0, date.evaluate(symbolsTable).asDate().toEpochSecond(), "date");
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

        assertSame(expected, actual, "#1 Unexpected Value");
        verify(term, times(1)).evaluate(symbolsTable);

        actual = value.evaluate(symbolsTable);

        assertSame(expected, actual, "#2 Unexpected Value");
        verify(term, times(1)).evaluate(symbolsTable);
    }


    /**
     * Unit test {@link ValueArgument#asText}
     */
    @Test
    public void test_asText() {
        assertEquals("", empty.evaluate(symbolsTable).asText(), "empty");
        assertEquals("1234", text.evaluate(symbolsTable).asText(), "text");
        assertEquals("456.789", number.evaluate(symbolsTable).asText(), "number");
        assertEquals("true", logic.evaluate(symbolsTable).asText(), "logic");
        assertEquals("1970-01-01T00:00:00Z", date.evaluate(symbolsTable).asText(), "date");
    }

    /**
     * Unit test {@link ValueArgument#asNumber}
     */
    @Test
    public void test_asNumber() {
        assertEquals(new BigDecimal("1234"), text.evaluate(symbolsTable).asNumber(), "text");
        assertEquals(new BigDecimal("456.789"), number.evaluate(symbolsTable).asNumber(), "number");
        assertEquals(BigDecimal.ONE, logic.evaluate(symbolsTable).asNumber(), "logic");
        assertEquals(BigDecimal.ZERO, date.evaluate(symbolsTable).asNumber(), "date");
    }

    /**
     * Unit test {@link ValueArgument#asLogic}
     */
    @Test
    public void test_asLogic() {
        assertFalse(empty.evaluate(symbolsTable).asLogic(), "empty");
        assertTrue(number.evaluate(symbolsTable).asLogic(), "number");
        assertTrue(logic.evaluate(symbolsTable).asLogic(), "logic");
        assertFalse(date.evaluate(symbolsTable).asLogic(), "date");
    }

    /**
     * Unit test {@link ValueArgument#asDate}
     */
    @Test
    public void test_asDate() {
        assertEquals(ZonedDateTime.of(1234, 1, 1, 0, 0, 0, 0, ZoneOffset.of("Z")), text.evaluate(symbolsTable).asDate(), "text");
        assertEquals(ZonedDateTime.of(1970, 1, 1, 0, 7, 36, 789_000_000, ZoneOffset.of("Z")), number.evaluate(symbolsTable).asDate(), "number");
        assertEquals(ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.of("Z")), logic.evaluate(symbolsTable).asDate(), "logic");
        assertEquals(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.of("Z")), date.evaluate(symbolsTable).asDate(), "date");
    }


    /**
     * Unit test {@link AbstractValue#asBigInteger}
     */
    @Test
    public void test_asBigInteger() {
        assertEquals(new BigInteger("1234"), text.evaluate(symbolsTable).asBigInteger(), "text");
        assertEquals(new BigInteger("456"), number.evaluate(symbolsTable).asBigInteger(), "number");
        assertEquals(BigInteger.ONE, logic.evaluate(symbolsTable).asBigInteger(), "logic");
        assertEquals(BigInteger.ZERO, date.evaluate(symbolsTable).asBigInteger(), "date");
    }

    /**
     * Unit test {@link AbstractValue#asDouble}
     */
    @Test
    public void test_asDouble() {
        assertEquals(1234.0, text.evaluate(symbolsTable).asDouble(), 0.001, "text");
        assertEquals(456.789, number.evaluate(symbolsTable).asDouble(), 0.001, "number");
        assertEquals(1.0, logic.evaluate(symbolsTable).asDouble(), 0.001, "logic");
        assertEquals(0.0, date.evaluate(symbolsTable).asDouble(), 0.001, "date");
    }

    /**
     * Unit test {@link AbstractValue#asLong}
     */
    @Test
    public void test_asLong() {
        assertEquals(1234L, text.evaluate(symbolsTable).asLong(), "text");
        assertEquals(456L, number.evaluate(symbolsTable).asLong(), "number");
        assertEquals(1L, logic.evaluate(symbolsTable).asLong(), "logic");
        assertEquals(0L, date.evaluate(symbolsTable).asLong(), "date");
    }

    /**
     * Unit test {@link AbstractValue#asInt}
     */
    @Test
    public void test_asInt() {
        assertEquals(1234, text.evaluate(symbolsTable).asInt(), "text");
        assertEquals(456, number.evaluate(symbolsTable).asInt(), "number");
        assertEquals(1, logic.evaluate(symbolsTable).asInt(), "logic");
        assertEquals(0, date.evaluate(symbolsTable).asInt(), "date");
    }

    /**
     * Unit test {@link Constant#asChar()}
     */
    @Test
    public void test_asChar() {
        assertEquals('1', text.evaluate(symbolsTable).asChar(), "text");
        assertEquals('4', number.evaluate(symbolsTable).asChar(), "number");
        assertEquals('t', logic.evaluate(symbolsTable).asChar(), "logic");
        assertEquals('1', date.evaluate(symbolsTable).asChar(), "date");

        assertThrows(EelConvertException.class, () -> empty.evaluate(symbolsTable).asChar(), "empty");
    }
}