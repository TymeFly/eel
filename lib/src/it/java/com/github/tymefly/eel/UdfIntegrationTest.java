package com.github.tymefly.eel;

import java.time.Duration;

import com.github.tymefly.eel.exception.EelUnknownFunctionException;
import func.functions.Plus1;
import func.functions.Sum;
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
 * Integration Tests for adding User Defined Functions
 */
@ExtendWith(SystemStubsExtension.class)
public class UdfIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NotExtended() {
        EelUnknownFunctionException actual = assertThrows(EelUnknownFunctionException.class,
            () -> Eel.factory()
                .withTimeout(Duration.ofSeconds(0))
                .compile("Result is $( Plus1( 5 ) )"));

        assertEquals("Undefined function 'Plus1'", actual.getMessage(), "Unexpected Error");
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

        assertEquals("Result is 6", actual, "Unexpected value");
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

        assertEquals("Result is 6", actual, "Unexpected value");
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

        assertEquals("Result is 3", actual, "Unexpected value");
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

        assertEquals("Result is 12", actual, "Unexpected value");
    }
}
