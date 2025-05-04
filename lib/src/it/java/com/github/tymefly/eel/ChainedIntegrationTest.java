package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;

import com.github.tymefly.eel.exception.EelSemanticException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Integration Tests that involve look backs
 */
public class ChainedIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

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
    public void test_SingleString() {
        Result actual = Eel.compile(context, "$( 'Hello World' ; $[1] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "Hello World", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString_fromSymbolsTable() {
        Result actual = Eel.compile(context, "$( ${unknown-default value} ; $[1] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "default value", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleNumber() {
        Result actual = Eel.compile(context, "$( 123 ; $[1] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected number", new BigDecimal("123"), actual.asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_MultipleLookBacks() {
        Result actual = Eel.compile(context, "$( 'Hello' ; 'world' ; 'unused' ; $[1] ~> ' ' ~> $[2] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected Text", "Hello world", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_reuseLookBacks() {
        Result actual = Eel.compile(context, "$( 123 ; $[1] + $[1] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Number", 246, actual.asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_nestedLookBacks() {
        Result actual = Eel.compile(context, "$( 10 ; $[1] + 1 ; $[2] + 2 )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected Number", 13, actual.asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_trailingSemicolon() {
        Exception actual = Assert.assertThrows(EelSyntaxException.class,
            () -> Eel.compile(context, "$( 'Hello' ;  )").evaluate());

        Assert.assertEquals("Unexpected message", "Error at position 15: ')' was unexpected", actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_skipUnusedLookBacks() {
        Result actual = Eel.compile(context, "$( true ; fail('unexpected') ; $[1] )")
            .evaluate();

        Assert.assertEquals("Unexpected Type", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected Text", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_invalidLookBack_0() {
        Exception actual = Assert.assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( true ; $[0] )").evaluate());

        Assert.assertEquals("Unexpected message", "Error at position 14: Undefined lookBack $[0]", actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_invalidLookBack_1() {
        Exception actual = Assert.assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( $[1] )").evaluate());

        Assert.assertEquals("Unexpected message", "Error at position 7: Undefined lookBack $[1]", actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_invalidLookBack_1_1() {
        Exception actual = Assert.assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( true; $[1.1 )").evaluate());

        Assert.assertEquals("Unexpected message", "Error at position 12: Invalid lookBack $1.1", actual.getMessage());
    }
}
