package com.github.tymefly.eel.evaluate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import test.functions1.Plus1;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

public class EvaluateIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_EmptyExpression() {
        State actual = Evaluate.execute(new String[] { "" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "", stdOut.getLinesNormalized());
    }


    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_script() {
        String testScript = this.getClass()
            .getClassLoader()
            .getResource("IntegrationTest.eel")
            .getFile();

        State actual = Evaluate.execute(new String[] { "-v", "--script", testScript });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "[NUMBER] 4\n", stdOut.getLinesNormalized());
    }


    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_hardcodedString() {
        State actual = Evaluate.execute(new String[] { "/path/to/my.file" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "/path/to/my.file\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_simpleMath() {
        State actual = Evaluate.execute(new String[] { "$( 6 * 7 )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "42\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_simpleMath_verbose() {
        State actual = Evaluate.execute(new String[] { "--verbose", "$( 6 * 7 )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "[NUMBER] 42\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_functionCall() {
        State actual = Evaluate.execute(new String[] { "$( max( 1, 2, 3) )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "3\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_mixed() {
        State actual = Evaluate.execute(new String[] { ">>> $( max( 1, 2, 3) ) <<<" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", ">>> 3 <<<\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_mixed_verbose() {
        State actual = Evaluate.execute(new String[] { "-v", ">>> $( max( 1, 2, 3) ) <<<" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "[TEXT] >>> 3 <<<\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_properties() {
        State actual = Evaluate.execute(new String[] { "--props", "${java.version}" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", System.getProperty("java.version") + "\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_properties_undefined() {
        State actual = Evaluate.execute(new String[] { "${java.version-???}" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "???\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_definitions() {
        State actual = Evaluate.execute(new String[] { "-D", "key=value", "${key-???}" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "value\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_definitions_undefined() {
        State actual = Evaluate.execute(new String[] { "-D", "key2=value", "${key-???}" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "???\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_precision_default() {
        State actual = Evaluate.execute(new String[] { "$( 1 /3 )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "0.3333333333333333\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_precision_expanded() {
        State actual = Evaluate.execute(new String[] { "--precision", "15", "$( 1 /3 )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "0.333333333333333\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_precision_reduced() {
        State actual = Evaluate.execute(new String[] { "--precision", "5", "$( 1 /3 )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "0.33333\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_externalFunction() {
        State actual = Evaluate.execute(new String[] { "--udf-class", Plus1.class.getName(), "$( test.plus1( 10 ) )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "11\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_externalPackage() {
        State actual = Evaluate.execute(new String[] { "--udf-package", "test.functions2", "$( test.plus2( 10 ) )" });

        Assert.assertEquals("Unexpected State", State.EVALUATED, actual);
        Assert.assertEquals("Unexpected result", "12\n", stdOut.getLinesNormalized());
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_invalidExpression() {
        State actual = Evaluate.execute(new String[] { "$(" });

        Assert.assertEquals("Unexpected State", State.EXPRESSION_FAILED, actual);
        Assert.assertTrue("Unexpected result", stdErr.getLinesNormalized().startsWith("Failed to evaluate : $("));
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_cliError() {
        State actual = Evaluate.execute(new String[] { "-unknown", "Hello" });

        Assert.assertEquals("Unexpected State", State.BAD_COMMAND_LINE, actual);
        Assert.assertTrue("Unexpected result", stdErr.getLinesNormalized().startsWith("Error: \"-unknown\" is not a valid option"));
    }

    /**
     * Functional test {@link Evaluate}
     */
    @Test
    public void test_Help() {
        State actual = Evaluate.execute(new String[] { "--help" });

        Assert.assertEquals("Unexpected State", State.HELP, actual);
        Assert.assertTrue("Unexpected result", stdOut.getLinesNormalized().startsWith("Usage:"));
    }
}
