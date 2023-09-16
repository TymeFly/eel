package com.github.tymefly.eel.function.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link LocalPaths}
 */
public class LocalPathsTest {
    /**
     * Unit test {@link LocalPaths#realPath(String)}
     */
    @Test
    public void test_realPath() throws Exception {
        String currentDir = System.getProperty("user.dir");
        String slash = System.getProperty("file.separator");

        Assert.assertEquals("current dir", currentDir, new LocalPaths().realPath("."));
        Assert.assertEquals("child dir", currentDir + slash + "child", new LocalPaths().realPath("child"));
        Assert.assertEquals("file in dir", currentDir + slash + "read.me", new LocalPaths().realPath("read.me"));
        Assert.assertEquals("remove current dir", currentDir, new LocalPaths().realPath("./."));
        Assert.assertEquals("parent dir", currentDir + slash + "b", new LocalPaths().realPath("a/../b"));
        Assert.assertEquals("absolute dir",
            "/a/c",
            new LocalPaths().realPath("/a/b/../c/.").replaceFirst("^[A-Za-z]:", "").replace('\\', '/'));

    }


    /**
     * Unit test {@link LocalPaths#dirName(String)}
     */
    @Test
    public void test_dirName() {
        Assert.assertEquals("empty", new LocalPaths('/').dirName(""), "");
        Assert.assertEquals("Full Path (*nix)", new LocalPaths('/').dirName("/a/b/c/d.txt"), "/a/b/c");
        Assert.assertEquals("Full Path (Windows)", new LocalPaths('\\').dirName("c:\\a\\b\\c\\d.txt"), "c:\\a\\b\\c");
        Assert.assertEquals("No Extension", new LocalPaths('\\').dirName("c:\\a\\b\\c\\d"), "c:\\a\\b\\c");
        Assert.assertEquals("Just File", new LocalPaths('/').dirName("d.txt"), "");
        Assert.assertEquals("Just Slash and File", new LocalPaths('/').dirName("/d.txt"), "");
        Assert.assertEquals("Relative Path", new LocalPaths('/').dirName("a/b/c/d.txt"), "a/b/c");
        Assert.assertEquals("With Navigation", new LocalPaths('/').dirName("/a/b/../c/d.txt"), "/a/b/../c");
    }


    /**
     * Unit test {@link LocalPaths#baseName(String, String)}
     */
    @Test
    public void test_baseName_withExtension() {
        Assert.assertEquals("empty", "", new LocalPaths('/').baseName("", ""));
        Assert.assertEquals("Full Path (*nix)", "d.txt", new LocalPaths('/').baseName("/a/b/c/d.txt", ""));
        Assert.assertEquals("Full Path (Windows)", "d.txt", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d.txt", ""));
        Assert.assertEquals("No Extension", "d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d", ""));
        Assert.assertEquals("Just File", "d.txt", new LocalPaths('/').baseName("d.txt", ""));
        Assert.assertEquals("Just Slash and File", "d.txt", new LocalPaths('/').baseName("/d.txt", ""));
        Assert.assertEquals("Relative Path", "d.txt", new LocalPaths('/').baseName("a/b/c/d.txt", ""));
        Assert.assertEquals("With Navigation", "d.txt", new LocalPaths('\\').baseName("\\a\\b\\..\\c\\d.txt", ""));
    }

    /**
     * Unit test {@link LocalPaths#baseName(String, String)}
     */
    @Test
    public void test_baseName_removeExtension() {
        Assert.assertEquals("empty", "", new LocalPaths('/').baseName("", ".txt"));
        Assert.assertEquals("Full Path (*nix)", "d", new LocalPaths('/').baseName("/a/b/c/d.txt", ".txt"));
        Assert.assertEquals("Full Path (Windows)", "d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d.txt", ".txt"));
        Assert.assertEquals("No Extension", "d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d", ".txt"));
        Assert.assertEquals("Just File", "d", new LocalPaths('/').baseName("d.txt", ".txt"));
        Assert.assertEquals("Just Slash and File", "d", new LocalPaths('/').baseName("/d.txt", ".txt"));
        Assert.assertEquals("Relative Path", "d", new LocalPaths('/').baseName("a/b/c/d.txt", ".txt"));
        Assert.assertEquals("With Navigation", "d", new LocalPaths('\\').baseName("\\a\\b\\..\\c\\d.txt", ".txt"));
    }

    /**
     * Unit test {@link LocalPaths#extension(String, int)}
     */
    @Test
    public void test_extension() {
        Assert.assertEquals("empty", "", new LocalPaths().extension("", -1));
        Assert.assertEquals("Full Path (*nix)", "txt", new LocalPaths('/').extension("/a/b/c/d.txt", -1));
        Assert.assertEquals("Full Path (Windows)", "txt", new LocalPaths('\\').extension("c:\\a\\b\\c\\d.txt", -1));
        Assert.assertEquals("No Extension", "", new LocalPaths('\\').extension("c:\\a\\b\\c\\d", -1));
        Assert.assertEquals("Just File", "txt", new LocalPaths('/').extension("d.txt", -1));
        Assert.assertEquals("Just Slash and File", "txt", new LocalPaths().extension("/d.txt", -1));
        Assert.assertEquals("Relative Path", "txt", new LocalPaths('/').extension("a/b/c/d.txt", -1));
        Assert.assertEquals("With Navigation", "txt", new LocalPaths('\\').extension("\\a\\b\\..\\c\\d.txt", -1));

        Assert.assertEquals("No File", "txt", new LocalPaths('/').extension(".txt", -1));
        Assert.assertEquals("Missing Extension", "", new LocalPaths('/').extension("d.", -1));

        Assert.assertEquals("multiple extensions (all)", "tar.gz", new LocalPaths('/').extension("archive.tar.gz", -1));
        Assert.assertEquals("multiple extensions (zero)", "", new LocalPaths('/').extension("archive.tar.gz", 0));
        Assert.assertEquals("multiple extensions (one)", "gz", new LocalPaths('/').extension("archive.tar.gz", 1));
        Assert.assertEquals("multiple extensions (two)", "tar.gz", new LocalPaths('/').extension("archive.tar.gz", 2));
        Assert.assertEquals("multiple extensions (three)", "tar.gz", new LocalPaths('/').extension("archive.tar.gz", 3));

        Assert.assertEquals("trailing extensions (all)", "tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", -1));
        Assert.assertEquals("trailing extensions (zero)", "", new LocalPaths('/').extension("archive.tar.gz.", 0));
        Assert.assertEquals("trailing extensions (one)", "", new LocalPaths('/').extension("archive.tar.gz.", 1));
        Assert.assertEquals("trailing extensions (two)", "gz.", new LocalPaths('/').extension("archive.tar.gz.", 2));
        Assert.assertEquals("trailing extensions (three)", "tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", 3));
        Assert.assertEquals("trailing extensions (four)", "tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", 4));

        Assert.assertEquals("No File, multiple extensions (all)", "tar.gz", new LocalPaths('/').extension(".tar.gz", -1));
        Assert.assertEquals("No File, multiple extensions (zero)", "", new LocalPaths('/').extension(".tar.gz", 0));
        Assert.assertEquals("No File, multiple extensions (one)", "gz", new LocalPaths('/').extension(".tar.gz", 1));
        Assert.assertEquals("No File, multiple extensions (two)", "tar.gz", new LocalPaths('/').extension(".tar.gz", 2));
        Assert.assertEquals("No File, multiple extensions (three)", "tar.gz", new LocalPaths('/').extension(".tar.gz", 3));
    }
}