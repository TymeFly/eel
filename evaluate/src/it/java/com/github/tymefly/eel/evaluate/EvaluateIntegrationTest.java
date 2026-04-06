package com.github.tymefly.eel.evaluate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.functions1.Plus1;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Evaluate}
 */
@ExtendWith(SystemStubsExtension.class)
class EvaluateIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_EmptyExpression() {
        State actual = Evaluate.execute(new String[] { "" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_script() {
        String testScript = this.getClass()
            .getClassLoader()
            .getResource("IntegrationTest.eel")
            .getFile();

        State actual = Evaluate.execute(new String[] { "-v", "--script", testScript });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("[Number] 4\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_hardcodedString() {
        State actual = Evaluate.execute(new String[] { "/path/to/my.file" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("/path/to/my.file\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_simpleMath() {
        State actual = Evaluate.execute(new String[] { "$( 6 * 7 )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("42\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_simpleMath_verbose() {
        State actual = Evaluate.execute(new String[] { "--verbose", "$( 6 * 7 )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("[Number] 42\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_functionCall() {
        State actual = Evaluate.execute(new String[] { "$( max( 1, 2, 3) )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("3\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_mixed() {
        State actual = Evaluate.execute(new String[] { ">>> $( max( 1, 2, 3) ) <<<" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals(">>> 3 <<<\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_mixed_verbose() {
        State actual = Evaluate.execute(new String[] { "-v", ">>> $( max( 1, 2, 3) ) <<<" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("[Text] >>> 3 <<<\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_properties() {
        State actual = Evaluate.execute(new String[] { "--props", "${java.version}" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals(System.getProperty("java.version") + "\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_properties_undefined() {
        State actual = Evaluate.execute(new String[] { "${java.version-???}" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("???\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_definitions() {
        State actual = Evaluate.execute(new String[] { "-D", "key=value", "${key-???}" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("value\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_definitions_undefined() {
        State actual = Evaluate.execute(new String[] { "-D", "key2=value", "${key-???}" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("???\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_precision_default() {
        State actual = Evaluate.execute(new String[] { "$( 1 /3 )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("0.3333333333333333\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_precision_expanded() {
        State actual = Evaluate.execute(new String[] { "--precision", "15", "$( 1 /3 )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("0.333333333333333\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_precision_reduced() {
        State actual = Evaluate.execute(new String[] { "--precision", "5", "$( 1 /3 )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("0.33333\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_externalFunction() {
        State actual = Evaluate.execute(new String[] { "--udf-class", Plus1.class.getName(), "$( test.plus1( 10 ) )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("11\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_externalPackage() {
        State actual = Evaluate.execute(new String[] { "--udf-package", "test.functions2", "$( test.plus2( 10 ) )" });

        assertEquals(State.EVALUATED, actual, "Unexpected State");
        assertEquals("12\n", stdOut.getLinesNormalized(), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_invalidExpression() {
        State actual = Evaluate.execute(new String[] { "$(" });

        assertEquals(State.EXPRESSION_FAILED, actual, "Unexpected State");
        assertTrue(stdErr.getLinesNormalized().startsWith("Failed to evaluate : $("), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_cliError() {
        State actual = Evaluate.execute(new String[] { "-unknown", "Hello" });

        assertEquals(State.BAD_COMMAND_LINE, actual, "Unexpected State");
        assertTrue(stdErr.getLinesNormalized().startsWith("Error: \"-unknown\" is not a valid option"), "Unexpected result");
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    void test_Help() {
        State actual = Evaluate.execute(new String[] { "--help" });

        assertEquals(State.HELP, actual, "Unexpected State");
        assertTrue(stdOut.getLinesNormalized().startsWith("Usage:"), "Unexpected result");
    }
}
