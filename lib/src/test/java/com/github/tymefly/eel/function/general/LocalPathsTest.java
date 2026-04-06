package com.github.tymefly.eel.function.general;

import java.nio.file.FileSystems;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.EelFunctionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link LocalPaths}
 */
public class LocalPathsTest {
    /**
     * Unit test {@link LocalPaths#realPath(EelContext, String)}
     */
    @Test
    public void test_realPath() throws Exception {
        EelContext context = EelContext.factory().build();

        String currentDir = System.getProperty("user.dir");
        String slash = FileSystems.getDefault().getSeparator();

        assertEquals(currentDir, new LocalPaths().realPath(context, "."), "current dir");
        assertEquals(currentDir + slash + "child", new LocalPaths().realPath(context, "child"), "child dir");
        assertEquals(currentDir + slash + "read.me", new LocalPaths().realPath(context, "read.me"), "file in dir");
        assertEquals(currentDir, new LocalPaths().realPath(context, "./."), "remove current dir");
        assertEquals(currentDir + slash + "b", new LocalPaths().realPath(context, "a/../b"), "parent dir");
        assertEquals("/a/c",
            new LocalPaths().realPath(context, "/a/b/../c/.").replaceFirst("^[A-Za-z]:", "").replace('\\', '/'),
            "absolute dir");

        assertEquals("/a/c",
            new LocalPaths().realPath(context, "/a/b/../c/").replaceFirst("^[A-Za-z]:", "").replace('\\', '/'),
            "trailing slash");
        assertEquals("/a/c",
            new LocalPaths().realPath(context, "/a/b/../c////").replaceFirst("^[A-Za-z]:", "").replace('\\', '/'),
            "multiple trailing slashes");

        assertThrows(EelFunctionException.class, () -> new LocalPaths().realPath(context, ""), "Empty String");
    }


    /**
     * Unit test {@link LocalPaths#dirName(String)}
     */
    @Test
    public void test_dirName() {
        assertEquals("", new LocalPaths('/').dirName(""), "empty");
        assertEquals("/a/b/c", new LocalPaths('/').dirName("/a/b/c/d.txt"), "Full Path (*nix)");
        assertEquals("c:\\a\\b\\c", new LocalPaths('\\').dirName("c:\\a\\b\\c\\d.txt"), "Full Path (Windows)");
        assertEquals("c:\\a\\b\\c", new LocalPaths('\\').dirName("c:\\a\\b\\c\\d"), "No Extension");
        assertEquals("", new LocalPaths('/').dirName("d.txt"), "Just File");
        assertEquals("/", new LocalPaths('/').dirName("/d.txt"), "Just Slash and File");
        assertEquals("a/b/c", new LocalPaths('/').dirName("a/b/c/d.txt"), "Relative Path");
        assertEquals("/a/b/../c", new LocalPaths('/').dirName("/a/b/../c/d.txt"), "With Navigation");
        assertEquals("/a/b/c", new LocalPaths('/').dirName("/a/b/c/d/"), "ends with a slash");
        assertEquals("/a/b/c", new LocalPaths('/').dirName("/a/b/c/d//"), "ends with multiple slashes");
    }


    /**
     * Unit test {@link LocalPaths#baseName(String, String)}
     */
    @Test
    public void test_baseName_withExtension() {
        assertEquals("", new LocalPaths('/').baseName("", ""), "empty");
        assertEquals("d.txt", new LocalPaths('/').baseName("/a/b/c/d.txt", ""), "Full Path (*nix)");
        assertEquals("d.txt", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d.txt", ""), "Full Path (Windows)");
        assertEquals("d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d", ""), "No Extension");
        assertEquals("d.txt", new LocalPaths('/').baseName("d.txt", ""), "Just File");
        assertEquals("d.txt", new LocalPaths('/').baseName("/d.txt", ""), "Just Slash and File");
        assertEquals("d.txt", new LocalPaths('/').baseName("a/b/c/d.txt", ""), "Relative Path");
        assertEquals("d.txt", new LocalPaths('\\').baseName("\\a\\b\\..\\c\\d.txt", ""), "With Navigation");
        assertEquals("d.txt", new LocalPaths('\\').baseName("\\a\\b\\c\\d.txt\\", ""), "ends with slash");
        assertEquals("d.txt", new LocalPaths('\\').baseName("\\a\\b\\c\\d.txt\\\\", ""), "ends with multiple slashes");
    }

