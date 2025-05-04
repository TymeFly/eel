package com.github.tymefly.eel.function.general;

import java.io.File;
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

import com.github.tymefly.eel.Value;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link LocalFiles}
 */
public class LocalFilesTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.parse("2000-01-01T12:13:14Z");

    private final Map<String, BasicFileAttributes> mockAttributes = new HashMap<>();
    private BasicFileAttributes attributes;
    private File tempFolderPath;

    private Value defaultEmptyString;
    private Value defaultNumber;
    private Value defaultDate;
    private Value defaultFileName;


    @Before
    public void setUp() throws Exception {
        attributes = mock();

        when(attributes.creationTime())
            .thenReturn(FileTime.from(946684800, TimeUnit.SECONDS));
        when(attributes.lastAccessTime())
            .thenReturn(FileTime.from(981075661, TimeUnit.SECONDS));
        when(attributes.lastModifiedTime())
            .thenReturn(FileTime.from(1015120922, TimeUnit.SECONDS));
        when(attributes.size())
            .thenReturn(0x12345L);

        defaultEmptyString = Value.of(LocalFiles.DEFAULT_THROW_EXCEPTION);
        defaultNumber = Value.of(-1);
        defaultDate = Value.of("1970");
        defaultFileName = Value.of("myFile.txt");

        tempFolderPath = tempFolder.getRoot();

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
        Path path = Paths.get(tempFolderPath.getAbsolutePath() + "/" + fileName);
        BasicFileAttributes attributes = mock();

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
    public void test_exists() {
        Assert.assertTrue("Expected pom.xml to exist", new LocalFiles().exists("pom.xml"));
        Assert.assertTrue("Expected pom.?ml to exist", new LocalFiles().exists("pom.?ml"));
        Assert.assertFalse("Expected unknown not to exist", new LocalFiles().exists("unknown.file"));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount() {
        File sub = new File(tempFolderPath, "sub");
        File subDir = new File(tempFolderPath, "sub/dir");
        LocalFiles localFiles = new LocalFiles();

        Assert.assertEquals("All Files", 3, localFiles.fileCount(sub, "*"));
        Assert.assertEquals("Text File", 2, localFiles.fileCount(sub, "*.txt"));
        Assert.assertEquals("Dat File", 1, localFiles.fileCount(sub, "*.dat"));
        Assert.assertEquals("No Match", 0, localFiles.fileCount(sub, "*.unknown"));
        Assert.assertEquals("Empty Dir", 0, localFiles.fileCount(subDir, "*"));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount_noDirectory() {
        Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(new File(tempFolderPath + "unknown/dir"), "*"));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Can not read directory "));
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount_notDirectory() {
        Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(new File(tempFolderPath + "sub/file.txt"), "*"));

        Assert.assertTrue("Unexpected message: " + actual.getMessage(),
            actual.getMessage().startsWith("Can not read directory "));
    }


    /**
     * Unit test {@link LocalFiles#createAt(File, Value)}
     */
    @Test
    public void test_createAt() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .createAt(new File("pom.xml"), defaultDate)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected create time", Instant.parse("2000-01-01T00:00:00Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#createAt(File, Value)}
     */
    @Test
    public void test_createAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .createAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }

    /**
     * Unit test {@link LocalFiles#createAt(File, Value)}
     */
    @Test
    public void test_createAt_directory() {
        Assert.assertThrows(IOException.class,
            () -> new LocalFiles().createAt(new File(tempFolderPath, "sub"), defaultDate));
    }


    /**
     * Unit test {@link LocalFiles#accessedAt(File, Value)}
     */
    @Test
    public void test_accessedAt() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .accessedAt(new File("pom.xml"), defaultDate)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected accessed time", Instant.parse("2001-02-02T01:01:01Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#accessedAt(File, Value)}
     */
    @Test
    public void test_accessedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .accessedAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#modifiedAt(File, Value)}
     */
    @Test
    public void test_modifiedAt() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            Instant actual = new LocalFiles()
                .modifiedAt(new File("pom.xml"), defaultDate)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();

            Assert.assertEquals("Unexpected modified time", Instant.parse("2002-03-03T02:02:02Z"), actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#modifiedAt(File, Value)}
     */
    @Test
    public void test_modifiedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .modifiedAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#fileSize(File, Value)}
     */
    @Test
    public void test_fileSize() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.readAttributes(any(Path.class), eq(BasicFileAttributes.class)))
                .thenReturn(attributes);

            long actual = new LocalFiles()
                .fileSize(new File("pom.xml"), defaultNumber);

            Assert.assertEquals("Unexpected modified time", 74565, actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#fileSize(File, Value)}
     */
    @Test
    public void test_fileSize_missingFile() {
        long actual = new LocalFiles()
            .fileSize(new File("unknown.file"), Value.of(-999));

        Assert.assertEquals("Unexpected create time", -999, actual);
    }

    /**
     * Unit test {@link LocalFiles#fileSize(File, Value)}
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
                () -> new LocalFiles().fileSize(new File("pom.xml"), defaultNumber));

            String message = actual.getMessage();

            Assert.assertTrue("Unexpected message: " + message, message.startsWith("Can not read attributes for:"));
            Assert.assertSame("Unexpected cause", cause, actual.getCause());
        }
    }


    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_all() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_all_index1() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*", 1, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.txt"));

            verify(mockAttributes.get("2.txt"), times(2)).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_fileName() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "2.*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.txt"));

            verify(mockAttributes.get("2.txt")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_fileName_index1() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "2.*", 1, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).creationTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_images() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*.jpg", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_NoFiles() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*.unknown", 0, defaultEmptyString));

            Assert.assertTrue("Unexpected message: " + actual.getMessage(),
                actual.getMessage().matches("^No file in .* found with index 0 that matches '\\*.unknown'$"));
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_negativeIndex() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

           Exception actual = Assert.assertThrows(IllegalArgumentException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*", -1, defaultFileName));

            Assert.assertEquals("Unexpected File found: ", "-1 is an invalid index", actual.getMessage());
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_highIndex() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*", 999, defaultEmptyString));

            Assert.assertTrue("Unexpected message: " + actual.getMessage(),
                actual.getMessage().matches("^No file in .* found with index 999 that matches '\\*'$"));
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_NoFiles_Defaulted() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstCreated(tempFolderPath, "*.unknown", 0, defaultFileName);

            Assert.assertEquals("Unexpected File found: ", "myFile.txt", actual);
        }
    }

    /**
     * Unit test {@link LocalFiles#firstCreated(File, String, int, Value)}
     */
    @Test
    public void test_firstCreated_CantReadDirectory() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenThrow(new IOException("Expected"));

            Exception actual = Assert.assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(tempFolderPath, "*", 0, defaultEmptyString));

            Assert.assertTrue("Unexpected message:" + actual.getMessage(),
                actual.getMessage().startsWith("Can not read directory"));
        }
    }


    /**
     * Unit test {@link LocalFiles#lastCreated(File, String, int, Value)}
     */
    @Test
    public void test_lastCreated() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastCreated(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).creationTime();
        }
    }


    /**
     * Unit test {@link LocalFiles#firstAccessed(File, String, int, Value)}
     */
    @Test
    public void test_firstAccessed() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstAccessed(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).lastAccessTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#lastAccessed(File, String, int, Value)} 
     */
    @Test
    public void test_lastAccessed() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastAccessed(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).lastAccessTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#firstModified(File, String, int, Value)}
     */
    @Test
    public void test_firstModified() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().firstModified(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("1.txt"));

            verify(mockAttributes.get("1.txt")).lastModifiedTime();
        }
    }

    /**
     * Unit test {@link LocalFiles#lastModified(File, String, int, Value)}
     */
    @Test
    public void test_lastModified() {
        try (
            MockedStatic<Files> files = Mockito.mockStatic(Files.class)
        ) {
            files.when(() -> Files.list(any(Path.class)))
                .thenAnswer(i -> mockDirectory(files));

            String actual = new LocalFiles().lastModified(tempFolderPath, "*", 0, defaultEmptyString);

            Assert.assertTrue("Unexpected File found: " + actual,
                actual.endsWith("2.jpg"));

            verify(mockAttributes.get("2.jpg")).lastModifiedTime();
        }
    }
}