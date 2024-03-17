package com.github.tymefly.eel;

import java.time.Duration;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Integration Test for the counter function
 */
public class CounterIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Integration test for {@link com.github.tymefly.eel.function.util.Count}
     */
    @Test
    public void test_singleContext_OneCount() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count() ~> '_' )");

        Assert.assertEquals("Iteration 1", "_0_", expression.evaluate().asText());
        Assert.assertEquals("Iteration 2", "_1_", expression.evaluate().asText());
        Assert.assertEquals("Iteration 3", "_2_", expression.evaluate().asText());
        Assert.assertEquals("Iteration 4", "_3_", expression.evaluate().asText());
    }

    /**
     * Integration test for {@link com.github.tymefly.eel.function.util.Count}
     */
    @Test
    public void test_singleContext_MultipleCounts() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count() ~> '_' ~> count() )");

        Assert.assertEquals("Iteration 1", "_0_1", expression.evaluate().asText());
        Assert.assertEquals("Iteration 2", "_2_3", expression.evaluate().asText());
        Assert.assertEquals("Iteration 3", "_4_5", expression.evaluate().asText());
        Assert.assertEquals("Iteration 4", "_6_7", expression.evaluate().asText());
    }

    /**
     * Integration test for {@link com.github.tymefly.eel.function.util.Count}
     */
    @Test
    public void test_singleContext_NamedCounts() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
        Eel expression = Eel.compile(context, "$( '_' ~> count('1') ~> '_' ~> count('2') )");

        Assert.assertEquals("Iteration 1", "_0_0", expression.evaluate().asText());
        Assert.assertEquals("Iteration 2", "_1_1", expression.evaluate().asText());
        Assert.assertEquals("Iteration 3", "_2_2", expression.evaluate().asText());
        Assert.assertEquals("Iteration 4", "_3_3", expression.evaluate().asText());
    }

    /**
     * Integration test for {@link com.github.tymefly.eel.function.util.Count}
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

        Assert.assertEquals("Iteration 1", "_0_1", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 2", "_2_3", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 3", "_4_5", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 4", "_6_7", expression1.evaluate().asText());

        Assert.assertEquals("Iteration 5", "_0_1", expression2.evaluate().asText());
        Assert.assertEquals("Iteration 6", "_2_3", expression2.evaluate().asText());
        Assert.assertEquals("Iteration 7", "_4_5", expression2.evaluate().asText());

        Assert.assertEquals("Iteration 8", "_8_9", expression1.evaluate().asText());
    }

    /**
     * Integration test for {@link com.github.tymefly.eel.function.util.Count}
     */
    @Test
    public void test_MultipleImplicitContext_MultipleCounts() {
        Eel expression1 = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( '_' ~> count() ~> '_' ~> count() )");
        Eel expression2 = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( '_' ~> count() ~> '_' ~> count() )");

        Assert.assertEquals("Iteration 1", "_0_1", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 2", "_2_3", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 3", "_4_5", expression1.evaluate().asText());
        Assert.assertEquals("Iteration 4", "_6_7", expression1.evaluate().asText());

        Assert.assertEquals("Iteration 5", "_0_1", expression2.evaluate().asText());
        Assert.assertEquals("Iteration 6", "_2_3", expression2.evaluate().asText());
        Assert.assertEquals("Iteration 7", "_4_5", expression2.evaluate().asText());

        Assert.assertEquals("Iteration 8", "_8_9", expression1.evaluate().asText());
    }
}
