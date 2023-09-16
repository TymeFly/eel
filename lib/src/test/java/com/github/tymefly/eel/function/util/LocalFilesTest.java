package com.github.tymefly.eel.function.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link LocalFiles}
 */
public class LocalFilesTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.parse("2000-01-01T12:13:14Z");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");


    private final Map<String, BasicFileAttributes> mockAttributes = new HashMap<>();
    private BasicFileAttributes attributes;
    private String tempFolderPath;


    @Before
    public void setUp() throws Exception {
        attributes = mock(BasicFileAttributes.class);

        when(attributes.creationTime())
            .thenReturn(FileTime.from(946684800, TimeUnit.SECONDS));
        when(attributes.lastAccessTime())
            .thenReturn(FileTime.from(981075661, TimeUnit.SECONDS));
        when(attributes.lastModifiedTime())
            .thenReturn(FileTime.from(1015120922, TimeUnit.SECONDS));
        when(attributes.size())
            .thenReturn(0x12345L);

        tempFolderPath = tempFolder.getRoot().getAbsolutePath() + FILE_SEPARATOR;

        tempFolder.newFolder("sub");
        tempFolder.newFolder("sub/dir");
        tempFolder.newFile("sub/file.txt");           // these should be ignored
        tempFolder.newFile("sub/file1.txt");
        tempFolder.newFile("sub/file3.dat");
    }


    @Nonnull
    private Stream<Path> mockDirectory(@Nonnull MockedStatic<Files> files) {
        return Stream.of(
            mockFile(files, "1.txt", FileTime.from(1000000000, TimeUnit.SECONDS)),      // oldest
            mockFile(files, "2.txt", FileTime.from(1000010000, TimeUnit.SECONDS)),
            mockFile(files, "sub",   FileTime.from(1000020000, TimeUnit.SECONDS)),      // dir should be ignored
            mockFile(files, "2.jpg", FileTime.from(1000030000, TimeUnit.SECONDS))       // newest
        );
    }

    @Nonnull
    private Path mockFile(@Nonnull MockedStatic<Files> files, @Nonnull String fileName, @Nonnull FileTime time) {
        Path path = Paths.get(tempFolderPath + fileName);
        BasicFileAttributes attributes = mock(BasicFileAttributes.class);

        when(attributes.creationTime())
            .thenReturn(time);
        when(attributes.lastAccessTime())
            .thenReturn(time);
        when(attributes.lastModifiedTime())
            .thenReturn(time);

        files.when(() -> Files.readAttributes(eq(path), eq(BasicFileAttributes.class)))
            .thenReturn(attributes);

        try {
            tempFolder.newFile(fileName);
        } catch (Exception e) {
            // No need to do anything - the test will fail if we can't create the backing files
        }

        mockAttributes.put(fileName, attributes);

        return path;
    }

    /**
     * Unit test {@link LocalFiles#exists(String)}
     */
    @Test
    public void test_exists() throws Exception {
        Assert.assertTrue("Expected POM to exist", new LocalFiles().exists("pom.xml"));
        Assert.assertFalse("Expected unknown not to exist", new LocalFiles().exists("unknown.file"));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(String, String)}
     */
    @Test
    public void test_fileCount() throws Exception {
        LocalFiles localFiles = new LocalFiles();

        Assert.assertEquals("All Files", 3, localFiles.fileCount(tempFolderPath + "sub", "*"));
        Assert.assertEquals("Text File", 2, localFiles.fileCount(tempFolderPath + "sub", "*.txt"));
        Assert.assertEquals("Dat File", 1, localFiles.fileCount(tempFolderPath + "sub", "*.dat"));
        Assert.assertEquals("No Match", 0, localFiles.fileCount(tempFolderPath + "sub", "*.unknown"));
        Assert.assertEquals("Empty Dir", 0, localFiles.fileCount(tempFolderPath + "sub/dir", "*"));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(String, String)}
     */
    @Test
    public void test_fileCount_noDirectory() {
        Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(tempFolderPath + "unknown/dir", "*"));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Can not read directory "));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(String, String)}
     */
    @Test
    public void test_fileCount_notDirectory() {
        Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(tempFolderPath + "sub/file.txt", "*"));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Can not read directory "));
    }


    /**
     * Unit test {@link LocalFiles#createAt(String, ZonedDateTime)}
     */
    @Test
    public void test_createAt() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .createAt("pom.xml", DEFAULT_DATE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected create time", Instant.parse("2000-01-01T00:00:00Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#createAt(String, ZonedDateTime)}
     */
    @Test
    public void test_createAt_missingFile() throws Exception {
        ZonedDateTime actual = new LocalFiles()
            .createAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }

    /**
     * Unit test {@link LocalFiles#createAt(String, ZonedDateTime)}
     */
    @Test
    public void test_createAt_directory() {
        Assert.assertThrows(IOException.class,
            () -> new LocalFiles().createAt(tempFolderPath + "sub", DEFAULT_DATE));
    }


    /**
     * Unit test {@link LocalFiles#accessedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_accessedAt() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .accessedAt("pom.xml", DEFAULT_DATE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected accessed time", Instant.parse("2001-02-02T01:01:01Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#accessedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_accessedAt_missingFile() throws Exception {
        ZonedDateTime actual = new LocalFiles()
            .accessedAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#modifiedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_modifiedAt() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .modifiedAt("pom.xml", DEFAULT_DATE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected modified time", Instant.parse("2002-03-03T02:02:02Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#modifiedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_modifiedAt_missingFile() throws Exception {
        ZonedDateTime actual = new LocalFiles()
            .modifiedAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#fileSize(String, long)}
     */
    @Test
    public void test_fileSize() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            long actual = new LocalFiles()
                .fileSize("pom.xml", -1);

            Assert.assertEquals("Unexpected modified time", 74565, actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#fileSize(String, long)}
     */
    @Test
    public void test_fileSize_missingFile() throws Exception {
        long actual = new LocalFiles()
            .fileSize("unknown.file", -999);

        Assert.assertEquals("Unexpected create time", -999, actual);
    }

    /**
     * Unit test {@link LocalFiles#fileSize(String, long)}
     */
    @Test
    public void test_fileSize_noAttributes() {
        IOException cause = new IOException("expected");

        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenThrow(cause);

            IOException actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().fileSize("pom.xml", -1));

            String message = actual.getMessage();

            Assert.assertTrue("Unexpected message: " + message, message.startsWith("Can not read attributes for:"));
            Assert.assertSame("Unexpected cause", cause, actual.getCause());
        }
    }


    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_all() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_withOutTrailingSlash() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String path = tempFolder.getRoot().getAbsolutePath();
            String actual = new LocalFiles().firstCreated(path, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_fileName() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "2.*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.txt"));

            verify(mockAttributes.get("2.txt")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_images() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*.jpg");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_NoFiles() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*.unknown"));

            Assert.assertTrue("Unexpected message: " + actual.getMessage(),
                actual.getMessage().startsWith("There are no matching files in "));
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(String, String)}
     */
    @Test
    public void test_firstCreated_CantReadDirectory() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenThrow(new IOException("Expected"));

            Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*"));

            Assert.assertTrue("Unexpected message:" + actual.getMessage(),
                actual.getMessage().startsWith("Can not read directory"));
        }
    }


    /**
     * Unit test {@link LocalFiles#lastCreated(String, String)}
     */
    @Test
    public void test_lastCreated() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastCreated(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).creationTime();
        }
    }


    /**
     * Unit test {@link LocalFiles#firstAccessed(String, String)}
     */
    @Test
    public void test_firstAccessed() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstAccessed(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).lastAccessTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#lastAccessed(String, String)}
     */
    @Test
    public void test_lastAccessed() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastAccessed(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).lastAccessTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstModified(String, String)}
     */
    @Test
    public void test_firstModified() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstModified(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).lastModifiedTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#lastModified(String, String)}
     */
    @Test
    public void test_lastModified() throws Exception {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastModified(tempFolderPath, "*");

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).lastModifiedTime();
        }
    }
}