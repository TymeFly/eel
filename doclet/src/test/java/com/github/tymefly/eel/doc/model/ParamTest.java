package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Param}
 */
public class ParamTest {
    private final Param nullParam = new Param("nullParam", null);
    private final Param first = new Param("first", new Parameter("first", EelType.TEXT, 0, null, false));
    private final Param second = new Param("second", new Parameter("second", EelType.NUMBER, 1, "my description", false));
    private final Param third = new Param("third", new Parameter("third", EelType.LOGIC, 2, "other description", true));


    /**
     * Unit test {@link Param#identifier()}
     */
    @Test
    public void test_identifier() {
        assertEquals("nullParam", nullParam.identifier(), "nullParam");
        assertEquals("first", first.identifier(), "first");
        assertEquals("second", second.identifier(), "second");
        assertEquals("third", third.identifier(), "third");
    }

    /**
     * Unit test {@link Param#isParameter()}
     */
    @Test
    public void test_isParameter() {
        assertFalse(nullParam.isParameter(), "nullParam");
        assertTrue(first.isParameter(), "first");
        assertTrue(second.isParameter(), "second");
        assertTrue(third.isParameter(), "third");
    }

    /**
     * Unit test {@link Param#isVarArgs()}
     */
    @Test
    public void test_isVarArgs() {
        assertFalse(nullParam.isVarArgs(), "nullParam");
        assertFalse(first.isVarArgs(), "first");
        assertFalse(second.isVarArgs(), "second");
        assertTrue(third.isVarArgs(), "third");
    }

    /**
     * Unit test {@link Param#type()}
     */
    @Test
    public void test_type() {
        assertEquals(Optional.empty(), nullParam.type(), "nullParam");
        assertEquals(Optional.of(EelType.TEXT), first.type(), "first");
        assertEquals(Optional.of(EelType.NUMBER), second.type(), "second");
        assertEquals(Optional.of(EelType.LOGIC), third.type(), "third");
    }

    /**
     * Unit test {@link Param#order()}
     */
    @Test
    public void test_order() {
        assertEquals(-1, nullParam.order(), "nullParam");
        assertEquals(0, first.order(), "first");
        assertEquals(1, second.order(), "second");
        assertEquals(2, third.order(), "third");
    }

    /**
     * Unit test {@link Param#isDefaulted()}
     */
    @Test
    public void test_isDefaulted() {
        assertFalse(nullParam.isDefaulted(), "nullParam");
        assertFalse(first.isDefaulted(), "first");
        assertTrue(second.isDefaulted(), "second");
        assertTrue(third.isDefaulted(), "third");
    }

    /**
     * Unit test {@link Param#defaultDescription()}
     */
    @Test
    public void test_defaultDescription() {
        assertEquals(Optional.empty(), nullParam.defaultDescription(), "nullParam");
        assertEquals(Optional.empty(), first.defaultDescription(), "first");
        assertEquals(Optional.of("my description"), second.defaultDescription(), "second");
        assertEquals(Optional.of("other description"), third.defaultDescription(), "third");
    }
}