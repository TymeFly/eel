package com.github.tymefly.eel.doc.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Group}
 */
public class GroupTest {
    private Group general = new Group("");
    private Group text = new Group("text");
    private Group number = new Group("number");
    private Group user1 = new Group("xyz");
    private Group user2 = new Group("abc");


    /**
     * Unit test {@link Group#name()}
     */
    @Test
    public void test_name() {
        Assert.assertEquals("general group", "General utilities", general.name());
        Assert.assertEquals("text group", "text", text.name());
        Assert.assertEquals("number group", "number", number.name());
        Assert.assertEquals("user1 group", "xyz", user1.name());
        Assert.assertEquals("user2 group", "abc", user2.name());
    }

    /**
     * Unit test {@link Group#fileName()}
     */
    @Test
    public void test_fileName() {
        Assert.assertEquals("general group", "_$.html", general.fileName());
        Assert.assertEquals("text group", "_text.html", text.fileName());
        Assert.assertEquals("number group", "_number.html", number.fileName());
        Assert.assertEquals("user1 group", "_xyz.html", user1.fileName());
        Assert.assertEquals("user2 group", "_abc.html", user2.fileName());
    }

    /**
     * Unit test {@link Group#displayOrder()}
     */
    @Test
    public void test_displayOrder() {
        Assert.assertEquals("general group", 0, general.displayOrder());
        Assert.assertEquals("text group", 3, text.displayOrder());
        Assert.assertEquals("number group", 4, number.displayOrder());
        Assert.assertEquals("user1 group", 999, user1.displayOrder());
        Assert.assertEquals("user2 group", 999, user2.displayOrder());
    }

    /**
     * Unit test {@link Group#withDescription()} and {@link Group#hasDescription()}
     */
    @Test
    public void test_hasDescription() {
        general.withDescription();

        Assert.assertTrue("general group",general.hasDescription());
        Assert.assertFalse("text group", text.hasDescription());
    }


    /**
     * Unit test and Group#add(String, Function)} {@link Group#getFunctions()}
     */
    @Test
    public void test_getFunctions() {
        Function func1 = mock();
        Function func2 = mock();
        Function func3 = mock();
        Function func4 = mock();

        when(func1.name()).thenReturn("xyz");
        when(func2.name()).thenReturn("hidden");
        when(func3.name()).thenReturn("abc_1");
        when(func4.name()).thenReturn("abc_2");
        when(func2.isHidden()).thenReturn(true);

        user2.add(func1);
        user2.add(func2);
        user2.add(func3);
        user2.add(func4);

        Assert.assertEquals("Empty Group", List.of(), user1.getFunctions());
        Assert.assertEquals("Sorted and filtered", List.of(func3, func4, func1), user2.getFunctions());
    }
}