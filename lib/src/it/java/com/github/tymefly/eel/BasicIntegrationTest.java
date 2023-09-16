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
    public void test_SingleValue() {
        BigDecimal actual = Eel.compile(context, "$(123)")
            .evaluate()
            .asNumber();

        Assert.assertEquals("Unexpected Number", BigDecimal.valueOf(123), actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ConstantsValue() {
        testConstant("$( true )", Type.LOGIC, "true");
        testConstant("$( false )", Type.LOGIC, "false");
        testConstant("$( pi )", Type.NUMBER, "3.141592653589793");
        testConstant("$( e )", Type.NUMBER, "2.718281828459045");
        testConstant("$( c )", Type.NUMBER, "299792458");
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
        String actual = Eel.compile(context, "Hello $(2 ^ 8) World")
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
    public void test_ReuseCompiledExpression() {
        Eel compiled = Eel.factory()                            // Create a new Context for count
            .compile("Result is $( count() )");

        Assert.assertEquals("First", "Result is 1", compiled.evaluate().asText());
        Assert.assertEquals("Second", "Result is 2", compiled.evaluate().asText());
        Assert.assertEquals("Third", "Result is 3", compiled.evaluate().asText());
    }
}
