package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelRuntimeException;
import func.functions.Plus1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Tests on the EEL Context
 */
@ExtendWith(SystemStubsExtension.class)
public class ContextIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseContext() {
        EelContext context = EelContext.factory()
            .withUdfPackage(Plus1.class.getPackage())
            .withTimeout(Duration.ofSeconds(0))
            .build();

        assertEquals(12,
            Eel.compile(context, "$( test.plus1(11))").evaluate().asNumber().intValue(),
            "Exp1");

        assertEquals(37,
            Eel.compile(context, "$( test.plus1(test.sum(11, 12, 13)) )").evaluate().asInt(),
            "Exp2");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_maxExpressionSize() {
        EelSourceException actual = assertThrows(EelSourceException.class,
            () -> Eel.factory()
                .withMaxExpressionSize(10)
                .withTimeout(Duration.ofSeconds(0))
                .compile("1234567890a"));

        assertEquals("Attempt to read beyond maximum expression length of 10 bytes",
            actual.getMessage(),
            "Unexpected error");
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

        assertEquals("Result is 0.3333333333333333", actual, "Unexpected value");
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

        assertEquals("Result is 0.3", actual, "Unexpected value");
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

        assertEquals("Result is 0.333333", actual, "Unexpected value");
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

        assertEquals((first + 1), second, "Default Context was not reused");
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

        assertEquals(first, second, "Default Context was not reused");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FileFactory_Allowed() {
        Eel expression = Eel.factory()
            .withFileFactory(File::new)
            .compile("$io.head('pom.xml', 1)");

        assertDoesNotThrow(() -> expression.evaluate());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FileFactory_NormalRules() {
        boolean isWindows = '\\' == File.separatorChar;
        String blackDir = (isWindows ? System.getenv("ProgramFiles") : "/bin");
        String fileName = (blackDir + "/file.txt").replace('\\', '/');
        Eel expression = Eel.factory()
            .withFileFactory(File::new)                             // Don't replace the inbuilt restrictions
            .compile("$io.head('" + fileName + "', 1)");

        Exception actual = assertThrows(EelRuntimeException.class, expression::evaluate);

        assertEquals(actual.getMessage(), "File Factory for '" + fileName + "' failed", "Unexpected message");

        Throwable cause = actual.getCause();

        assertInstanceOf(IOException.class, cause, "Unexpected cause type");
        assertTrue(cause.getMessage().matches("Path '[^']+' is in a sensitive part of the local file system"),
            "Unexpected cause message");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FileFactory_BlockAll() {
        Eel expression = Eel.factory()
            .withFileFactory(f -> {
                throw new IOException("Don't read file: " + f);
            })
            .compile("$io.head('pom.xml', 1)");

        Exception actual = assertThrows(EelRuntimeException.class, expression::evaluate);

        assertEquals(actual.getMessage(), "File Factory for 'pom.xml' failed", "Unexpected message");

        Throwable cause = actual.getCause();

        assertInstanceOf(IOException.class, cause, "Unexpected cause type");
        assertEquals("Don't read file: pom.xml", cause.getMessage(), "Unexpected cause message");
    }
}
