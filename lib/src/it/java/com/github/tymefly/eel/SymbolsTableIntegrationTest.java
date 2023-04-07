package com.github.tymefly.eel;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Integration Tests on the EEL Symbols Table
 */
public class SymbolsTableIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();



    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withSymbolsTable() {
        SymbolsTable table = SymbolsTable.factory()
            .withProperties()
            .withEnvironment()
            .withValues(Map.of("Key1", "Foo",
                "Key2", "Bar"))
            .withDefault("????")
            .build();

        String actual = Eel.compile("${Key1} <-${file.separator}-> ${Key2} <-${file.separator}-> ${unknown}")
            .evaluate(table)
            .asText();

        actual = actual.replace('\\', '/');

        Assert.assertEquals("Unexpected value", "Foo <-/-> Bar <-/-> ????", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withMap() {
        String actual = Eel.compile("${hello-???}")
            .evaluate(Map.of("hello", "world"))
            .asText();

        Assert.assertEquals("Failed to read Map", "world", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withLookup() {
        String actual = Eel.compile("${key-???}")
            .evaluate(String::toUpperCase)
            .asText();

        Assert.assertEquals("Failed to read Map", "KEY", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withEnvironment() {
        Map<String, String> env = System.getenv();
        String key = env.keySet().iterator().next();
        String expected = env.get(key);

        String actual = Eel.compile("${" + key + "-???}")
            .evaluateEnvironment()
            .asText();

        Assert.assertEquals("Failed to read Env", expected, actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withProperties() {
        String expected = System.getProperty("user.dir");
        String actual = Eel.compile("${user.dir-???}")
            .evaluateProperties()
            .asText();

        Assert.assertEquals("Failed to read Map", expected, actual);
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_ReuseSymbolsTable() {
        SymbolsTable table = SymbolsTable.factory()
            .withProperties()
            .withEnvironment()
            .withValues(Map.of("Key1", "Foo",
                "Key2", "Bar"))
            .withDefault("????")
            .build();

        Eel expression1 = Eel.compile("#1: ${Key1}");
        Eel expression2 = Eel.compile("#2: ${Key2}");

        Assert.assertEquals("First", "#1: Foo", expression1.evaluate(table).asText());
        Assert.assertEquals("Second", "#2: Bar", expression2.evaluate(table).asText());
    }
}
