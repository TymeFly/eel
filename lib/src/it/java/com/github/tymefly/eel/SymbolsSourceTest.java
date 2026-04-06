package com.github.tymefly.eel;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Integration test for {@link SymbolsSource}
 */
public class SymbolsSourceTest {
    private Map<String, String> backing;


    @BeforeEach
    public void setUp() {
        backing = Map.ofEntries(
            Map.entry("1", "a"),
            Map.entry("2", "b")
        );
    }

    /**
     * Integration test {@link SymbolsSource#unscoped}
     */
    @Test
    public void test_unscoped() {
        SymbolsSource source = SymbolsSource.unscoped(backing::get);

        assertEquals("a", source.read("1"), "Expected");
        assertNull(source.read("9"), "Unexpected");
    }

    /**
     * Integration test {@link SymbolsSource#scoped}
     */
    @Test
    public void test_scoped() {
        SymbolsSource source = SymbolsSource.scoped("defined", "-", backing::get);

        assertEquals("a", source.read("defined-1"), "Expected");
        assertNull(source.read("undefined-1"), "Out of Scope");
        assertNull(source.read("defined-9"), "Unexpected");
    }
}