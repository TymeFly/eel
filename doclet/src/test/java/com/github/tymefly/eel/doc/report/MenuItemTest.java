package com.github.tymefly.eel.doc.report;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link MenuItem}
 */
public class MenuItemTest {
    /**
     * Unit test {@link MenuItem#getHref()}
     */
    @Test
    public void test_getHref() {
        Assert.assertEquals("OVERVIEW", "index.html", MenuItem.OVERVIEW.getHref());
        Assert.assertEquals("INDEX", "_index.html", MenuItem.INDEX.getHref());
        Assert.assertEquals("EEL", "https://github.com/TymeFly/eel?tab=readme-ov-file#eel", MenuItem.EEL.getHref());
    }

    /**
     * Unit test {@link MenuItem#toString()}
     */
    @Test
    public void test_toString() {
        Assert.assertEquals("OVERVIEW", "Overview", MenuItem.OVERVIEW.toString());
        Assert.assertEquals("INDEX", "Index", MenuItem.INDEX.toString());
        Assert.assertEquals("EEL", "EEL", MenuItem.EEL.toString());
    }
}