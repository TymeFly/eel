package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link SecureFileFactory}
 */
public class SecureFileFactoryTest {
    private static final String HOME = System.getProperty("user.home");
    private static final String WIN_DIR = System.getenv("windir");


    private SecureFileFactory standard;

    @BeforeEach
    void setUp() {
        standard = SecureFileFactory.standard();
    }

    /**
     * Unit test {@link SecureFileFactory#build(String)}
     */
    @Test
    public void test_build_HappyPath_path() throws Exception {
        File actual = standard.build("/a/b/c.d");
        String actualPath = actual.getAbsolutePath();

        assertTrue(actualPath.matches("([A-Z]:)?[\\\\/]a[\\\\/]b[\\\\/]c.d"), "Unexpected path: " + actualPath);
    }

    /**
     * Unit test {@link SecureFileFactory#build(String)}
     */
    @Test
    public void test_build_invalidPath() {
        Exception actual = assertThrows(IOException.class, () -> standard.build("Hello\u0000World?"), "Invalid path");

        assertTrue(actual.getMessage().startsWith("Can not read path"), "Unexpected message: " + actual.getMessage());
    }

    /**
     * Unit test {@link SecureFileFactory#build(String)}
     */
    @Test
    public void test_build_emptyPath() {
        Exception actual = assertThrows(IOException.class, () -> standard.build(""), "Invalid path");

        assertEquals("Empty path", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link SecureFileFactory#build(String)}
     */
    @Test
    public void test_build_custom() throws Exception {
        FileFactory factory = path -> new File(path + "!");
        SecureFileFactory fileFactory = SecureFileFactory.custom(factory);

        File actual = fileFactory.build("/a/b/c.d");
        String actualPath = actual.getAbsolutePath();

        assertTrue(actualPath.matches("([A-Z]:)?[\\\\/]a[\\\\/]b[\\\\/]c.d!"), "Unexpected path: " + actualPath);
    }


    /**
     * Unit test {@link SecureFileFactory#build(String)}
     */
    @Test
    public void test_from_blackList() {
        assertThrows(IOException.class,
            () -> {                                         // One of these should fail, but we can't say which
                standard.build("/proc");
                standard.build(WIN_DIR);
            },
            "Blacklisted");
    }


    /**
     * Unit test {@link SecureFileFactory#onBlackList(SecureFileFactory.Rules, String)}
     */
    @Test
    public void test_onBlackList_nix() {
        assertTrue(standard.onBlackList(SecureFileFactory.Rules.NIX, "/proc/"), "Sensitive dir");
        assertTrue(standard.onBlackList(SecureFileFactory.Rules.NIX, "/proc/swaps"), "Sensitive file");

        assertFalse(standard.onBlackList(SecureFileFactory.Rules.NIX, "/home/"), "Home dir");
        assertFalse(standard.onBlackList(SecureFileFactory.Rules.NIX, "/home/my.txt"), "Home file");
    }

    // We can't test the blacklisted locations for Windows on *nix systems as Windows specific var are not set.


    /**
     * Unit test {@link SecureFileFactory#onWhiteList(SecureFileFactory.Rules, String)}
     */
    @Test
    public void test_onWhiteList_nix() {
        assertTrue(standard.onWhiteList(SecureFileFactory.Rules.NIX, HOME + "/"), "Home dir");
        assertTrue(standard.onWhiteList(SecureFileFactory.Rules.NIX, HOME + "/hello.world"), "Home file");

        assertFalse(standard.onWhiteList(SecureFileFactory.Rules.NIX, "/Other/"), "Unknown dir");
        assertFalse(standard.onWhiteList(SecureFileFactory.Rules.NIX, "/Other/hello.world"), "Unknown file");
    }

    /**
     * Unit test {@link SecureFileFactory#onWhiteList(SecureFileFactory.Rules, String)}
     */
    @Test
    public void test_onWhiteList_win() {
        assertTrue(standard.onWhiteList(SecureFileFactory.Rules.WINDOWS, HOME.toLowerCase() + "\\"), "Home dir");
        assertTrue(standard.onWhiteList(SecureFileFactory.Rules.WINDOWS, HOME.toLowerCase() + "\\hello.world"), "Home file");

        assertFalse(standard.onWhiteList(SecureFileFactory.Rules.WINDOWS, "C:\\Other\\"), "Unknown dir");
        assertFalse(standard.onWhiteList(SecureFileFactory.Rules.WINDOWS, "C:\\Other\\hello.world"), "Unknown file");
    }

}