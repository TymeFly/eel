package com.github.tymefly.eel;

import java.time.Duration;

import com.github.tymefly.eel.function.general.Count;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Integration Test for the 'counter' function
 */
@ExtendWith(SystemStubsExtension.class)
public class CounterIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    /**
     * Integration test for {@link Count}
     */
    @Test
    public void test_singleContext_OneCount() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count() ~> '_' )");

        assertEquals("_0_", expression.evaluate().asText(), "Iteration 1");
        assertEquals("_1_", expression.evaluate().asText(), "Iteration 2");
        assertEquals("_2_", expression.evaluate().asText(), "Iteration 3");
        assertEquals("_3_", expression.evaluate().asText(), "Iteration 4");
    }

    /**
     * Integration test for {@link Count}
     */
    @Test
    public void test_singleContext_MultipleCounts() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count() ~> '_' ~> count() )");

        assertEquals("_0_1", expression.evaluate().asText(), "Iteration 1");
        assertEquals("_2_3", expression.evaluate().asText(), "Iteration 2");
        assertEquals("_4_5", expression.evaluate().asText(), "Iteration 3");
        assertEquals("_6_7", expression.evaluate().asText(), "Iteration 4");
    }

    /**
     * Integration test for {@link Count}
     */
    @Test
    public void test_singleContext_NamedCounts() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count('1') ~> '_' ~> count('2') )");

        assertEquals("_0_0", expression.evaluate().asText(), "Iteration 1");
        assertEquals("_1_1", expression.evaluate().asText(), "Iteration 2");
        assertEquals("_2_2", expression.evaluate().asText(), "Iteration 3");
        assertEquals("_3_3", expression.evaluate().asText(), "Iteration 4");
    }

    /**
     * Integration test for {@link Count}
     */
    @Test
    public void test_MultipleContext_MultipleCounts() {
        EelContext context1 = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        EelContext context2 = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression1 = Eel.compile(context1, "$( '_' ~> count() ~> '_' ~> count() )");
        Eel expression2 = Eel.compile(context2, "$( '_' ~> count() ~> '_' ~> count() )");

        assertEquals("_0_1", expression1.evaluate().asText(), "Iteration 1");
        assertEquals("_2_3", expression1.evaluate().asText(), "Iteration 2");
        assertEquals("_4_5", expression1.evaluate().asText(), "Iteration 3");
        assertEquals("_6_7", expression1.evaluate().asText(), "Iteration 4");

        assertEquals("_0_1", expression2.evaluate().asText(), "Iteration 5");
        assertEquals("_2_3", expression2.evaluate().asText(), "Iteration 6");
        assertEquals("_4_5", expression2.evaluate().asText(), "Iteration 7");

        assertEquals("_8_9", expression1.evaluate().asText(), "Iteration 8");
    }

    /**
     * Integration test for {@link Count}
     */
    @Test
    public void test_MultipleImplicitContext_MultipleCounts() {
        Eel expression1 = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( '_' ~> count() ~> '_' ~> count() )");
        Eel expression2 = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( '_' ~> count() ~> '_' ~> count() )");

        assertEquals("_0_1", expression1.evaluate().asText(), "Iteration 1");
        assertEquals("_2_3", expression1.evaluate().asText(), "Iteration 2");
        assertEquals("_4_5", expression1.evaluate().asText(), "Iteration 3");
        assertEquals("_6_7", expression1.evaluate().asText(), "Iteration 4");

        assertEquals("_0_1", expression2.evaluate().asText(), "Iteration 5");
        assertEquals("_2_3", expression2.evaluate().asText(), "Iteration 6");
        assertEquals("_4_5", expression2.evaluate().asText(), "Iteration 7");

        assertEquals("_8_9", expression1.evaluate().asText(), "Iteration 8");
    }
}
