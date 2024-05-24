package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.Duration;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Basic set of integration Tests
 */
public class BasicIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

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
    public void test_SingleString_nixPath() {
        String actual = Eel.compile(context, "/path/to/my/file.txt")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "/path/to/my/file.txt", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString_dosPath() {
        String actual = Eel.compile(context, "c:\\path\\to\\my\\file.txt")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "c:\\path\\to\\my\\file.txt", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_NumericValues() {
        BigDecimal actual = Eel.compile(context, "$(123)")
            .evaluate()
            .asNumber();

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
        Assert.assertEquals("Double $",
            "$99",
            Eel.compile(context, "$$random(99,99)").evaluate().asText());
        Assert.assertEquals("Nested in Expression",
            "$99",
            Eel.compile(context, "$$( $random(99,99) )").evaluate().asText());
        Assert.assertEquals("Nested in Value",
            "def:$99$",
            Eel.compile(context, "${UNDEFINED-def:$$random(99,99)$}").evaluate().asText());
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
}
