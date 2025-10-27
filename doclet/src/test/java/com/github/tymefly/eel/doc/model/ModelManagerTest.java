package com.github.tymefly.eel.doc.model;

import java.util.List;

import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ModelManager}
 */
public class ModelManagerTest {
    private ModelManager manager;


    @Before
    public void setUp() {
        manager = new ModelManager();

        manager.addFunction("global", EelType.TEXT, "my.class.Func0()");
        manager.addFunction("group1.func1", EelType.TEXT, "my.class.Func1()");
        manager.addFunction("group1.func2", EelType.NUMBER, "my.class.Func2()");
        manager.addFunction("group2.func1", EelType.LOGIC, "my.class.Func3()");
    }

    /**
     * Unit test {@link ModelManager#getInstance()}
     */
    @Test
    public void test_getInstance() {
        Assert.assertSame("Not a singleton", ModelManager.getInstance(), ModelManager.getInstance());
    }


    /**
     * Unit test {@link ModelManager#textBlock()}
     */
    @Test
    public void test_textBlock() {
        TextBlock textBlock = (TextBlock) manager.textBlock();

        Assert.assertEquals("block is not empty", 0, textBlock.text().size());
        Assert.assertNotSame("Same block returned", textBlock, manager.textBlock());
    }



    /**
     * Unit test {@link ModelManager#group(String)}
     */
    @Test
    public void test_group() {
        Group general = (Group) manager.group("");
        Group group1 = (Group) manager.group("group1");
        Group group2 = (Group) manager.group("group2");
        Group unknown = (Group) manager.group("unknown");

        Assert.assertEquals("general has unexpected size", 1, general.getFunctions().size());
        Assert.assertEquals("group1 has unexpected size", 2, group1.getFunctions().size());
        Assert.assertEquals("group2 has unexpected size", 1, group2.getFunctions().size());
        Assert.assertEquals("unknown has unexpected size", 0, unknown.getFunctions().size());
    }



    /**
     * Unit test {@link ModelManager#groups()}
     */
    @Test
    public void test_groups_empty() {
        Assert.assertEquals("no groups", List.of(), new ModelManager().groups());
    }


    /**
     * Unit test {@link ModelManager#groups()}
     */
    @Test
    public void test_groups_populated() {
        manager = new ModelManager();

        manager.group("");
        manager.group("text");
        manager.group("eel");
        manager.group("number");
        manager.group("user_XXX");
        manager.group("user_AAA");
        manager.group("user_ZZZ");

        List<String> actual = manager.groups()
            .stream()
            .map(GroupModel::name)
            .toList();

        Assert.assertEquals("Unexpected groups",
            List.of("General utilities", "eel", "text", "number", "user_AAA", "user_XXX", "user_ZZZ"),
            actual);
    }


    /**
     * Unit test {@link ModelManager#hasFunction}
     */
    @Test
    public void test_hasFunction() {
        Assert.assertTrue("global", manager.hasFunction("global"));
        Assert.assertTrue("group1.func1", manager.hasFunction("group1.func1"));
        Assert.assertTrue("group1.func2", manager.hasFunction("group1.func2"));
        Assert.assertTrue("group2.func1", manager.hasFunction("group2.func1"));

        Assert.assertFalse("group1.func3", manager.hasFunction("group1.func3"));
        Assert.assertFalse("group3.func1", manager.hasFunction("group3.func1"));
    }


    /**
     * Unit test {@link ModelManager#bySignature(String)}
     */
    @Test
    public void test_bySignature() {
        Assert.assertNotNull("global", manager.bySignature("my.class.Func0()"));
        Assert.assertNotNull("group1.func1", manager.bySignature("my.class.Func1()"));
        Assert.assertNotNull("group1.func2", manager.bySignature("my.class.Func2()"));
        Assert.assertNotNull("group2.func1", manager.bySignature("my.class.Func3()"));

        Assert.assertNull("group1.func3", manager.bySignature("another.func()"));
    }
}