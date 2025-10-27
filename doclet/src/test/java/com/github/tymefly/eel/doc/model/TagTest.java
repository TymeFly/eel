package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Tag}
 */
public class TagTest {
    private Tag noReference = new Tag(TagType.SUMMARY);
    private Tag withReference = (Tag) new Tag(TagType.THROWS)
        .withReference("MyException", "myPackage.MyException");



    /**
     * Unit test {@link Tag#tagType()}
     */
    @Test
    public void test_tagType() {
        Assert.assertEquals("noReference", TagType.SUMMARY, noReference.tagType());
        Assert.assertEquals("withReference", TagType.THROWS, withReference.tagType());
    }

    /**
     * Unit test {@link Tag#reference()}
     */
    @Test
    public void test_reference() {
        Assert.assertEquals("noReference", Optional.empty(), noReference.reference());
        Assert.assertEquals("withReference", Optional.of("MyException"), withReference.reference());
    }

    /**
     * Unit test {@link Tag#target()}
     */
    @Test
    public void test_target() {
        Assert.assertNull("noReference",noReference.target());
        Assert.assertEquals("withReference", "myPackage.MyException", withReference.target());
    }
}