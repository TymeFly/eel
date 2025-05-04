package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link FileFactory}
 */
public class FileFactoryTest {
    private static final String HOME = System.getProperty("user.home");
    private static final String WIN_DIR = System.getenv("windir");


    /**
     * Unit test {@link FileFactory#from(String)}
     */
    @Test
    public void test_from_HappyPath() throws Exception {
        File actual = FileFactory.from("/a/b/c.d");
        String actualPath = actual.getAbsolutePath();

        Assert.assertTrue("Unexpected path: " + actualPath,
            actualPath.matches("([A-Z]:)?[\\\\/]a[\\\\/]b[\\\\/]c.d"));
    }

    @Test
    public void test_from_invalidPath() {
        Assert.assertThrows("Invalid path", IOException.class, () -> FileFactory.from("Hello\u0000World?"));
    }

    @Test
    public void test_from_blackList() {
        Assert.assertThrows("Blacklisted",
            IOException.class,
            () -> {                                         // One of these should fail, but we can't say which
                FileFactory.from("/proc");
                FileFactory.from(WIN_DIR);
            });
    }


    /**
     * Unit test {@link FileFactory#onBlackList(FileFactory.Rules, String)}
     */
    @Test
    public void test_onBlackList_nix() {
        Assert.assertTrue("Sensitive dir", FileFactory.onBlackList(FileFactory.Rules.NIX, "/proc/"));
        Assert.assertTrue("Sensitive file", FileFactory.onBlackList(FileFactory.Rules.NIX, "/proc/swaps"));

        Assert.assertFalse("Home dir", FileFactory.onBlackList(FileFactory.Rules.NIX, "/home/"));
        Assert.assertFalse("Home file", FileFactory.onBlackList(FileFactory.Rules.NIX, "/home/my.txt"));
    }

    // We can't test the blacklisted locations for Windows on *nix systems as Windows specific var are not set.


    /**
     * Unit test {@link FileFactory#onWhiteList(FileFactory.Rules, String)}
     */
    @Test
    public void test_onWhiteList_nix() {
        Assert.assertTrue("Home dir", FileFactory.onWhiteList(FileFactory.Rules.NIX, HOME + "/"));
        Assert.assertTrue("Home file", FileFactory.onWhiteList(FileFactory.Rules.NIX, HOME + "/hello.world"));

        Assert.assertFalse("Unknown dir", FileFactory.onWhiteList(FileFactory.Rules.NIX, "/Other/"));
        Assert.assertFalse("Unknown file", FileFactory.onWhiteList(FileFactory.Rules.NIX, "/Other/hello.world"));
    }

    /**
     * Unit test {@link FileFactory#onWhiteList(FileFactory.Rules, String)}
     */
    @Test
    public void test_onWhiteList_win() {
        Assert.assertTrue("Home dir", FileFactory.onWhiteList(FileFactory.Rules.WINDOWS, HOME.toLowerCase() + "\\"));
        Assert.assertTrue("Home file", FileFactory.onWhiteList(FileFactory.Rules.WINDOWS, HOME.toLowerCase() + "\\hello.world"));

        Assert.assertFalse("Unknown dir", FileFactory.onWhiteList(FileFactory.Rules.WINDOWS, "C:\\Other\\"));
        Assert.assertFalse("Unknown file", FileFactory.onWhiteList(FileFactory.Rules.WINDOWS, "C:\\Other\\hello.world"));
    }
}