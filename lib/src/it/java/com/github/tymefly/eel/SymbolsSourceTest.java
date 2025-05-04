package com.github.tymefly.eel;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for {@link SymbolsSource}
 */
public class SymbolsSourceTest {
    private Map<String, String> backing;


    @Before
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

        Assert.assertEquals("Expected", "a", source.read("1"));
        Assert.assertNull("Unexpected", source.read("9"));
    }

    /**
     * Integration test {@link SymbolsSource#scoped}
     */
    @Test
    public void test_scoped() {
        SymbolsSource source = SymbolsSource.scoped("defined", "-", backing::get);

        Assert.assertEquals("Expected", "a", source.read("defined-1"));
        Assert.assertNull("Out of Scope",source.read("undefined-1"));
        Assert.assertNull("Unexpected", source.read("defined-9"));
    }
}