package com.github.tymefly.eel;

import java.util.Map;

import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;


/**
 * Integration Test for variable expansions
 */
public class VariableExpansionIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withDefault() {
        String actual = Eel.compile("${hello-???}")
            .evaluate("myDefaultValue")
            .asText();

        Assert.assertEquals("Failed to read Map", "myDefaultValue", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_notFound() {
        Eel expression = Eel.compile("${fred}");

        EelUnknownSymbolException actual = Assert.assertThrows(EelUnknownSymbolException.class, expression::evaluate);

        Assert.assertEquals("Unexpected message", "Unknown variable 'fred'", actual.getMessage());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_use_default() {
        Result actual = Eel.compile("${key-default value}")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "default value", actual.asText());
    }

    /**
     * Integration test {@link Eel} unrequired
     */
    @Test
    public void test_default_unrequired() {
        Result actual = Eel.compile("${key-default value}")
            .evaluate(SymbolsTable.from(Map.of("key", "Value")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "Value", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_use_default_withEmbeddedExpression() {
        Result actual = Eel.compile("${key-~$( 1 + NUMBER(${two}) )~${postfix}~}!")
            .evaluate(SymbolsTable.from(Map.of("two", "2", "postfix", "@@")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "~3~@@~!", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstUpper() {
        Result actual = Eel.compile("${var^}")
            .evaluate(SymbolsTable.from(Map.of("var", "lower")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "Lower", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllUpper() {
        Result actual = Eel.compile("${var^^}")
            .evaluate(SymbolsTable.from(Map.of("var", "lower")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "LOWER", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstLower() {
        Result actual = Eel.compile("${var,}")
            .evaluate(SymbolsTable.from(Map.of("var", "UPPER")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "uPPER", actual.asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllLower() {
        Result actual = Eel.compile("${var,,}")
            .evaluate(SymbolsTable.from(Map.of("var", "UPPER")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "upper", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_FirstToggle() {
        Result actual = Eel.compile("${var1~} ${var2~}")
            .evaluate(SymbolsTable.from(Map.of("var1", "UPPER", "var2", "lower")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "uPPER Lower", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_AllToggle() {
        Result actual = Eel.compile("${var1~~} ${var2~~}")
            .evaluate(SymbolsTable.from(Map.of("var1", "UPPER", "var2", "lower")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "upper LOWER", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_caseChange_and_default() {
        Result actual = Eel.compile("${key^^-default value}")
            .evaluate(SymbolsTable.from(Map.of("key", "Value")));

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "VALUE", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_caseChange_and_default2() {
        Result actual = Eel.compile("${key^^-default value}")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "default value", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_length() {
        Result actual = Eel.compile("${#key}")
            .evaluate(SymbolsTable.from(Map.of("key", "123456")));

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value", "6", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_defaultAndLength() {
        Result actual = Eel.compile("${#key-12}")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "12", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_changeCaseAndLength() {
        Result actual = Eel.compile("${#key^^}")             // change of case should be ignored
            .evaluate(SymbolsTable.from(Map.of("key", "123456")));

        Assert.assertEquals("Unexpected type", Type.NUMBER, actual.getType());
        Assert.assertEquals("Unexpected value", "6", actual.asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined() {
        Result actual = Eel.compile("$( key? )")
            .evaluate(Map.of("key", "value"));

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertTrue("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isUndefined() {
        Result actual = Eel.compile("$( key? )")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.LOGIC, actual.getType());
        Assert.assertFalse("Unexpected value", actual.asLogic());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_isDefined_custom() {
        Result actual = Eel.compile("$( key? ? 'found' : 'notFound' )")
            .evaluate();

        Assert.assertEquals("Unexpected type", Type.TEXT, actual.getType());
        Assert.assertEquals("Unexpected value", "notFound", actual.asText());
    }
    
}