    /**
     * Unit test {@link LocalPaths#baseName(String, String)}
     */
    @Test
    public void test_baseName_removeExtension() {
        assertEquals("",  new LocalPaths('/').baseName("", ".txt"), "empty");
        assertEquals("d", new LocalPaths('/').baseName("/a/b/c/d.txt", ".txt"), "Full Path (*nix)");
        assertEquals("d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d.txt", ".txt"), "Full Path (Windows)");
        assertEquals("d", new LocalPaths('\\').baseName("c:\\a\\b\\c\\d", ".txt"), "No Extension");
        assertEquals("d", new LocalPaths('/').baseName("d.txt", ".txt"), "Just File");
        assertEquals("d", new LocalPaths('/').baseName("/d.txt", ".txt"), "Just Slash and File");
        assertEquals("d", new LocalPaths('/').baseName("a/b/c/d.txt", ".txt"), "Relative Path");
        assertEquals("d", new LocalPaths('\\').baseName("\\a\\b\\..\\c\\d.txt", ".txt"), "With Navigation");
        assertEquals("d", new LocalPaths('\\').baseName("\\a\\b\\c\\d.txt\\", ".txt"), "ends with slash");
        assertEquals("d", new LocalPaths('\\').baseName("\\a\\b\\c\\d.txt\\\\", ".txt"), "ends with multiple slashes");
    }

    /**
     * Unit test {@link LocalPaths#extension(String, int)}
     */
    @Test
    public void test_extension() throws Exception {
        assertEquals("", new LocalPaths().extension("", -1), "empty");

        assertEquals("txt", new LocalPaths('/').extension("/a/b/c/d.txt", -1), "Full Path (*nix)");
        assertEquals("txt", new LocalPaths('\\').extension("c:\\a\\b\\c\\d.txt", -1), "Full Path (Windows)");
        assertEquals("", new LocalPaths('\\').extension("c:\\a\\b\\c\\d", -1), "No Extension");
        assertEquals("txt", new LocalPaths('/').extension("d.txt", -1), "Just File");
        assertEquals("txt", new LocalPaths().extension("/d.txt", -1), "Just Slash and File");
        assertEquals("txt", new LocalPaths('/').extension("a/b/c/d.txt", -1), "Relative Path");
        assertEquals("txt", new LocalPaths('\\').extension("\\a\\b\\..\\c\\d.txt", -1), "With Navigation");

        assertEquals("txt", new LocalPaths('/').extension(".txt", -1), "No File");
        assertEquals("", new LocalPaths('/').extension("d.", -1), "Missing Extension");

        assertEquals("tar.gz", new LocalPaths('/').extension("archive.tar.gz", -1), "multiple extensions (all)");
        assertEquals("", new LocalPaths('/').extension("archive.tar.gz", 0), "multiple extensions (zero)");
        assertEquals("gz", new LocalPaths('/').extension("archive.tar.gz", 1), "multiple extensions (one)");
        assertEquals("tar.gz", new LocalPaths('/').extension("archive.tar.gz", 2), "multiple extensions (two)");
        assertEquals("tar.gz", new LocalPaths('/').extension("archive.tar.gz", 3), "multiple extensions (three)");

        assertEquals("tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", -1), "trailing extensions (all)");
        assertEquals("", new LocalPaths('/').extension("archive.tar.gz.", 0), "trailing extensions (zero)");
        assertEquals("", new LocalPaths('/').extension("archive.tar.gz.", 1), "trailing extensions (one)");
        assertEquals("gz.", new LocalPaths('/').extension("archive.tar.gz.", 2), "trailing extensions (two)");
        assertEquals("tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", 3), "trailing extensions (three)");
        assertEquals("tar.gz.", new LocalPaths('/').extension("archive.tar.gz.", 4), "trailing extensions (four)");

        assertEquals("tar.gz", new LocalPaths('/').extension(".tar.gz", -1), "No File, multiple extensions (all)");
        assertEquals("", new LocalPaths('/').extension(".tar.gz", 0), "No File, multiple extensions (zero)");
        assertEquals("gz", new LocalPaths('/').extension(".tar.gz", 1), "No File, multiple extensions (one)");
        assertEquals("tar.gz", new LocalPaths('/').extension(".tar.gz", 2), "No File, multiple extensions (two)");
        assertEquals("tar.gz", new LocalPaths('/').extension(".tar.gz", 3), "No File, multiple extensions (three)");
    }
}