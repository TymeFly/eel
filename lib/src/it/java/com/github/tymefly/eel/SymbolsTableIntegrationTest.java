package com.github.tymefly.eel;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Integration Tests on the EEL SymbolsTable
 */
@ExtendWith(SystemStubsExtension.class)
public class SymbolsTableIntegrationTest {
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

        assertEquals("Foo <-/-> Bar <-/-> ????", actual, "Unexpected value");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withMap() {
        String actual = Eel.compile(context, "${hello[12]-???}")
            .evaluate(Map.of("hello[12]", "world"))
            .asText();

        assertEquals("world", actual, "Failed to read Map");
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_withLookup() {
        String actual = Eel.compile(context, "${key-???}")
            .evaluate(String::toUpperCase)
            .asText();

        assertEquals("KEY", actual, "Failed to read Map");
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

        assertEquals(expected, actual, "Failed to read Env");
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

        assertEquals(expected, actual, "Failed to read Map");
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

        assertEquals("#1: Foo", expression1.evaluate(table).asText(), "First");
        assertEquals("#2: Bar", expression2.evaluate(table).asText(), "Second");
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_UseAnotherSymbolsTable() {
        SymbolsTable table1 = SymbolsTable.from(Map.of("a", "1"));
        SymbolsTable table2 = SymbolsTable.from(Map.of("a", "2"));

        Eel expression = Eel.compile(context, "result #$random( ${a}, ${a})");

        assertEquals("result #1", expression.evaluate(table1).asText(), "First");
        assertEquals("result #2", expression.evaluate(table2).asText(), "Second");
    }


    /**
     * Integration test {@link Eel}.
     * Test SymbolsTable can contain reserved words and standard functions names
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

            assertEquals(key + " is mapped to " + value, expression.evaluate(table).asText(), key);
        }
    }
}
