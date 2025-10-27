package com.github.tymefly.eel.doc.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Unit test for {@link Config}
 */
public class ConfigTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();


    /**
     * Unit test {@link Config#getInstance()}
     */
    @Test
    public void test_isSingleton() {
        Assert.assertSame("Not singleton", Config.getInstance(), Config.getInstance());
    }

    /**
     * Unit test {@link Config#options()}
     */
    @Test
    public void test_options() {
        Assert.assertEquals("Unexpected option count", 15, Config.getInstance().options().size());
    }

    /**
     * Unit test {@link Config} settings
     */
    @Test
    public void test_Config_defaults() {
        Config config = new Config();

        Assert.assertEquals("title", "EEL-Doc", config.title());
        Assert.assertEquals("docTitle", "EEL Functions", config.docTitle());
        Assert.assertEquals("charSet", "UTF-8", config.charSet());
        Assert.assertEquals("docEncoding", "UTF-8", config.docEncoding());
        Assert.assertFalse("author", config.author());
        Assert.assertFalse("version", config.version());
        Assert.assertFalse("ignoreErrors", config.ignoreErrors());
        Assert.assertFalse("allReferences", config.allReferences());
    }

    /**
     * Unit test {@link Config} settings
     */
    @Test
    public void test_Config_allSet() {
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

        Assert.assertTrue("targetDirectory",
            config.targetDirectory().getAbsolutePath().replace("\\", "/").endsWith("/my/path"));
        Assert.assertEquals("title", "Window: Abcdef", config.title());
        Assert.assertEquals("docTitle", "Document: abcdef", config.docTitle());
        Assert.assertEquals("top", Optional.of("Top message"), config.top());
        Assert.assertEquals("bottom", Optional.of("Bottom message"), config.bottom());
        Assert.assertEquals("charSet", "US-ASCII", config.charSet());
        Assert.assertEquals("docEncoding", "ISO-8859-1", config.docEncoding());
        Assert.assertTrue("author", config.author());
        Assert.assertTrue("version", config.version());
        Assert.assertTrue("ignoreErrors", config.ignoreErrors());
        Assert.assertTrue("allReferences", config.allReferences());
        Assert.assertEquals("overview", Optional.of(new File("/path/to/my.file")), config.overview());
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