package com.github.tymefly.eel;

import java.nio.file.FileSystems;
import java.util.Map;
import java.util.function.Function;

import com.github.tymefly.eel.builder.ScopedSymbolsTableBuilder;
import com.github.tymefly.eel.builder.SymbolsTableBuilder;
import com.github.tymefly.eel.exception.EelSymbolsTableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link SymbolsTable}
 */
public class SymbolsTableTest {
    /**
     * Unit test {@link SymbolsTableBuilder#withProperties()}
     */
    @Test
    public void test_withProperties() {
        SymbolsTable table = SymbolsTable.factory()
            .withProperties()
            .build();

        assertEquals(FileSystems.getDefault().getSeparator(), table.read("file.separator"), "Failed to read table");
    }

    /**
     * Unit test {@link SymbolsTableBuilder#withEnvironment()}
     */
    @Test
    public void test_withEnvironment() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();

        SymbolsTable table = SymbolsTable.factory()
            .withEnvironment()
            .build();

        assertEquals(variable.getValue(), table.read(variable.getKey()), "Failed to read env");
    }

    /**
     * Unit test {@link SymbolsTableBuilder#withValues(Map)}
     */
    @Test
    public void test_withValues() {
        Map<String, String> map = Map.of("Key", "value", "Key2", "Value2");

        SymbolsTable table = SymbolsTable.factory()
            .withValues(map)
            .build();

        assertEquals("value", table.read("Key"), "Failed to read map");
    }

    /**
     * Unit test {@link SymbolsTableBuilder#withLookup(Function)}
     */
    @Test
    public void test_withLookup() {
        SymbolsTable table = SymbolsTable.factory()
            .withLookup(k -> new StringBuilder(k).reverse().toString())
            .build();

        assertEquals("yeK", table.read("Key"), "Failed to read via callback");
    }

    /**
     * Unit test {@link SymbolsTableBuilder#withDefault(String)}
     */
    @Test
    public void test_withDefault() {
        SymbolsTable table = SymbolsTable.factory()
            .withDefault("????")
            .build();

        assertEquals("????", table.read("Key"), "Failed to read via callback");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_priorities() {
        String property = System.getProperty("java.specification.version");

        SymbolsTable table = SymbolsTable.factory()
            .withValues(Map.of("file.separator", "overridden!"))
            .withValues(Map.of("file.separator", "FromMap2", "map2", "value-2"))
            .withProperties()
            .withDefault("<not set>")
            .build();

        // file.separator in the first map overrides values in the second map and the system properties
        assertEquals("overridden!", table.read("file.separator"), "Should have read Map1");
        assertEquals("value-2", table.read("map2"), "Should have read Map2");
        assertEquals(property, table.read("java.specification.version"), "Should have read props");
        assertEquals("<not set>", table.read("Key"), "Should have read default");
    }

    /**
     * Unit test {@link SymbolsTable#EMPTY}
     */
    @Test
    public void test_Empty() {
        assertNull(SymbolsTable.EMPTY.read("Key"), "Empty Table has value");
    }


    /**
     * Unit test {@link SymbolsTable#fromEnvironment()}
     */
    @Test
    public void test_fromEnvironment() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();
        String key = variable.getKey();
        String value = variable.getValue();

        SymbolsTable table = SymbolsTable.fromEnvironment();

        assertEquals(value, table.read(key), "Failed to read env");
    }

    /**
     * Unit test {@link SymbolsTable#fromEnvironment(String)}
     */
    @Test
    public void test_fromEnvironment_scoped() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();
        String key = variable.getKey();
        String value = variable.getValue();

        SymbolsTable table = SymbolsTable.fromEnvironment("env");

        assertEquals(value, table.read("env." + key), "Failed to read env");
        assertNotEquals("read without scope", table.read(key));
    }


    /**
     * Unit test {@link SymbolsTable#fromProperties()}
     */
    @Test
    public void test_fromProperties() {
        SymbolsTable table = SymbolsTable.fromProperties();

        assertEquals(FileSystems.getDefault().getSeparator(), table.read("file.separator"), "Failed to read property");
    }

    /**
     * Unit test {@link SymbolsTable#fromProperties()}
     */
    @Test
    public void test_fromProperties_scoped() {
        SymbolsTable table = SymbolsTable.fromProperties("prop");

        assertEquals(FileSystems.getDefault().getSeparator(), table.read("prop.file.separator"), "Failed to read property");
        assertNotEquals("read without scope", table.read("file.separator"));
    }


    /**
     * Unit test {@link SymbolsTable#from(Map)}
     */
    @Test
    public void test_fromMap() {
        Map<String, String> map = Map.of("Key", "value", "Key2", "Value2");

        SymbolsTable table = SymbolsTable.from(map);

        assertEquals("value", table.read("Key"), "Failed to read map");
    }

    /**
     * Unit test {@link SymbolsTable#from(Map)}
     */
    @Test
    public void test_fromMap_scoped() {
        Map<String, String> map = Map.of("Key", "value", "Key2", "Value2");

        SymbolsTable table = SymbolsTable.from("map", map);

        assertEquals("value", table.read("map.Key"), "Failed to read map");
        assertNotEquals("read without scope", table.read("Key"));
    }


    /**
     * Unit test {@link SymbolsTable#from(Function)}
     */
    @Test
    public void test_fromLookup() {
        SymbolsTable table = SymbolsTable.from(k -> new StringBuilder(k).reverse().toString());

        assertEquals("yeK", table.read("Key"), "Failed to read via callback");
    }

    /**
     * Unit test {@link SymbolsTable#from(Function)}
     */
    @Test
    public void test_fromLookup_scoped() {
        SymbolsTable table = SymbolsTable.from("func", k -> new StringBuilder(k).reverse().toString());

        assertEquals("yeK", table.read("func.Key"), "Failed to read via callback");
        assertNotEquals("read without scope", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable#from(String)}
     */
    @Test
    public void test_fromDefault() {
        SymbolsTable table = SymbolsTable.from("<myDefault>");

        assertEquals("<myDefault>", table.read("any key"), "Failed to read via default");
    }


    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_properties() {
        String property = System.getProperty("java.specification.version");

        SymbolsTable table = SymbolsTable.factory(".")
            .withProperties("props")
            .withDefault("<not set>")
            .build();

        assertEquals("<not set>", table.read("java.specification.version"), "Without scope");
        assertEquals("<not set>", table.read("xxx.java.specification.version"), "Wrong scope");
        assertEquals(property, table.read("props.java.specification.version"), "Correct scope");
        assertEquals("<not set>", table.read("Key"), "Should have read default");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_environment() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();

        SymbolsTable table = SymbolsTable.factory(".")
            .withEnvironment("env")
            .build();

        assertNull(table.read(variable.getKey()), "Without scope");
        assertNull(table.read("xxx" + variable.getKey()), "Wrong scope");
        assertEquals(variable.getValue(), table.read("env." + variable.getKey()), "Correct scope");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_maps() {
        Map<String, String> map1 = Map.of("Key", "value",  "Key2", "value2");
        Map<String, String> map2 = Map.of("Key", "value1", "Key3", "value3");

        SymbolsTable table = SymbolsTable.factory(".")
            .withValues("map1", map1)
            .withValues("map2", map2)
            .build();

        assertEquals("value", table.read("map1.Key"), "Failed to read Key in map1");
        assertEquals("value1", table.read("map2.Key"), "Failed to read Key in map2");

        assertEquals("value2", table.read("map1.Key2"), "Failed to read Key2 in map1");
        assertNull(table.read("map2.Key2"), "Failed to read Key2 in map2");

        assertNull(table.read("map1.Key3"), "Failed to read Key3 in map1");
        assertEquals("value3", table.read("map2.Key3"), "Failed to read Key3 in map2");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_lookup() {
        SymbolsTable table = SymbolsTable.factory(".")
            .withLookup("f1", String::toUpperCase)
            .withLookup("f2", String::toLowerCase)
            .build();

        assertEquals("SOME_TEXT", table.read("f1.Some_Text"), "Failed to call function 1");
        assertEquals("some_text", table.read("f2.Some_Text"), "Failed to call function 2");
        assertNull(table.read("badFunction"), "Called bad function");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_badName() {
        ScopedSymbolsTableBuilder factory = SymbolsTable.factory(".");

        EelSymbolsTableException actual = assertThrows(EelSymbolsTableException.class,
            () -> factory.withLookup("a.b", String::toUpperCase));

        assertEquals("Scope name 'a.b' contains delimiter '.'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_duplicateName() {
        ScopedSymbolsTableBuilder factory = SymbolsTable.factory(".")
            .withLookup("x", String::toUpperCase);

        EelSymbolsTableException actual = assertThrows(EelSymbolsTableException.class,
            () -> factory.withLookup("x", String::toUpperCase));

        assertEquals("Duplicate scope 'x'", actual.getMessage(), "Unexpected message");
    }
}