package com.github.tymefly.eel;

import java.time.Duration;
import java.time.ZonedDateTime;

import func.functions.Plus1;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Integration Tests on the EEL Context
 */
public class ContextIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseContext() {
        EelContext context = EelContext.factory()
            .withUdfPackage(Plus1.class.getPackage())
            .withTimeout(Duration.ofSeconds(0))
            .build();

        Assert.assertEquals("Exp1",
            12,
            Eel.compile(context, "$( test.plus1(11))").evaluate().asNumber().intValue());

        Assert.assertEquals("Exp2",
            37,
            Eel.compile(context, "$( test.plus1(test.sum(11, 12, 13)) )").evaluate().asInt());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maxExpressionSize() {
        EelSourceException actual = Assert.assertThrows(EelSourceException.class,
            () -> Eel.factory()
                .withMaxExpressionSize(10)
                .withTimeout(Duration.ofSeconds(0))
                .compile("1234567890a"));

        Assert.assertEquals("Unexpected error",
            "Attempt to read beyond maximum expression length of 10 bytes",
            actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_setScale_default() {
        String actual = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( 1 / 3 )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 0.3333333333333333", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withPrecision_reduced() {
        String actual = Eel.factory()
            .withPrecision(1)
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( 1 / 3 )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 0.3", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withPrecision_expanded() {
        String actual = Eel.factory()
            .withPrecision(6)
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( 1 / 3 )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 0.333333", actual);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultContextReuse_count() {
        Eel expression = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( count() )");
        int first = expression.evaluate().asInt();
        int second = expression.evaluate().asInt();

        Assert.assertEquals("Default Context was not reused", (first + 1), second);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultContextReuse_timeStamp() throws Exception {
        Eel expression = Eel.factory()
            .withTimeout(Duration.ofSeconds(0))
            .compile("$( date.start() )");
        ZonedDateTime first = expression.evaluate()
            .asDate();

        Thread.sleep(1_500);

        ZonedDateTime second = expression.evaluate()
            .asDate();

        Assert.assertEquals("Default Context was not reused", first, second);
    }
}
