package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Integration Test for type conversions
 */
public class ConversionIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();



    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Text() {
        Result actual = Eel.compile("$( 2 + 3 )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value", "5", actual.asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_BigNumber_To_Text() {
        Result actual = Eel.compile("$( 3 * 10 ^ 8 )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value", "300000000", actual.asText());      // Plain string (Not Sci Notation)
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Text() {
        Result actual = Eel.compile("$( true & false )").evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected value", "false", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Text() {
        Result actual = Eel.compile("$( date.utc() )").evaluate();
        String text = actual.asText();

        Assert.assertEquals("Unexpected type", Type.DATE, actual.getType());
        Assert.assertTrue("Unexpected value: " + text,
            actual.asText().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Number() {
        Result actual = Eel.compile("$( '4' ~> '.5' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", new BigDecimal("4.5"), actual.asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Number_invalid() {
        Result actual = Eel.compile("$( 'He' ~> 'llo' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertThrows("Inconvertible value", EelConvertException.class, actual::asNumber);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Number() {
        Result actual = Eel.compile("$( true & false )").evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertEquals("Unexpected value", BigDecimal.ZERO, actual.asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Number() {
        Result actual = Eel.compile("$( date.utc() )").evaluate();

        Assert.assertEquals("Unexpected type", Type.DATE, actual.getType());
        Assert.assertTrue("Unexpected value", actual.asNumber().longValue() > 100);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Number_HandleZones() {
        Result first = Eel.compile("$( date.start() )").evaluate();
        Result second = Eel.compile("$( date.start('+1') )").evaluate();

        // All the preconditions
        Assert.assertEquals("first: Unexpected type", Type.DATE, first.getType());
        Assert.assertEquals("second: Unexpected type", Type.DATE, second.getType());

        Assert.assertEquals("first: Unexpected zone", ZoneOffset.ofHours(0), first.asDate().getOffset());
        Assert.assertEquals("second: Unexpected zone", ZoneOffset.ofHours(1), second.asDate().getOffset());

        Assert.assertEquals("Expected same instant", first.asDate().toInstant(), second.asDate().toInstant());

        // The real test!
        Assert.assertEquals("Expected same number", first.asNumber(), second.asNumber());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Logic() {
        Result actual = Eel.compile("$( 'tr' ~> 'ue' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertTrue("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Logic_invalid() {
        Result actual = Eel.compile("$( 'enabled' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertThrows("Inconvertible value", EelConvertException.class, actual::asLogic);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Logic() {
        Result actual = Eel.compile("$( 2 - 1 - 1 )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertFalse("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Logic_invalid() {
        Result actual = Eel.compile("$( 2 + 1 + 1 )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertThrows("Inconvertible value", EelConvertException.class, actual::asLogic);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_To_Logic_invalid() {
        Result actual = Eel.compile("$( date.utc() )").evaluate();

        Assert.assertEquals("Unexpected type", Type.DATE, actual.getType());
        Assert.assertThrows("Inconvertible value", EelConvertException.class, actual::asLogic);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_To_Date() {
        Result actual = Eel.compile("$( '2000-01-02' ~> 'T' ~> '03:04:05Z' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value",
            ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_To_Date() {
        Result actual = Eel.compile("$( 9467822450 / 10 )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value",
            ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_To_Date() {
        Result actual = Eel.compile("$( true & false )").evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertThrows("Inconvertible value", EelConvertException.class, actual::asDate);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Text_Operator() {
        Result actual = Eel.compile("$( TEXT( 1 + 2 ) )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "3", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_Operator() {
        Result actual = Eel.compile("$( NUMBER( true ) )").evaluate();

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value", BigDecimal.ONE, actual.asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Number_Operator_invalid() {
        Eel expression = Eel.compile("$( NUMBER( 'text' ) )");

        Assert.assertThrows(EelConvertException.class, expression::evaluate);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_Operator() {
        Result actual = Eel.compile("$( LOGIC( 'true' ) )").evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Logic_Operator_invalid() {
        Eel expression = Eel.compile("$( LOGIC( 'text' ) )");

        Assert.assertThrows(EelConvertException.class, expression::evaluate);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_Operator() {
        Result actual = Eel.compile("$( DATE( 946782240 + 5 ) )").evaluate();

        Assert.assertEquals("Unexpected type", Type.DATE, actual.getType());
        Assert.assertEquals("Unexpected value",
            ZonedDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC),
            actual.asDate());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Date_Operator_invalid() {
        Eel expression = Eel.compile("$( DATE( true | false ) )");

        Assert.assertThrows(EelConvertException.class, expression::evaluate);
    }
}
