package com.github.tymefly.eel;

import java.util.Map;
import java.util.function.Function;

import com.github.tymefly.eel.builder.ScopedSymbolsTableBuilder;
import com.github.tymefly.eel.exception.EelSymbolsTableException;
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

        Assert.assertEquals("Without scope", "<not set>", table.read("java.specification.version"));
        Assert.assertEquals("Wrong scope", "<not set>", table.read("xxx.java.specification.version"));
        Assert.assertEquals("Correct scope", property, table.read("props.java.specification.version"));
        Assert.assertEquals("Should have read default", "<not set>", table.read("Key"));
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

        Assert.assertNull("Without scope", table.read(variable.getKey()));
        Assert.assertNull("Wrong scope",table.read("xxx" + variable.getKey()));
        Assert.assertEquals("Correct scope", variable.getValue(), table.read("env." + variable.getKey()));
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

        Assert.assertEquals("Failed to read Key in map1", "value", table.read("map1.Key"));
        Assert.assertEquals("Failed to read Key in map2", "value1", table.read("map2.Key"));

        Assert.assertEquals("Failed to read Key2 in map1", "value2", table.read("map1.Key2"));
        Assert.assertNull("Failed to read Key2 in map2", table.read("map2.Key2"));

        Assert.assertNull("Failed to read Key3 in map1", table.read("map1.Key3"));
        Assert.assertEquals("Failed to read Key3 in map2", "value3", table.read("map2.Key3"));
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

        Assert.assertEquals("Failed to call function 1", "SOME_TEXT", table.read("f1.Some_Text"));
        Assert.assertEquals("Failed to call function 2", "some_text", table.read("f2.Some_Text"));
        Assert.assertNull("Called bad function", table.read("badFunction"));
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_badName() {
        ScopedSymbolsTableBuilder factory = SymbolsTable.factory(".");

        EelSymbolsTableException actual = Assert.assertThrows(EelSymbolsTableException.class,
            () -> factory.withLookup("a.b", String::toUpperCase));

        Assert.assertEquals("Unexpected message", "Scope name 'a.b' contains delimiter '.'", actual.getMessage());
    }

    /**
     * Unit test {@link SymbolsTable}
     */
    @Test
    public void test_scoped_duplicateName() {
        ScopedSymbolsTableBuilder factory = SymbolsTable.factory(".")
            .withLookup("x", String::toUpperCase);

        EelSymbolsTableException actual = Assert.assertThrows(EelSymbolsTableException.class,
            () -> factory.withLookup("x", String::toUpperCase));

        Assert.assertEquals("Unexpected message", "Duplicate scope 'x'", actual.getMessage());
    }
}