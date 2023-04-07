package com.github.tymefly.eel.function.util;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Paths}
 */
public class PathsTest {
    /**
     * Unit test {@link Paths#realPath(String)}
     */
    @Test
    public void test_realPath() throws Exception {
        String currentDir = System.getProperty("user.dir");
        String slash = System.getProperty("file.separator");

        Assert.assertEquals("current dir", currentDir, new Paths().realPath("."));
        Assert.assertEquals("child dir", currentDir + slash + "child", new Paths().realPath("child"));
        Assert.assertEquals("file in dir", currentDir + slash + "read.me", new Paths().realPath("read.me"));
        Assert.assertEquals("remove current dir", currentDir, new Paths().realPath("./."));
        Assert.assertEquals("parent dir", currentDir + slash + "b", new Paths().realPath("a/../b"));
        Assert.assertEquals("absolute dir",
            "/a/c",
            new Paths().realPath("/a/b/../c/.").replaceFirst("^[A-Za-z]:", "").replace('\\', '/'));

    }


    /**
     * Unit test {@link Paths#dirName(String)}
     */
    @Test
    public void test_dirName() throws IOException {
        assertDirName("empty", "", "");
        assertDirName("Full Path (*nix)", "(.:)?/a/b/c", "/a/b/c/d.txt");
        assertDirName("Full Path (Windows)", "(.:)?/a/b/c", "c:\\a\\b\\c\\d.txt");
        assertDirName("No Extension", "(.:)?/a/b/c", "c:\\a\\b\\c\\d");
        assertDirName("Just File", "", "d.txt");
        assertDirName("Just File and Slash", "(.:)?/", "/d.txt");
        assertDirName("Relative Path", "(.:)?(/.+)?/a/b/c", "a/b/c/d.txt");
        assertDirName("With Navigation", "(.:)?/a/c", "\\a\\b\\..\\c\\d.txt");
    }

    private void assertDirName(@Nonnull String message,
                               @Nonnull String regEx,
                               @Nonnull String path) throws IOException {
        String actual = new Paths().dirName(path)
            .replace('\\', '/');

        Assert.assertTrue(message + ". '" + actual + "' failed to match '" + regEx + "'", actual.matches(regEx));
    }


    /**
     * Unit test {@link Paths#baseName(String, String)}
     */
    @Test
    public void test_baseName_withExtension() {
        Assert.assertEquals("empty", "", new Paths().baseName("", ""));
        Assert.assertEquals("Full Path (*nix)", "d.txt", new Paths().baseName("/a/b/c/d.txt", ""));
        Assert.assertEquals("Full Path (Windows)", "d.txt", new Paths().baseName("c:\\a\\b\\c\\d.txt", ""));
        Assert.assertEquals("No Extension", "d", new Paths().baseName("c:\\a\\b\\c\\d", ""));
        Assert.assertEquals("Just File", "d.txt", new Paths().baseName("d.txt", ""));
        Assert.assertEquals("Just File and Slash", "d.txt", new Paths().baseName("/d.txt", ""));
        Assert.assertEquals("Relative Path", "d.txt", new Paths().baseName("a/b/c/d.txt", ""));
        Assert.assertEquals("With Navigation", "d.txt", new Paths().baseName("\\a\\b\\..\\c\\d.txt", ""));
    }

    /**
     * Unit test {@link Paths#baseName(String, String)}
     */
    @Test
    public void test_baseName_removeExtension() {
        Assert.assertEquals("empty", "", new Paths().baseName("", ".txt"));
        Assert.assertEquals("Full Path (*nix)", "d", new Paths().baseName("/a/b/c/d.txt", ".txt"));
        Assert.assertEquals("Full Path (Windows)", "d", new Paths().baseName("c:\\a\\b\\c\\d.txt", ".txt"));
        Assert.assertEquals("No Extension", "d", new Paths().baseName("c:\\a\\b\\c\\d", ".txt"));
        Assert.assertEquals("Just File", "d", new Paths().baseName("d.txt", ".txt"));
        Assert.assertEquals("Just File and Slash", "d", new Paths().baseName("/d.txt", ".txt"));
        Assert.assertEquals("Relative Path", "d", new Paths().baseName("a/b/c/d.txt", ".txt"));
        Assert.assertEquals("With Navigation", "d", new Paths().baseName("\\a\\b\\..\\c\\d.txt", ".txt"));
    }

    /**
     * Unit test {@link Paths#extension(String, int)}
     */
    @Test
    public void test_extension() {
        Assert.assertEquals("empty", "", new Paths().extension("", -1));
        Assert.assertEquals("Full Path (*nix)", ".txt", new Paths().extension("/a/b/c/d.txt", -1));
        Assert.assertEquals("Full Path (Windows)", ".txt", new Paths().extension("c:\\a\\b\\c\\d.txt", -1));
        Assert.assertEquals("No Extension", "", new Paths().extension("c:\\a\\b\\c\\d", -1));
        Assert.assertEquals("Just File", ".txt", new Paths().extension("d.txt", -1));
        Assert.assertEquals("Just File and Slash", ".txt", new Paths().extension("/d.txt", -1));
        Assert.assertEquals("Relative Path", ".txt", new Paths().extension("a/b/c/d.txt", -1));
        Assert.assertEquals("With Navigation", ".txt", new Paths().extension("\\a\\b\\..\\c\\d.txt", -1));

        Assert.assertEquals("No File", ".txt", new Paths().extension(".txt", -1));
        Assert.assertEquals("Missing Extension", ".", new Paths().extension("d.", -1));

        Assert.assertEquals("multiple extensions (all)", ".tar.gz", new Paths().extension("archive.tar.gz", -1));
        Assert.assertEquals("multiple extensions (zero)", "", new Paths().extension("archive.tar.gz", 0));
        Assert.assertEquals("multiple extensions (one)", ".gz", new Paths().extension("archive.tar.gz", 1));
        Assert.assertEquals("multiple extensions (two)", ".tar.gz", new Paths().extension("archive.tar.gz", 2));
        Assert.assertEquals("multiple extensions (three)", ".tar.gz", new Paths().extension("archive.tar.gz", 3));

        Assert.assertEquals("No File, multiple extensions (all)", ".tar.gz", new Paths().extension(".tar.gz", -1));
        Assert.assertEquals("No File, multiple extensions (zero)", "", new Paths().extension(".tar.gz", 0));
        Assert.assertEquals("No File, multiple extensions (one)", ".gz", new Paths().extension(".tar.gz", 1));
        Assert.assertEquals("No File, multiple extensions (two)", ".tar.gz", new Paths().extension(".tar.gz", 2));
        Assert.assertEquals("No File, multiple extensions (three)", ".tar.gz", new Paths().extension(".tar.gz", 3));
    }
}