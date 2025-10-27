package com.github.tymefly.eel.doc.model;

import java.util.Optional;

import com.github.tymefly.eel.doc.source.Parameter;
import com.github.tymefly.eel.doc.utils.EelType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Param}
 */
public class ParamTest {
    private Param nullParam = new Param("nullParam", null);
    private Param first = new Param("first", new Parameter("first", EelType.TEXT, 0, null, false));
    private Param second = new Param("second", new Parameter("second", EelType.NUMBER, 1, "my description", false));
    private Param third = new Param("third", new Parameter("third", EelType.LOGIC, 2, "other description", true));


    /**
     * Unit test {@link Param#identifier()}
     */
    @Test
    public void test_identifier() {
        Assert.assertEquals("nullParam", "nullParam", nullParam.identifier());
        Assert.assertEquals("first", "first", first.identifier());
        Assert.assertEquals("second", "second", second.identifier());
        Assert.assertEquals("third", "third", third.identifier());
    }

    /**
     * Unit test {@link Param#isParameter()}
     */
    @Test
    public void test_isParameter() {
        Assert.assertFalse("nullParam", nullParam.isParameter());
        Assert.assertTrue("first", first.isParameter());
        Assert.assertTrue("second", second.isParameter());
        Assert.assertTrue("third", third.isParameter());
    }

    /**
     * Unit test {@link Param#isVarArgs()}
     */
    @Test
    public void test_isVarArgs() {
        Assert.assertFalse("nullParam", nullParam.isVarArgs());
        Assert.assertFalse("first", first.isVarArgs());
        Assert.assertFalse("second", second.isVarArgs());
        Assert.assertTrue("third", third.isVarArgs());
    }

    /**
     * Unit test {@link Param#type()}
     */
    @Test
    public void test_type() {
        Assert.assertEquals("nullParam", Optional.empty(), nullParam.type());
        Assert.assertEquals("first", Optional.of(EelType.TEXT), first.type());
        Assert.assertEquals("second", Optional.of(EelType.NUMBER), second.type());
        Assert.assertEquals("third", Optional.of(EelType.LOGIC), third.type());
    }

    /**
     * Unit test {@link Param#order()}
     */
    @Test
    public void test_order() {
        Assert.assertEquals("nullParam", -1, nullParam.order());
        Assert.assertEquals("first", 0, first.order());
        Assert.assertEquals("second", 1, second.order());
        Assert.assertEquals("third", 2, third.order());
    }

    /**
     * Unit test {@link Param#isDefaulted()}
     */
    @Test
    public void test_isDefaulted() {
        Assert.assertFalse("nullParam", nullParam.isDefaulted());
        Assert.assertFalse("first", first.isDefaulted());
        Assert.assertTrue("second", second.isDefaulted());
        Assert.assertTrue("third", third.isDefaulted());
    }

    /**
     * Unit test {@link Param#defaultDescription()}
     */
    @Test
    public void test_defaultDescription() {
        Assert.assertEquals("nullParam", Optional.empty(), nullParam.defaultDescription());
        Assert.assertEquals("first", Optional.empty(), first.defaultDescription());
        Assert.assertEquals("second", Optional.of("my description"), second.defaultDescription());
        Assert.assertEquals("third", Optional.of("other description"), third.defaultDescription());
    }
}