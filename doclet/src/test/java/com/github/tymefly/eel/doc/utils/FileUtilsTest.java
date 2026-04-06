package com.github.tymefly.eel.doc.utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.github.tymefly.eel.doc.exception.EelDocException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit test for {@link FileUtils}
 */
public class FileUtilsTest {

    @TempDir
    File root;

    private File text;
    private File unknown;
    private File target;

    @BeforeEach
    public void setUp() throws Exception {
        text = new File(root, "text.txt");
        unknown = new File(root, "unknown.file!");
        target = new File(root, "path/to/my/file.txt");

        Files.writeString(text.toPath(), "Hello\nWorld\n$(1 + 2)");
    }

    /**
     * Unit test {@link FileUtils#canRead(File)}
     */
    @Test
    public void test_canRead() {
        assertFalse(FileUtils.canRead(root), "Read directory");
        assertFalse(FileUtils.canRead(unknown), "missing");
        assertTrue(FileUtils.canRead(text), "text file");
    }

    /**
     * Unit test {@link FileUtils#read(File)}
     */
    @Test
    public void test_read_happyPath() {
        List<String> actual = FileUtils.read(text);

        assertEquals(List.of("Hello", "World", "3"), actual, "Unexpected text");
    }

    /**
     * Unit test {@link FileUtils#read(File)}
     */
    @Test
    public void test_read_missingFile() {
        EelDocException actual = assertThrows(EelDocException.class, () -> FileUtils.read(unknown));

        assertTrue(actual.getMessage().matches("^Failed to read file .*unknown.file!$"),
            "Unexpected message: " + actual.getMessage());
    }

    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write() throws Exception {
        FileUtils.write(target, "Some Text", StandardCharsets.UTF_8);

        assertEquals("Some Text", Files.readString(target.toPath()), "Unexpected content");
    }

    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write_badDirectory() {
        File bad = new File(text, "invalid.path"); // path through a file

        EelDocException actual = assertThrows(EelDocException.class,
            () -> FileUtils.write(bad, "Some Text", StandardCharsets.UTF_8));

        assertTrue(actual.getMessage().startsWith("Failed to create directory"),
            "Unexpected message: " + actual.getMessage());
    }

    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write_badFile() throws Exception {
        File bad = new File(root, "subDir");
        Files.createDirectory(bad.toPath());

        EelDocException actual = assertThrows(EelDocException.class,
            () -> FileUtils.write(bad, "Some Text", StandardCharsets.UTF_8));

        assertTrue(actual.getMessage().startsWith("Failed to write file"),
            "Unexpected message: " + actual.getMessage());
    }

    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource() throws Exception {
        File target = new File(root, "text.txt");

        FileUtils.copyResource("file.txt", target);

        assertEquals("Some data", Files.readString(text.toPath()), "Unexpected content");
    }

    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource_missingFile() {
        File target = new File(root, "text.txt");

        EelDocException actual = assertThrows(EelDocException.class,
            () -> FileUtils.copyResource("unknown.file!", target));

        assertEquals("Missing resource 'unknown.file!'", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource_badTarget() {
        IOException cause = new IOException("expected");
        File target = new File(root, "subDir");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.copy(any(InputStream.class), any(Path.class), any()))
                 .thenThrow(cause);

            EelDocException actual = assertThrows(EelDocException.class,
                () -> FileUtils.copyResource("file.txt", target));

            assertTrue(actual.getMessage().matches("Failed to copy file 'file.txt' to '.*subDir'"),
                "Unexpected message: " + actual.getMessage());
        }
    }
}
