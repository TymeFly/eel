package com.github.tymefly.eel;

import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link SymbolsTable}
 */
public class SymbolsTableTest {
    /**
     * Unit test {@link SymbolsTable.Builder#withProperties()}
     */
    @Test
    public void test_withProperties() {
        SymbolsTable table = SymbolsTable.factory()
            .withProperties()
            .build();

        Assert.assertEquals("Failed to read table", System.getProperty("file.separator"), table.read("file.separator"));
    }

    /**
     * Unit test {@link SymbolsTable.Builder#withEnvironment()}
     */
    @Test
    public void test_withEnvironment() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();

        SymbolsTable table = SymbolsTable.factory()
            .withEnvironment()
            .build();

        Assert.assertEquals("Failed to read env", variable.getValue(), table.read(variable.getKey()));
    }

    /**
     * Unit test {@link SymbolsTable.Builder#withValues(Map)}
     */
    @Test
    public void test_withValues() {
        Map<String, String> map = Map.of("Key", "value", "Key2", "Value2");

        SymbolsTable table = SymbolsTable.factory()
            .withValues(map)
            .build();

        Assert.assertEquals("Failed to read map", "value", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable.Builder#withLookup(Function)}
     */
    @Test
    public void test_withLookup() {
        SymbolsTable table = SymbolsTable.factory()
            .withLookup(k -> new StringBuilder(k).reverse().toString())
            .build();

        Assert.assertEquals("Failed to read via callback", "yeK", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable.Builder#withDefault(String)}
     */
    @Test
    public void test_withDefault() {
        SymbolsTable table = SymbolsTable.factory()
            .withDefault("????")
            .build();

        Assert.assertEquals("Failed to read via callback", "????", table.read("Key"));
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

        // file.separator in first map overrides values in the second map and the system properties
        Assert.assertEquals("Should have read Map1", "overridden!", table.read("file.separator"));
        Assert.assertEquals("Should have read Map2", "value-2", table.read("map2"));
        Assert.assertEquals("Should have read props", property, table.read("java.specification.version"));
        Assert.assertEquals("Should have read default", "<not set>", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable#EMPTY}
     */
    @Test
    public void test_Empty() {
        Assert.assertNull("Empty Table has value", SymbolsTable.EMPTY.read("Key"));
    }


    /**
     * Unit test {@link SymbolsTable#fromEnvironment()}
     */
    @Test
    public void test_fromEnvironment() {
        Map.Entry<String, String> variable = System.getenv().entrySet().iterator().next();

        SymbolsTable table = SymbolsTable.fromEnvironment();

        Assert.assertEquals("Failed to read env", variable.getValue(), table.read(variable.getKey()));
    }

    /**
     * Unit test {@link SymbolsTable#fromProperties()}
     */
    @Test
    public void test_fromProperties() {
        SymbolsTable table = SymbolsTable.fromProperties();

        Assert.assertEquals("Failed to read property",
            System.getProperty("file.separator"),
            table.read("file.separator"));
    }

    /**
     * Unit test {@link SymbolsTable#from(Map)}
     */
    @Test
    public void test_fromMap() {
        Map<String, String> map = Map.of("Key", "value", "Key2", "Value2");

        SymbolsTable table = SymbolsTable.from(map);

        Assert.assertEquals("Failed to read map", "value", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable#from(Function)}
     */
    @Test
    public void test_fromLookup() {
        SymbolsTable table = SymbolsTable.from(k -> new StringBuilder(k).reverse().toString());

        Assert.assertEquals("Failed to read via callback", "yeK", table.read("Key"));
    }

    /**
     * Unit test {@link SymbolsTable#from(String)}
     */
    @Test
    public void test_fromDefault() {
        SymbolsTable table = SymbolsTable.from("<myDefault>");

        Assert.assertEquals("Failed to read via default", "<myDefault>", table.read("any key"));
    }
}