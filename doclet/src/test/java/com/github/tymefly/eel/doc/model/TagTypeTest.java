package com.github.tymefly.eel.doc.model;

import java.util.List;

import com.github.tymefly.eel.doc.config.Config;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TagType}
 */
public class TagTypeTest {

    /**
     * Unit test {@link TagType.Cardinality#getMaxValue()}
     */
    @Test
    public void test_maxValue() {
        Assert.assertEquals("SINGLETON", 1, TagType.Cardinality.SINGLETON.getMaxValue());
        Assert.assertEquals("UNBOUNDED", 0x7FFFFFFF, TagType.Cardinality.UNBOUNDED.getMaxValue());
    }

    /**
     * Unit test {@link TagType#sections()}
     */
    @Test
    public void test_sections() {
        Assert.assertEquals("Unexpected sections to display",
            List.of(TagType.RETURN, TagType.THROWS, TagType.SEE, TagType.SINCE, TagType.VERSION, TagType.AUTHOR),
            TagType.sections());
    }

    /**
     * Unit test {@link TagType#cardinality()}
     */
    @Test
    public void test_cardinality() {
        Assert.assertEquals("SUMMARY", TagType.Cardinality.SINGLETON, TagType.SUMMARY.cardinality());
        Assert.assertEquals("DEPRECATED", TagType.Cardinality.SINGLETON, TagType.DEPRECATED.cardinality());
        Assert.assertEquals("RETURN", TagType.Cardinality.SINGLETON, TagType.RETURN.cardinality());
        Assert.assertEquals("THROWS", TagType.Cardinality.UNBOUNDED, TagType.THROWS.cardinality());
        Assert.assertEquals("SEE", TagType.Cardinality.UNBOUNDED, TagType.SEE.cardinality());
        Assert.assertEquals("SINCE", TagType.Cardinality.SINGLETON, TagType.SINCE.cardinality());
        Assert.assertEquals("VERSION", TagType.Cardinality.SINGLETON, TagType.VERSION.cardinality());
        Assert.assertEquals("AUTHOR", TagType.Cardinality.UNBOUNDED, TagType.AUTHOR.cardinality());
    }

    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_isEnabled_defaults() {
        Assert.assertTrue("SUMMARY", TagType.SUMMARY.isEnabled());
        Assert.assertTrue("DEPRECATED", TagType.DEPRECATED.isEnabled());
        Assert.assertTrue("RETURN", TagType.RETURN.isEnabled());
        Assert.assertTrue("THROWS", TagType.THROWS.isEnabled());
        Assert.assertTrue("SEE", TagType.SEE.isEnabled());
        Assert.assertTrue("SINCE", TagType.SINCE.isEnabled());
        Assert.assertTrue("VERSION", TagType.VERSION.isEnabled());
    }

    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_isEnabled_disableAuthor() {
        Config config = mock(Config.class, RETURNS_DEEP_STUBS);

        when(config.author())
            .thenReturn(false);

        try (
            MockedStatic<Config> configMock = mockStatic(Config.class)
        ) {
            configMock.when(Config::getInstance)
                .thenReturn(config);

            Assert.assertFalse("AUTHOR", TagType.AUTHOR.isEnabled());
        }
    }

    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_isEnabled_enableAuthor() {
        Config config = mock(Config.class, RETURNS_DEEP_STUBS);

        when(config.author())
            .thenReturn(true);

        try (
            MockedStatic<Config> configMock = mockStatic(Config.class)
        ) {
            configMock.when(Config::getInstance)
                .thenReturn(config);

            Assert.assertTrue("AUTHOR", TagType.AUTHOR.isEnabled());
        }
    }

    /**
     * Unit test {@link TagType#showAllReference()}
     */
    @Test
    public void test_showAllReference_defaults() {
        Assert.assertTrue("SUMMARY", TagType.SUMMARY.showAllReference());
        Assert.assertTrue("DEPRECATED", TagType.DEPRECATED.showAllReference());
        Assert.assertTrue("RETURN", TagType.RETURN.showAllReference());
        Assert.assertTrue("THROWS", TagType.THROWS.showAllReference());
        Assert.assertTrue("SINCE", TagType.SINCE.showAllReference());
        Assert.assertTrue("VERSION", TagType.VERSION.showAllReference());
        Assert.assertTrue("AUTHOR", TagType.AUTHOR.showAllReference());
    }


    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_showAllReference_disabled() {
        Config config = mock(Config.class, RETURNS_DEEP_STUBS);

        when(config.allReferences())
            .thenReturn(false);

        try (
            MockedStatic<Config> configMock = mockStatic(Config.class)
        ) {
            configMock.when(Config::getInstance)
                .thenReturn(config);

            Assert.assertFalse("SEE", TagType.SEE.showAllReference());
        }
    }


    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_showAllReference_enabled() {
        Config config = mock(Config.class, RETURNS_DEEP_STUBS);

        when(config.allReferences())
            .thenReturn(true);

        try (
            MockedStatic<Config> configMock = mockStatic(Config.class)
        ) {
            configMock.when(Config::getInstance)
                .thenReturn(config);

            Assert.assertTrue("SEE", TagType.SEE.showAllReference());
        }
    }


    /**
     * Unit test {@link TagType#toString()}
     */
    @Test
    public void test_toString() {
        Assert.assertEquals("SUMMARY", "Summary", TagType.SUMMARY.toString());
        Assert.assertEquals("DEPRECATED", "Deprecated", TagType.DEPRECATED.toString());
        Assert.assertEquals("RETURN", "Returns", TagType.RETURN.toString());
        Assert.assertEquals("THROWS", "Throws", TagType.THROWS.toString());
        Assert.assertEquals("SEE", "See", TagType.SEE.toString());
        Assert.assertEquals("SINCE", "Since", TagType.SINCE.toString());
        Assert.assertEquals("VERSION", "Version", TagType.VERSION.toString());
        Assert.assertEquals("AUTHOR", "Author", TagType.AUTHOR.toString());
    }
}