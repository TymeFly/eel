package com.github.tymefly.eel.doc.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Config}
 */
@ExtendWith(SystemStubsExtension.class)
class ConfigTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    /**
     * Unit test {@link Config#getInstance()}
     */
    @Test
    void test_isSingleton() {
        assertSame(Config.getInstance(), Config.getInstance(), "Not singleton");
    }

    /**
     * Unit test {@link Config#options()}
     */
    @Test
    void test_options() {
        assertEquals(15, Config.getInstance().options().size(), "Unexpected option count");
    }

    /**
     * Unit test {@link Config} settings
     */
    @Test
    void test_Config_defaults() {
        Config config = new Config();

        assertEquals("EEL-Doc", config.title(), "title");
        assertEquals("EEL Functions", config.docTitle(), "docTitle");
        assertEquals("UTF-8", config.charSet(), "charSet");
        assertEquals("UTF-8", config.docEncoding(), "docEncoding");
        assertFalse(config.author(), "author");
        assertFalse(config.version(), "version");
        assertFalse(config.ignoreErrors(), "ignoreErrors");
        assertFalse(config.allReferences(), "allReferences");
    }

    /**
     * Unit test {@link Config} settings
     */
    @Test
    void test_Config_allSet() {
        Config config = new Config();

        setFlag(config, "-version");
        setOption(config, "-d", "/my/path");
        setOption(config, "-windowtitle", "Window: $title('abcdef')");
        setOption(config, "-doctitle", "Document: $lower('ABCdef')");
        setOption(config, "-doc-overview", "/path/to/my.file");
        setOption(config, "-top", "Top message");
        setOption(config, "-bottom", "Bottom message");
        setOption(config, "-charset", StandardCharsets.US_ASCII.displayName());
        setOption(config, "-docencoding", StandardCharsets.ISO_8859_1.displayName());
        setFlag(config, "-author");
        setFlag(config, "-use");
        setFlag(config, "-Xdoclint:");
        setFlag(config, "-Ewarn");
        setFlag(config, "-allrefs");

        assertTrue(config.targetDirectory().getAbsolutePath().replace("\\", "/").endsWith("/my/path"), "targetDirectory");
        assertEquals("Window: Abcdef", config.title(), "title");
        assertEquals("Document: abcdef", config.docTitle(), "docTitle");
        assertEquals(Optional.of("Top message"), config.top(), "top");
        assertEquals(Optional.of("Bottom message"), config.bottom(), "bottom");
        assertEquals("US-ASCII", config.charSet(), "charSet");
        assertEquals("ISO-8859-1", config.docEncoding(), "docEncoding");
        assertTrue(config.author(), "author");
        assertTrue(config.version(), "version");
        assertTrue(config.ignoreErrors(), "ignoreErrors");
        assertTrue(config.allReferences(), "allReferences");
        assertEquals(Optional.of(new File("/path/to/my.file")), config.overview(), "overview");
    }

    private void setFlag(@Nonnull Config config, @Nonnull String name) {
        config.options()
            .stream()
            .filter(o -> o.getNames().get(0).equals(name))
            .findFirst()
            .ifPresent(o -> o.process("", List.of()));
    }

    private void setOption(@Nonnull Config config, @Nonnull String name, @Nonnull String value) {
        config.options()
            .stream()
            .filter(o -> o.getNames().get(0).equals(name))
            .findFirst()
            .ifPresent(o -> o.process("", List.of(value)));
    }
}
