package com.github.tymefly.eel.doc.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Group}
 */
public class GroupTest {
    private final Group general = new Group("");
    private final Group text = new Group("text");
    private final Group number = new Group("number");
    private final Group user1 = new Group("xyz");
    private final Group user2 = new Group("abc");


    /**
     * Unit test {@link Group#name()}
     */
    @Test
    public void test_name() {
        assertEquals("General utilities", general.name(), "general group");
        assertEquals("text", text.name(), "text group");
        assertEquals("number", number.name(), "number group");
        assertEquals("xyz", user1.name(), "user1 group");
        assertEquals("abc", user2.name(), "user2 group");
    }

    /**
     * Unit test {@link Group#fileName()}
     */
    @Test
    public void test_fileName() {
        assertEquals("_$.html", general.fileName(), "general group");
        assertEquals("_text.html", text.fileName(), "text group");
        assertEquals("_number.html", number.fileName(), "number group");
        assertEquals("_xyz.html", user1.fileName(), "user1 group");
        assertEquals("_abc.html", user2.fileName(), "user2 group");
    }

    /**
     * Unit test {@link Group#displayOrder()}
     */
    @Test
    public void test_displayOrder() {
        assertEquals(0, general.displayOrder(), "general group");
        assertEquals(3, text.displayOrder(), "text group");
        assertEquals(4, number.displayOrder(), "number group");
        assertEquals(999, user1.displayOrder(), "user1 group");
        assertEquals(999, user2.displayOrder(), "user2 group");
    }

    /**
     * Unit test {@link Group#withDescription()} and {@link Group#hasDescription()}
     */
    @Test
    public void test_hasDescription() {
        general.withDescription();

        assertTrue(general.hasDescription(), "general group");
        assertFalse(text.hasDescription(), "text group");
    }

    /**
     * Unit test and {@link Group#add(Function)} {@link Group#getFunctions()}
     */
    @Test
    public void test_getFunctions() {
        Function func1 = mock(Function.class);
        Function func2 = mock(Function.class);
        Function func3 = mock(Function.class);
        Function func4 = mock(Function.class);

        when(func1.name()).thenReturn("xyz");
        when(func2.name()).thenReturn("hidden");
        when(func3.name()).thenReturn("abc_1");
        when(func4.name()).thenReturn("abc_2");
        when(func2.isHidden()).thenReturn(true);

        user2.add(func1);
        user2.add(func2);
        user2.add(func3);
        user2.add(func4);

        assertEquals(List.of(), user1.getFunctions(), "Empty Group");
        assertEquals(List.of(func3, func4, func1), user2.getFunctions(), "Sorted and filtered");
    }
}
