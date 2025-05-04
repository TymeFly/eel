package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Basic set of integration Tests
 */
public class BasicIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    private EelContext context;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(0))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString() {
        String actual = Eel.compile(context, "Hello World")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "Hello World", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_String_Paths() {
        Assert.assertEquals("*nix path",
            "/path/to/my/file.txt",
             Eel.compile(context, "/path/to/my/file.txt")
                .evaluate()
                .asText());

        Assert.assertEquals("DOS path",
            "c:\\path\\to\\my\\file.txt",
            Eel.compile(context, "c:\\\\path\\\\to\\\\my\\\\file.txt")
                .evaluate()
                .asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_String_EscapedChars() {
        Assert.assertEquals("Escaped $",
            "$My $Text",
            Eel.compile(context, "\\$My \\$Text")
                .evaluate()
                .asText());

        Assert.assertEquals("Tab in expression",
            "Hello\tWorld",
            Eel.compile(context, "Hello\\tWorld")
                .evaluate()
                .asText());

        Assert.assertEquals("Tab in string",
            "Hello\tWorld",
            Eel.compile(context, "$( 'Hello\\tWorld' )")
                .evaluate()
                .asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_InterpolationInText() {
        Map<String, String> symbols = Map.ofEntries(Map.entry("a", "first"), Map.entry("b", "second"));

        Assert.assertEquals("Symbols",
            "Value: first, second",
            Eel.compile(context, "$( 'Value: ${a}, ${b}' )")
                .evaluate(symbols)
                .asText());

        Assert.assertEquals("Function",
            "Value: 9",
            Eel.compile(context, "$( 'Value: $random(9, 9)' )")
                .evaluate(symbols)
                .asText());

        Assert.assertEquals("Expression",
            "Value: 18",
            Eel.compile(context, "$( 'Value: $(9 + 9)' )")
                .evaluate(symbols)
                .asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NumericValues() {
        Assert.assertEquals("+ve Decimal Integer", BigDecimal.valueOf(1234), Eel.compile("$(1234)").evaluate().asNumber());
        Assert.assertEquals("-ve Decimal Integer", BigDecimal.valueOf(-123), Eel.compile("$(-123)").evaluate().asNumber());
        Assert.assertEquals("+ve Fractional Decimal", BigDecimal.valueOf(123.456), Eel.compile("$(123.456)").evaluate().asNumber());
        Assert.assertEquals("-ve Fractional Decimal", BigDecimal.valueOf(-123.456), Eel.compile("$(-123.456)").evaluate().asNumber());
        Assert.assertEquals("Large Scientific Format", new BigDecimal("2.99792e8"), Eel.compile("$(2.99792e8)").evaluate().asNumber());
        Assert.assertEquals("Small Scientific Format", new BigDecimal("9.109383e-31"), Eel.compile("$(9.109383e-31)").evaluate().asNumber());

        Assert.assertEquals("Binary integers", BigDecimal.TEN, Eel.compile("$(0b1010)").evaluate().asNumber());
        Assert.assertEquals("Octal integers", BigDecimal.valueOf(342391), Eel.compile("$(0c1234567)").evaluate().asNumber());
        Assert.assertEquals("Hex integers", BigDecimal.valueOf(35243), Eel.compile("$(0x89ab)").evaluate().asNumber());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ConstantsValue() {
        testConstant("$( true )", Type.LOGIC, "true");
        testConstant("$( false )", Type.LOGIC, "false");
    }

    private void testConstant(@Nonnull String expression, @Nonnull Type expectedType, @Nonnull String expectedValue) {
        Result result = Eel.compile(expression)
            .evaluate();

        Assert.assertEquals("'" + expression + "' has unexpected type", expectedType, result.getType());
        Assert.assertEquals("'" + expression + "' has unexpected value", expectedValue, result.asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_StringAndValue() {
        String actual = Eel.compile(context, "Hello $(2 ** 8) World")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected StringAndValue", "Hello 256 World", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_EmbeddedFunction() {
        String actual = Eel.compile(context, "~~~ $( max(-3, 1, 23, 101) ) ~~~")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected value", "~~~ 101 ~~~", actual);
    }

     /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_EmbeddedFunctionInterpolation() {
        boolean actual = Eel.compile(context, "$( $date.plus(${unknown-2000-01-02T12:00}, '5m') isAfter 0 )")
            .evaluate()
            .asLogic();

        Assert.assertTrue("Unexpected value", actual);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Dollar() {
        Assert.assertEquals("Function Interpolation",
            "~~~ 0 ~~~ 1 ~~~",
            Eel.compile(context, "~~~ $count() ~~~ $count() ~~~").evaluate().asText());
        Assert.assertEquals("Digits",
            "$99",
            Eel.compile(context, "$99").evaluate().asText());
        Assert.assertEquals("Spaces",
            "$  random(99,99)",
            Eel.compile(context, "$  random(99,99)").evaluate().asText());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseCompiledExpression() {
        Eel compiled = Eel.factory()                            // Create a new Context for count
            .compile("Result is $( count() )");

        Assert.assertEquals("First", "Result is 0", compiled.evaluate().asText());
        Assert.assertEquals("Second", "Result is 1", compiled.evaluate().asText());
        Assert.assertEquals("Third", "Result is 2", compiled.evaluate().asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_lazyEvaluation() {
        Result result = Eel.compile(context, "$indexOf('abcd', 'c', log.debug('test', fail('Never evaluated') ) )")
            .evaluate();

        // The real test is that fail() is returned from log.debug(), but never evaluated

        Assert.assertEquals("Unexpected Index", 2, result.asInt());
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleEvaluation() {
        Eel compiled = Eel.compile(context, "$count()");
        Result result1 = compiled.evaluate();
        Result result2 = compiled.evaluate();

        int expected1 = result1.asInt();

        Assert.assertEquals("expression re-read",  expected1, result1.asInt());             // should not change
        Assert.assertEquals("expression re-evaluated",  (expected1 + 1), result2.asInt());  // should have changed
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleEvaluation_LookBack() {
        Eel compiled = Eel.compile(context, "$count()");

        // Bump counter to 3
        compiled.evaluate();
        compiled.evaluate();
        compiled.evaluate();

        Result actual = Eel.compile(context, "$( 2 ; count() ; $[1] + $[2] + $[2] )")    // $[2] should be consistently 3
            .evaluate();

        Assert.assertEquals("Unexpected result", 8, actual.asInt());
    }
}
