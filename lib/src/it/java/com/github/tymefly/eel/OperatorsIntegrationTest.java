package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Functional testing of each of the EEL operators
 */
public class OperatorsIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

    private EelContext context;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitwise() {
        Assert.assertEquals("AND", 0x8, Eel.compile(context, "$( 0xc AND 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("OR", 0xe, Eel.compile(context, "$( 0xc OR 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("XOR", 0x6, Eel.compile(context, "$( 0xc XOR 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("NOT", (byte) 0xf5, Eel.compile(context, "$( NOT 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("Double NOT", (byte) 0x0a, Eel.compile(context, "$( NOT NOT 0xA )").evaluate().asNumber().byteValue());
        Assert.assertEquals("Combined",
            (byte) 0x5,
            Eel.compile(context, "$( (0xc AND NOT 0xA) OR 0x1 )").evaluate().asNumber().byteValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_logic() {
        Assert.assertFalse("AND", Eel.compile(context, "$( true & false )").evaluate().asLogic());
        Assert.assertTrue("OR", Eel.compile(context, "$( true | false )").evaluate().asLogic());
        Assert.assertFalse("Short Circuit AND", Eel.compile(context, "$( true && false )").evaluate().asLogic());
        Assert.assertTrue("Short Circuit OR", Eel.compile(context, "$( true || false )").evaluate().asLogic());
        Assert.assertTrue("NOT", Eel.compile(context, "$( ! false )").evaluate().asLogic());
        Assert.assertFalse("Double NOT", Eel.compile(context, "$( ! ! false )").evaluate().asLogic());
        Assert.assertTrue("Combined", Eel.compile(context, "$( ! false | false)").evaluate().asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ternary() {
        Result actual = Eel.compile(context, "$( (true != false) ? 'first' ~> '!' : 'second' )").evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "first!", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_relations() {
        Assert.assertTrue("= positive", Eel.compile(context, "$( 1 = 1 )").evaluate().asLogic());
        Assert.assertFalse("= negative", Eel.compile(context, "$( 1 = 2 )").evaluate().asLogic());

        Assert.assertTrue("<> positive", Eel.compile(context, "$( 1 <> 2 )").evaluate().asLogic());
        Assert.assertFalse("<> negative", Eel.compile(context, "$( 1 <> 1 )").evaluate().asLogic());

        Assert.assertTrue("!= positive", Eel.compile(context, "$( 1 != 2 )").evaluate().asLogic());
        Assert.assertFalse("!= negative", Eel.compile(context, "$( 1 != 1 )").evaluate().asLogic());

        Assert.assertTrue("> positive", Eel.compile(context, "$( 1 > -1 )").evaluate().asLogic());
        Assert.assertFalse("> negative", Eel.compile(context, "$( 1 > +1 )").evaluate().asLogic());

        Assert.assertTrue(">= positive", Eel.compile(context, "$( 1 >= 1 )").evaluate().asLogic());
        Assert.assertFalse(">= negative", Eel.compile(context, "$( 1 >= 1.1 )").evaluate().asLogic());

        Assert.assertTrue("< positive", Eel.compile(context, "$( 1 < 1.1 )").evaluate().asLogic());
        Assert.assertFalse("< negative", Eel.compile(context, "$( 1 < 0.9 )").evaluate().asLogic());

        Assert.assertTrue("<= positive", Eel.compile(context, "$( 1 <= 1 )").evaluate().asLogic());
        Assert.assertFalse("<= negative", Eel.compile(context, "$( 1 <= 0.9995 )").evaluate().asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_bitShifts() {
        Assert.assertEquals("left 1", 0x0020, Eel.compile(context, "$( 16 << 1 )").evaluate().asNumber().intValue());
        Assert.assertEquals("left 2", 0x0040, Eel.compile(context, "$( 16 << 2 )").evaluate().asNumber().intValue());
        Assert.assertEquals("left 5", 0x0200, Eel.compile(context, "$( 16 << 5 )").evaluate().asNumber().intValue());

        Assert.assertEquals("right 1", 0x0008, Eel.compile(context, "$( 16 >> 1 )").evaluate().asNumber().intValue());
        Assert.assertEquals("right 2", 0x0004, Eel.compile(context, "$( 16 >> 2 )").evaluate().asNumber().intValue());
        Assert.assertEquals("right 5", 0x0000, Eel.compile(context, "$( 16 >> 5 )").evaluate().asNumber().intValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maths() {
        Assert.assertEquals("Plus", 26, Eel.compile(context, "$( 5 + 21 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Minus", -16, Eel.compile(context, "$( 5 - 21 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Multiply", 42, Eel.compile(context, "$( 6 * 7 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Divide", 6, Eel.compile(context, "$( 42 / 7 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Modulus", 3, Eel.compile(context, "$( 45 % 7 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Power", 4, Eel.compile(context, "$( 0.5 ^ -2 )").evaluate().asNumber().intValue());
        Assert.assertEquals("Root", new BigDecimal("8.000000000000000"), Eel.compile(context, "$( 64 ^ 0.5 )").evaluate().asNumber());
        Assert.assertEquals("combined", 18, Eel.compile(context, "$( (8/2) ^ (3-1) + 2)").evaluate().asNumber().intValue());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_parsePriorities() {
        Assert.assertEquals("- (true ? 1 : 0)", new BigDecimal("-1"), Eel.compile(context, "$( - (true ? 1 : 0) )").evaluate().asNumber());
        Assert.assertEquals("- NOT 5", new BigDecimal("6"), Eel.compile(context, "$( - NOT 5 )").evaluate().asNumber());
        Assert.assertEquals("NOT -5", new BigDecimal("4"), Eel.compile(context, "$( NOT -5 )").evaluate().asNumber());
        Assert.assertEquals("1 - -5", new BigDecimal("6"), Eel.compile(context, "$( 1 - -5 )").evaluate().asNumber());
        Assert.assertEquals("2 * -10", new BigDecimal("-20"), Eel.compile(context, "$( 2 * -10 )").evaluate().asNumber());
        Assert.assertEquals("e ^ -pi", new BigDecimal("0.04321391826377227"), Eel.compile(context, "$( e ^ -pi )").evaluate().asNumber());
    }
}
