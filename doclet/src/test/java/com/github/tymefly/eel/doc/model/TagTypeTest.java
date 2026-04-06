package com.github.tymefly.eel.doc.model;

import java.util.List;

import com.github.tymefly.eel.doc.config.Config;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals(1, TagType.Cardinality.SINGLETON.getMaxValue(), "SINGLETON");
        assertEquals(0x7FFFFFFF, TagType.Cardinality.UNBOUNDED.getMaxValue(), "UNBOUNDED");
    }

    /**
     * Unit test {@link TagType#sections()}
     */
    @Test
    public void test_sections() {
        assertEquals(
            List.of(TagType.RETURN, TagType.THROWS, TagType.SEE, TagType.SINCE, TagType.VERSION, TagType.AUTHOR),
            TagType.sections(),
            "Unexpected sections to display");
    }

    /**
     * Unit test {@link TagType#cardinality()}
     */
    @Test
    public void test_cardinality() {
        assertEquals(TagType.Cardinality.SINGLETON, TagType.SUMMARY.cardinality(), "SUMMARY");
        assertEquals(TagType.Cardinality.SINGLETON, TagType.DEPRECATED.cardinality(), "DEPRECATED");
        assertEquals(TagType.Cardinality.SINGLETON, TagType.RETURN.cardinality(), "RETURN");
        assertEquals(TagType.Cardinality.UNBOUNDED, TagType.THROWS.cardinality(), "THROWS");
        assertEquals(TagType.Cardinality.UNBOUNDED, TagType.SEE.cardinality(), "SEE");
        assertEquals(TagType.Cardinality.SINGLETON, TagType.SINCE.cardinality(), "SINCE");
        assertEquals(TagType.Cardinality.SINGLETON, TagType.VERSION.cardinality(), "VERSION");
        assertEquals(TagType.Cardinality.UNBOUNDED, TagType.AUTHOR.cardinality(), "AUTHOR");
    }

    /**
     * Unit test {@link TagType#isEnabled()}
     */
    @Test
    public void test_isEnabled_defaults() {
        assertTrue(TagType.SUMMARY.isEnabled(), "SUMMARY");
        assertTrue(TagType.DEPRECATED.isEnabled(), "DEPRECATED");
        assertTrue(TagType.RETURN.isEnabled(), "RETURN");
        assertTrue(TagType.THROWS.isEnabled(), "THROWS");
        assertTrue(TagType.SEE.isEnabled(), "SEE");
        assertTrue(TagType.SINCE.isEnabled(), "SINCE");
        assertTrue(TagType.VERSION.isEnabled(), "VERSION");
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

            assertFalse(TagType.AUTHOR.isEnabled(), "AUTHOR");
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

            assertTrue(TagType.AUTHOR.isEnabled(), "AUTHOR");
        }
    }

    /**
     * Unit test {@link TagType#showAllReference()}
     */
    @Test
    public void test_showAllReference_defaults() {
        assertTrue(TagType.SUMMARY.showAllReference(), "SUMMARY");
        assertTrue(TagType.DEPRECATED.showAllReference(), "DEPRECATED");
        assertTrue(TagType.RETURN.showAllReference(), "RETURN");
        assertTrue(TagType.THROWS.showAllReference(), "THROWS");
        assertTrue(TagType.SINCE.showAllReference(), "SINCE");
        assertTrue(TagType.VERSION.showAllReference(), "VERSION");
        assertTrue(TagType.AUTHOR.showAllReference(), "AUTHOR");
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

            assertFalse(TagType.SEE.showAllReference(), "SEE");
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

            assertTrue(TagType.SEE.showAllReference(), "SEE");
        }
    }


    /**
     * Unit test {@link TagType#toString()}
     */
    @Test
    public void test_toString() {
        assertEquals("Summary", TagType.SUMMARY.toString(), "SUMMARY");
        assertEquals("Deprecated", TagType.DEPRECATED.toString(), "DEPRECATED");
        assertEquals("Returns", TagType.RETURN.toString(), "RETURN");
        assertEquals("Throws", TagType.THROWS.toString(), "THROWS");
        assertEquals("See", TagType.SEE.toString(), "SEE");
        assertEquals("Since", TagType.SINCE.toString(), "SINCE");
        assertEquals("Version", TagType.VERSION.toString(), "VERSION");
        assertEquals("Author", TagType.AUTHOR.toString(), "AUTHOR");
    }
}