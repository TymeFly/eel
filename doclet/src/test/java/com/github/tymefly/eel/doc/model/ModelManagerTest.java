package com.github.tymefly.eel.doc.model;

import java.util.List;

import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link ModelManager}
 */
public class ModelManagerTest {
    private ModelManager manager;

    @BeforeEach
    public void setUp() {
        manager = new ModelManager();

        manager.addFunction("global", EelType.TEXT, "my.class.Func0()");
        manager.addFunction("group1.func1", EelType.TEXT, "my.class.Func1()");
        manager.addFunction("group1.func2", EelType.NUMBER, "my.class.Func2()");
        manager.addFunction("group2.func1", EelType.LOGIC, "my.class.Func3()");
    }

    /**
     * Unit test {@link ModelManager#textBlock()}
     */
    @Test
    public void test_textBlock() {
        TextBlock textBlock = (TextBlock) manager.textBlock();

        assertEquals(0, textBlock.text().size(), "block is not empty");
        assertNotSame(textBlock, manager.textBlock(), "Same block returned");
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

        assertEquals(1, general.getFunctions().size(), "general has unexpected size");
        assertEquals(2, group1.getFunctions().size(), "group1 has unexpected size");
        assertEquals(1, group2.getFunctions().size(), "group2 has unexpected size");
        assertEquals(0, unknown.getFunctions().size(), "unknown has unexpected size");
    }

    /**
     * Unit test {@link ModelManager#groups()}
     */
    @Test
    public void test_groups_empty() {
        assertEquals(List.of(), new ModelManager().groups(), "no groups");
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

        assertEquals(
            List.of("General utilities", "eel", "text", "number", "user_AAA", "user_XXX", "user_ZZZ"),
            actual,
            "Unexpected groups"
        );
    }

    /**
     * Unit test {@link ModelManager#hasFunction}
     */
    @Test
    public void test_hasFunction() {
        assertTrue(manager.hasFunction("global"), "global");
        assertTrue(manager.hasFunction("group1.func1"), "group1.func1");
        assertTrue(manager.hasFunction("group1.func2"), "group1.func2");
        assertTrue(manager.hasFunction("group2.func1"), "group2.func1");

        assertFalse(manager.hasFunction("group1.func3"), "group1.func3");
        assertFalse(manager.hasFunction("group3.func1"), "group3.func1");
    }

    /**
     * Unit test {@link ModelManager#bySignature(String)}
     */
    @Test
    public void test_bySignature() {
        assertNotNull(manager.bySignature("my.class.Func0()"), "global");
        assertNotNull(manager.bySignature("my.class.Func1()"), "group1.func1");
        assertNotNull(manager.bySignature("my.class.Func2()"), "group1.func2");
        assertNotNull(manager.bySignature("my.class.Func3()"), "group2.func1");

        assertNull(manager.bySignature("another.func()"), "group1.func3");
    }
}
