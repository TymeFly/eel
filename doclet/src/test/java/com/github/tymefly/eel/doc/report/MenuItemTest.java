package com.github.tymefly.eel.doc.report;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link MenuItem}
 */
public class MenuItemTest {
    /**
     * Unit test {@link MenuItem#getHref()}
     */
    @Test
    public void test_getHref() {
        assertEquals("index.html", MenuItem.OVERVIEW.getHref(), "OVERVIEW");
        assertEquals("_index.html", MenuItem.INDEX.getHref(), "INDEX");
        assertEquals("https://github.com/TymeFly/eel?tab=readme-ov-file#eel", MenuItem.EEL.getHref(), "EEL");
    }

    /**
     * Unit test {@link MenuItem#toString()}
     */
    @Test
    public void test_toString() {
        assertEquals("Overview", MenuItem.OVERVIEW.toString(), "OVERVIEW");
        assertEquals("Index", MenuItem.INDEX.toString(), "INDEX");
        assertEquals("EEL", MenuItem.EEL.toString(), "EEL");
    }
}