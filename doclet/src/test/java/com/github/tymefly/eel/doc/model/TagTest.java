package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit test for {@link Tag}
 */
public class TagTest {
    private final Tag noReference = new Tag(TagType.SUMMARY);
    private final Tag withReference = (Tag) new Tag(TagType.THROWS)
        .withReference("MyException", "myPackage.MyException");



    /**
     * Unit test {@link Tag#tagType()}
     */
    @Test
    public void test_tagType() {
        assertEquals(TagType.SUMMARY, noReference.tagType(), "noReference");
        assertEquals(TagType.THROWS, withReference.tagType(), "withReference");
    }

    /**
     * Unit test {@link Tag#reference()}
     */
    @Test
    public void test_reference() {
        assertEquals(Optional.empty(), noReference.reference(), "noReference");
        assertEquals(Optional.of("MyException"), withReference.reference(), "withReference");
    }

    /**
     * Unit test {@link Tag#target()}
     */
    @Test
    public void test_target() {
        assertNull(noReference.target(), "noReference");
        assertEquals("myPackage.MyException", withReference.target(), "withReference");
    }
}