package com.github.tymefly.eel;

import java.math.BigDecimal;

import org.junit.Assert;
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


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString() {
        String actual = Eel.compile("Hello World")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "Hello World", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString_nixPath() {
        String actual = Eel.compile("/path/to/my/file.txt")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "/path/to/my/file.txt", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleString_dosPath() {
        String actual = Eel.compile("c:\\path\\to\\my\\file.txt")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected String", "c:\\path\\to\\my\\file.txt", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_SingleValue() {
        BigDecimal actual = Eel.compile("$(123)")
            .evaluate()
            .asNumber();

        Assert.assertEquals("Unexpected Number", BigDecimal.valueOf(123), actual);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_StringAndValue() {
        String actual = Eel.compile("Hello $(2 ^ 8) World")
            .evaluate()
            .asText();

        Assert.assertEquals("Unexpected StringAndValue", "Hello 256 World", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_EmbeddedFunction() {
        String actual = Eel.compile("~~~ $( max(-3, 1, 23, 101) ) ~~~")
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
