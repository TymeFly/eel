package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;

import com.github.tymefly.eel.exception.EelSemanticException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Tests that involve look backs
 */
@ExtendWith(SystemStubsExtension.class)
public class CompoundIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

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
    public void test_SingleString() {
        Result actual = Eel.compile(context, "$( 'Hello World' ; $[1] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("Hello World", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultLookback_unrequired() {
        Result actual = Eel.compile(context, "$( '0' ; $[1-???] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("0", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultLookback_required() {
        Result actual = Eel.compile(context, "$( 0 ; $[2-???] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("???", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultLookback_NoChain() {
        Result actual = Eel.compile(context, "$( $[1-???] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("???", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultLookback_used() {
        Result actual = Eel.compile(context, "$( 0 ; $[2-???] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("???", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString_fromSymbolsTable() {
        Result actual = Eel.compile(context, "$( ${unknown-default value} ; $[1] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("default value", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleNumber() {
        Result actual = Eel.compile(context, "$( 123 ; $[1] )")
            .evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(new BigDecimal("123"), actual.asNumber(), "Unexpected number");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_MultipleLookBacks() {
        Result actual = Eel.compile(context, "$( 'Hello' ; 'world' ; 'unused' ; $[1] ~> ' ' ~> $[2] )")
            .evaluate();

        assertEquals(Type.TEXT, actual.getType(), "Unexpected Type");
        assertEquals("Hello world", actual.asText(), "Unexpected Text");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_reuseLookBacks() {
        Result actual = Eel.compile(context, "$( 123 ; $[1] + $[1] )")
            .evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(246, actual.asInt(), "Unexpected Number");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_nestedLookBacks() {
        Result actual = Eel.compile(context, "$( 10 ; $[1] + 1 ; $[2] + 2 )")
            .evaluate();

        assertEquals(Type.NUMBER, actual.getType(), "Unexpected Type");
        assertEquals(13, actual.asInt(), "Unexpected Number");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_trailingSemicolon() {
        Exception actual = assertThrows(EelSyntaxException.class,
            () -> Eel.compile(context, "$( 'Hello' ;  )").evaluate());

        assertEquals("Error at position 15: ')' was unexpected", actual.getMessage(), "Unexpected message");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_skipUnusedLookBacks() {
        Result actual = Eel.compile(context, "$( true ; fail('unexpected') ; $[1] )")
            .evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected Type");
        assertTrue(actual.asLogic(), "Unexpected Value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_nonIntegralIndex() {
        Result actual = Eel.compile(context, "$( true; $[1.1] )")
            .evaluate();

        assertEquals(Type.LOGIC, actual.getType(), "Unexpected Type");
        assertTrue(actual.asLogic(), "Unexpected Value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_invalidLookBack_0() {
        Exception actual = assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( true ; $[0] )").evaluate());

        assertEquals("Error at position 13: Undefined lookback $[0]", actual.getMessage(), "Unexpected message");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_invalidLookBack_1() {
        Exception actual = assertThrows(EelSemanticException.class,
            () -> Eel.compile(context, "$( $[1] )").evaluate());

        assertEquals("Error at position 6: Undefined lookback $[1]", actual.getMessage(), "Unexpected message");
    }
}
