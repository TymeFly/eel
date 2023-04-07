package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelIOException;
import func.functions.Plus1;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Integration Tests on the EEL Context
 */
public class ContextIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseContext() {
        EelContext context = EelContext.factory()
            .withUdfPackage(Plus1.class.getPackage())
            .build();

        Assert.assertEquals("Exp1",
            12,
            Eel.compile(context, "$( test.plus1(11))").evaluate().asNumber().intValue());

        Assert.assertEquals("Exp2",
            37,
            Eel.compile(context, "$( test.plus1(test.sum(11, 12, 13)) )").evaluate().asNumber().intValue());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maxExpressionSize() {
        EelIOException actual = Assert.assertThrows(EelIOException.class,
            () -> Eel.factory()
                .withMaxExpressionSize(10)
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
        Eel expression = Eel.compile("$( count() )");
        int first = expression.evaluate()
            .asNumber()
            .intValue();
        int second = expression.evaluate()
            .asNumber()
            .intValue();

        Assert.assertEquals("Default Context was not reused", (first + 1), second);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultContextReuse_timeStamp() throws Exception {
        Eel expression = Eel.compile("$( date.start() )");
        ZonedDateTime first = expression.evaluate()
            .asDate();

        Thread.sleep(1_500);

        ZonedDateTime second = expression.evaluate()
            .asDate();

        Assert.assertEquals("Default Context was not reused", first, second);
    }
}
