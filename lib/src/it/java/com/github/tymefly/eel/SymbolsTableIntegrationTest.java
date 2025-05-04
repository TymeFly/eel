package com.github.tymefly.eel;

import java.time.Duration;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Integration Tests on the EEL Symbols Table
 */
public class SymbolsTableIntegrationTest {
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
    public void test_withSymbolsTable() {
        SymbolsTable table = SymbolsTable.factory()
            .withProperties()
            .withEnvironment()
            .withValues(Map.of("Key1", "Foo",
                "Key2", "Bar"))
            .withDefault("????")
            .build();

        String actual = Eel.compile(context, "${Key1} <-${file.separator}-> ${Key2} <-${file.separator}-> ${unknown}")
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
        String actual = Eel.compile(context, "${hello[12]-???}")
            .evaluate(Map.of("hello[12]", "world"))
            .asText();

        Assert.assertEquals("Failed to read Map", "world", actual);
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withLookup() {
        String actual = Eel.compile(context, "${key-???}")
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

        String actual = Eel.compile(context, "${" + key + "-???}")
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
        String actual = Eel.compile(context, "${user.dir-???}")
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

        Eel expression1 = Eel.compile(context, "#1: ${Key1}");
        Eel expression2 = Eel.compile(context, "#2: ${Key2}");

        Assert.assertEquals("First", "#1: Foo", expression1.evaluate(table).asText());
        Assert.assertEquals("Second", "#2: Bar", expression2.evaluate(table).asText());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UseAnotherSymbolsTable() {
        SymbolsTable table1 = SymbolsTable.from(Map.of("a", "1"));
        SymbolsTable table2 = SymbolsTable.from(Map.of("a", "2"));

        Eel expression = Eel.compile(context, "result #$random( ${a}, ${a})");

        Assert.assertEquals("First", "result #1", expression.evaluate(table1).asText());
        Assert.assertEquals("Second", "result #2", expression.evaluate(table2).asText());
    }


    /**
     * Integration test {@link Eel}.
     * Test symbols table can contain reserved words and standard functions names
     */
    @Test
    public void test_reservedWords() {
        String[] reserved = {
            "true",                         // reserved words
            "false",
            "text",
            "number",
            "logic",
            "date",
            "not",
            "and",
            "or",
            "xor",
            "isBefore",
            "isAfter",
            "in",
            "lower",
            "eel.version",                  // Standard functions
            "system.home",
            "system.home",
            "io.head",
            "number.pi",
            "date.start",
            "log.error",
            "format.binary"
        };

        int loop = 0;
        for (var key : reserved) {
            String value = Integer.toString(loop);
            SymbolsTable table = SymbolsTable.from(Map.of(key, value));
            Eel expression = Eel.compile(context, key + " is mapped to ${" + key + "}" );

            Assert.assertEquals(key, key + " is mapped to " + value, expression.evaluate(table).asText());
        }
    }
}
