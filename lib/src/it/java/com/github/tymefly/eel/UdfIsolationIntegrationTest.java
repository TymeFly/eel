package com.github.tymefly.eel;

import java.time.Duration;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelUnknownFunctionException;
import func.functions2.Half;
import func.functions2.Times2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration Test to demonstrate UDFs don't leak across contexts
 */
@ExtendWith(SystemStubsExtension.class)
public class UdfIsolationIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    /**
     * Integration test for {@link com.github.tymefly.eel.builder.EelBuilder#withUdfClass(Class)} 
     */
    @Test
    public void test_class() {
        EelContext withUdf = EelContext.factory()
            .withUdfClass(Half.class)
            .withTimeout(Duration.ofSeconds(0))
            .build();
        EelContext withoutUdf = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
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
            .withTimeout(Duration.ofSeconds(0))
            .build();
        EelContext withoutUdf = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();

        helper(withUdf, withoutUdf);
    }


    private void helper(@Nonnull EelContext withUdf, @Nonnull EelContext withoutUdf) {
        assertEquals("15",
            Eel.compile(withUdf, "$( test.half(30) )").evaluate().asText(),
            "Unexpected result for 30");

        EelUnknownFunctionException failure = assertThrows(EelUnknownFunctionException.class,
            () -> Eel.compile(withoutUdf, "$( test.half(40) )").evaluate().asText());
        assertEquals("Undefined function 'test.half'",
            failure.getMessage(),
            "Unexpected exception");

        assertEquals("50",
            Eel.compile(withUdf, "$( test.half(100) )").evaluate().asText(),
            "Unexpected result for 100");
    }
}
