package com.github.tymefly.eel;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelFunctionException;
import func.functions2.Half;
import func.functions2.Times2;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Integration Test to demonstrate UDFs don't leak across contexts
 */
public class UdfIsolationIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Integration test for {@link com.github.tymefly.eel.builder.EelBuilder#withUdfPackage(Package)}
     */
    @Test
    public void test_class() {
        EelContext withUdf = EelContext.factory()
            .withUdfClass(Half.class)
            .build();
        EelContext withoutUdf = EelContext.factory()
            .build();

        helper(withUdf, withoutUdf);
    }


    /**
     * Integration test for {@link com.github.tymefly.eel.builder.EelBuilder#withUdfPackage(Package)}
     */
    @Test
    public void test_package() {
        EelContext withUdf = EelContext.factory()
            .withUdfPackage(Times2.class.getPackage())     // Class in same package as "half"
            .build();
        EelContext withoutUdf = EelContext.factory()
            .build();

        helper(withUdf, withoutUdf);
    }


    private void helper(@Nonnull EelContext withUdf, @Nonnull EelContext withoutUdf) {
        Assert.assertEquals("Unexpected result for 30",
            "15",
            Eel.compile(withUdf, "$( test.half(30) )").evaluate().asText());

        EelFunctionException failure = Assert.assertThrows(EelFunctionException.class,
            () -> Eel.compile(withoutUdf, "$( test.half(40) )").evaluate().asText());
        Assert.assertEquals("Unexpected exception",
            "Undefined function 'test.half'",
            failure.getMessage());

        Assert.assertEquals("Unexpected result for 100",
            "50",
            Eel.compile(withUdf, "$( test.half(100) )").evaluate().asText());
    }
}
