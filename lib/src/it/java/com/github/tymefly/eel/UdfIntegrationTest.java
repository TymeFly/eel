package com.github.tymefly.eel;

import java.time.Duration;

import com.github.tymefly.eel.exception.EelUnknownFunctionException;
import func.functions.Plus1;
import func.functions.Sum;
import func.functions2.Half;
import func.functions2.Times2;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Integration Tests for adding User Defined Functions
 */
public class UdfIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NotExtended() {
        EelUnknownFunctionException actual = Assert.assertThrows(EelUnknownFunctionException.class,
            () -> Eel.factory()
                .withTimeout(Duration.ofSeconds(0))
                .compile("Result is $( Plus1( 5 ) )"));

        Assert.assertEquals("Unexpected Error", "Undefined function 'Plus1'", actual.getMessage());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Extend_Function() {
        EelContext context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .withUdfClass(Plus1.class)
            .build();

        String actual = Eel.factory()
            .withContext(context)
            .compile("Result is $( test.plus1( 5 ) )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 6", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Extend_Package() {
        String actual = Eel.factory()
            .withUdfPackage(Sum.class.getPackage())
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( test.plus1( 5 ) )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 6", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Extend_TwoPackages() {
        String actual = Eel.factory()
            .withUdfPackage(Sum.class.getPackage())
            .withUdfPackage(Half.class.getPackage())
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( test.half( test.plus1( 5 )) )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 3", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NamedFunction() {
        String actual = Eel.factory()
            .withUdfPackage(Sum.class.getPackage())
            .withUdfClass(Times2.class)
            .withTimeout(Duration.ofSeconds(0))
            .compile("Result is $( test.double( test.plus1( 5 )) )")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "Result is 12", actual);
    }
}
