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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit test for {@link FileUtils}
 */
public class FileUtilsTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File root;
    private File text;
    private File unknown;
    private File target;


    @Before
    public void setUp() throws Exception {
        root = tempFolder.getRoot();
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
        Assert.assertFalse("Read directory", FileUtils.canRead(root));
        Assert.assertFalse("missing", FileUtils.canRead(unknown));
        Assert.assertTrue("text file", FileUtils.canRead(text));
    }


    /**
     * Unit test {@link FileUtils#read(File)} 
     */
    @Test
    public void test_read_happyPath() {
        List<String> actual = FileUtils.read(text);

        Assert.assertEquals("Unexpected text", List.of("Hello", "World", "3"), actual);
    }

    /**
     * Unit test {@link FileUtils#read(File)}
     */
    @Test
    public void test_read_missingFile() {
        Exception actual =
            Assert.assertThrows(EelDocException.class, () -> FileUtils.read(unknown));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().matches("^Failed to read file .*unknown.file!$"));
    }


    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write() throws Exception {
        FileUtils.write(target, "Some Text", StandardCharsets.UTF_8);

        Assert.assertEquals("Unexpected content", "Some Text", Files.readString(target.toPath()));
    }


    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write_badDirectory() {
        File bad = new File(text, "invalid.path");                       // path through a file

        Exception actual = Assert.assertThrows(EelDocException.class,
            () -> FileUtils.write(bad, "Some Text", StandardCharsets.UTF_8));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Failed to create directory"));
    }

    /**
     * Unit test {@link FileUtils#write(File, String, Charset)}
     */
    @Test
    public void test_write_badFile() throws Exception {
        File bad = new File(root, "subDir");

        tempFolder.newFolder("subDir");

        Exception actual = Assert.assertThrows(EelDocException.class,
            () -> FileUtils.write(bad, "Some Text", StandardCharsets.UTF_8));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Failed to write file"));
    }


    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource() throws Exception {
        File target = new File(root, "text.txt");

        FileUtils.copyResource("file.txt", target);

        Assert.assertEquals("Unexpected content", "Some data", Files.readString(text.toPath()));
    }

    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource_missingFile() {
        File target = new File(root, "text.txt");

        Exception actual = Assert.assertThrows(EelDocException.class,
            () -> FileUtils.copyResource("unknown.file!", target));

        Assert.assertEquals("Unexpected message", "Missing resource 'unknown.file!'", actual.getMessage());
    }

    /**
     * Unit test {@link FileUtils#copyResource(String, File)}
     */
    @Test
    public void test_copyResource_badTarget() {
        Exception cause = new IOException("expected");
        File target = new File(root, "subDir");

        try (
            MockedStatic<Files> files = mockStatic(Files.class)
        ) {
            files.when(() -> Files.copy(any(InputStream.class), any(Path.class), any()))
                .thenThrow(cause);

            Exception actual = Assert.assertThrows(EelDocException.class,
                () -> FileUtils.copyResource("file.txt", target));

            Assert.assertTrue("Unexpected message: " + actual.getMessage(),
                actual.getMessage().matches("Failed to copy file 'file.txt' to '.*subDir'"));
        }
    }
}