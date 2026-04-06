package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic set of integration Tests
 */
@ExtendWith(SystemStubsExtension.class)
public class BasicIntegrationTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    private EelContext context;


    @BeforeEach
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

        assertEquals("Hello World", actual, "Unexpected String");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_String_Paths() {
        assertEquals("/path/to/my/file.txt",
            Eel.compile(context, "/path/to/my/file.txt")
                .evaluate()
                .asText(),
            "*nix path");

        assertEquals("c:\\path\\to\\my\\file.txt",
            Eel.compile(context, "c:\\\\path\\\\to\\\\my\\\\file.txt")
                .evaluate()
                .asText(),
            "DOS path");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_String_EscapedChars() {
        assertEquals("$My $Text",
            Eel.compile(context, "\\$My \\$Text")
                .evaluate()
                .asText(),
            "Escaped $");

        assertEquals("Hello\tWorld",
            Eel.compile(context, "Hello\\tWorld")
                .evaluate()
                .asText(),
            "Tab in expression");

        assertEquals("Hello\tWorld",
            Eel.compile(context, "$( 'Hello\\tWorld' )")
                .evaluate()
                .asText(),
            "Tab in string");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_InterpolationInText() {
        Map<String, String> symbols = Map.ofEntries(Map.entry("a", "first"), Map.entry("b", "second"));

        assertEquals("Value: first, second",
            Eel.compile(context, "$( 'Value: ${a}, ${b}' )")
                .evaluate(symbols)
                .asText(),
            "Symbols");

        assertEquals("Value: 9",
            Eel.compile(context, "$( 'Value: $random(9, 9)' )")
                .evaluate(symbols)
                .asText(),
            "Function");

        assertEquals("Value: 18",
            Eel.compile(context, "$( 'Value: $(9 + 9)' )")
                .evaluate(symbols)
                .asText(),
            "Expression");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NumericValues() {
        assertEquals(BigDecimal.valueOf(1234), Eel.compile("$(1234)").evaluate().asNumber(), "+ve Decimal Integer");
        assertEquals(BigDecimal.valueOf(-123), Eel.compile("$(-123)").evaluate().asNumber(), "-ve Decimal Integer");
        assertEquals(BigDecimal.valueOf(123.456), Eel.compile("$(123.456)").evaluate().asNumber(), "+ve Fractional Decimal");
        assertEquals(BigDecimal.valueOf(-123.456), Eel.compile("$(-123.456)").evaluate().asNumber(),"-ve Fractional Decimal");
        assertEquals(new BigDecimal("2.99792e8"), Eel.compile("$(2.99792e8)").evaluate().asNumber(), "Large Scientific Format");
        assertEquals(new BigDecimal("9.109383e-31"), Eel.compile("$(9.109383e-31)").evaluate().asNumber(), "Small Scientific Format");

        assertEquals(BigDecimal.TEN, Eel.compile("$(0b1010)").evaluate().asNumber(), "Binary integers");
        assertEquals(BigDecimal.valueOf(342391), Eel.compile("$(0c1234567)").evaluate().asNumber(), "Octal integers");
        assertEquals(BigDecimal.valueOf(35243), Eel.compile("$(0x89ab)").evaluate().asNumber(), "Hex integers");
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

        assertEquals(expectedType, result.getType(), "'" + expression + "' has unexpected type");
        assertEquals(expectedValue, result.asText(), "'" + expression + "' has unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_StringAndValue() {
        String actual = Eel.compile(context, "Hello $(2 ** 8) World")
            .evaluate()
            .asText();

        assertEquals("Hello 256 World", actual, "Unexpected String And Value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_EmbeddedFunction() {
        String actual = Eel.compile(context, "~~~ $( max(-3, 1, 23, 101) ) ~~~")
            .evaluate()
            .asText();

        assertEquals("~~~ 101 ~~~", actual, "Unexpected value");
    }

     /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_EmbeddedFunctionInterpolation() {
        boolean actual = Eel.compile(context, "$( $date.plus(${unknown-2000-01-02T12:00}, '5m') isAfter 0 )")
            .evaluate()
            .asLogic();

        assertTrue(actual, "Unexpected value");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_Dollar() {
        assertEquals("~~~ 0 ~~~ 1 ~~~",
            Eel.compile(context, "~~~ $count() ~~~ $count() ~~~").evaluate().asText(),
            "Function Interpolation");
        assertEquals("$99",
            Eel.compile(context, "$99").evaluate().asText(),
            "Digits");
        assertEquals("$  random(99,99)",
            Eel.compile(context, "$  random(99,99)").evaluate().asText(),
            "Spaces");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseCompiledExpression() {
        Eel compiled = Eel.factory()                            // Create a new Context for count
            .compile("Result is $( count() )");

        assertEquals("Result is 0", compiled.evaluate().asText(), "First");
        assertEquals("Result is 1", compiled.evaluate().asText(), "Second");
        assertEquals("Result is 2", compiled.evaluate().asText(), "Third");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_lazyEvaluation() {
        Result result = Eel.compile(context, "$indexOf('abcd', 'c', log.debug('test', fail('Never evaluated') ) )")
            .evaluate();

        // The real test is that fail() is returned from log.debug(), but never evaluated

        assertEquals(2, result.asInt(), "Unexpected Index");
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

        assertEquals(expected1, result1.asInt(), "expression re-read");             // should not change
        assertEquals((expected1 + 1), result2.asInt(), "expression re-evaluated");  // should have changed
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_constTerm_LookBack() {
        Eel compiled = Eel.compile(context, "$count()");

        // Bump counter to 3
        compiled.evaluate();
        compiled.evaluate();
        compiled.evaluate();

        Result actual = Eel.compile(context, "$( 2 ; count() ; $[1] + $[2] + $[2] )")    // $[2] should be consistently 3
            .evaluate();

        assertEquals(8, actual.asInt(), "Unexpected result");
    }
}
