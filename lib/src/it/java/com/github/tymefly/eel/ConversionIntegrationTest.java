package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Test for type conversions
 */
@ExtendWith(SystemStubsExtension.class)
public class ConversionIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    private static final ZonedDateTime DATE_TRUE = ZonedDateTime.of(1970, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);

    private EelContext context;


    @BeforeEach
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Text() {
        Result actual = Eel.compile(context, "$( 2 + 3 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertEquals("5", actual.asText(), "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_BigNumber_To_Text() {
        Result actual = Eel.compile(context, "$( 3 * 10 ** 8 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertEquals("300000000", actual.asText(), "Unexpected value");      // Plain string (Not Sci Notation)
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Text() {
        Result actual = Eel.compile(context, "$( true and false )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertEquals("false", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Text() {
        Result actual = Eel.compile(context, "$( date.utc() )").evaluate();
        String text = actual.asText();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertTrue(actual.asText().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z"),
            "Unexpected value: " + text);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Number() {
        Result actual = Eel.compile(context, "$( '4' ~> '.5' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals(new BigDecimal("4.5"), actual.asNumber(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Number_invalid() {
        Result actual = Eel.compile(context, "$( 'He' ~> 'llo' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertThrows(EelConvertException.class, actual::asNumber, "Inconvertible value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Number() {
        Result actual = Eel.compile(context, "$( true and false )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertEquals(BigDecimal.ZERO, actual.asNumber(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Number() {
        Result actual = Eel.compile(context, "$( date.utc() )").evaluate();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertTrue(actual.asLong() > 100, "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Number_HandleZones() {
        Result first = Eel.compile(context, "$( date.start() )").evaluate();
        Result second = Eel.compile(context, "$( date.start('+1') )").evaluate();

        // All the preconditions
        assertEquals(Type.DATE, first.getType(), "first: Unexpected type");
        assertEquals(Type.DATE, second.getType(), "second: Unexpected type");

        assertEquals(ZoneOffset.ofHours(0), first.asDate().getOffset(), "first: Unexpected zone");
        assertEquals(ZoneOffset.ofHours(1), second.asDate().getOffset(), "second: Unexpected zone");

        assertEquals(first.asDate().toInstant(), second.asDate().toInstant(), "Expected same instant");

        // The real test!
        assertEquals(first.asNumber(), second.asNumber(), "Expected same number");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Logic() {
        Result actual = Eel.compile(context, "$( 'tr' ~> 'ue' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Logic_invalid() {
        Result actual = Eel.compile(context, "$( 'enabled' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertThrows(EelConvertException.class, actual::asLogic, "Inconvertible value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Logic() {
        Result actual = Eel.compile(context, "$( 2 - 1 - 1 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertFalse(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Logic_true() {
        Result actual = Eel.compile(context, "$( 2 + 1 + 1 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Logic_false() {
        Result actual = Eel.compile(context, "$( 2 - 1 - 1 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertFalse(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Logic_true() {
        Result actual = Eel.compile(context, "$( date.utc() )").evaluate();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Logic_false() {
        Result actual = Eel.compile(context, "$( date(0) )").evaluate();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertFalse(actual.asLogic(), "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Date() {
        Result actual = Eel.compile(context, "$( '2000-01-02' ~> 'T' ~> '03:04:05Z' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate(),
            "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Date_WithMicro() {
        Result actual = Eel.compile(context, "$( '2000-01-02' ~> 'T' ~> '03:04:05.6789Z' )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 678_900_000, ZoneOffset.UTC),
            actual.asDate(),
            "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Date() {
        Result actual = Eel.compile(context, "$( 9467822450 / 10 )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertEquals(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate(),
            "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Date_true() {
        Result actual = Eel.compile(context, "$( true or false )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertEquals(DATE_TRUE, actual.asDate(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Date_false() {
        Result actual = Eel.compile(context, "$( true and false )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertEquals(EelContext.FALSE_DATE, actual.asDate(), "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_Operator() {
        Result actual = Eel.compile(context, "$( text( 1 + 2 ) )").evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected type");
        assertEquals("3", actual.asText(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_Operator() {
        Result actual = Eel.compile(context, "$( number( true ) )").evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected type");
        assertEquals(BigDecimal.ONE, actual.asNumber(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_Operator_invalid() {
        Eel expression = Eel.compile(context, "$( number( 'text' ) )");

        assertThrows(EelConvertException.class, expression::evaluate);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_Operator() {
        Result actual = Eel.compile(context, "$( logic( 'true' ) )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_Operator_number() {
        Result actual = Eel.compile(context, "$( logic( 12345 ) )").evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected type");
        assertTrue(actual.asLogic(), "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_Operator_invalid() {
        Eel expression = Eel.compile(context, "$( logic( 'text' ) )");

        assertThrows(EelConvertException.class, expression::evaluate);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_Operator_FromNumber() {
        Result actual = Eel.compile(context, "$( date( 946782240 + 5 ) )").evaluate();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertEquals(ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate(),
            "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_Operator_FromLogic() {
        Result actual = Eel.compile(context, "$( date( true or false ) )").evaluate();

        assertEquals(Type.DATE, actual.getType(), "Unexpected type");
        assertEquals(DATE_TRUE, actual.asDate(), "Unexpected value");
    }
}
