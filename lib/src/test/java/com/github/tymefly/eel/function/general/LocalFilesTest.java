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

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @TempDir
    private Path tempFolder;

    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.parse("2000-01-01T12:13:14Z");

    private final Map<String, BasicFileAttributes> mockAttributes = new HashMap<>();
    private BasicFileAttributes attributes;
    private File dir;

    private Value defaultEmptyString;
    private Value defaultNumber;
    private Value defaultDate;
    private Value defaultFileName;


    @BeforeEach
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

        dir = tempFolder.toFile();

        Files.createDirectory(tempFolder.resolve("sub"));
        Files.createDirectory(tempFolder.resolve("sub/dir"));

        new File(dir, "sub").mkdir();
        new File(dir, "sub/dir").mkdir();
        new File(dir, "sub/file.txt").createNewFile();          // these should be ignored
        new File(dir, "sub/file1.txt").createNewFile();
        new File(dir, "sub/file3.dat").createNewFile();
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
        Path path = Paths.get(dir.getAbsolutePath() + "/" + fileName);
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
            new File(tempFolder.toFile(), fileName).createNewFile();
        } catch (Exception e) {
            // No need to do anything - the test will fail if we can't create the backing files
        }

        mockAttributes.put(fileName, attributes);

        return path;
    }

    /**
     * Unit test {@link LocalFiles#exists(EelContext, String)}
     */
    @Test
    public void test_exists() {
        EelContext context = EelContext.factory().build();

        assertTrue(new LocalFiles().exists(context, "pom.xml"), "Expected pom.xml to exist");
        assertTrue(new LocalFiles().exists(context, "pom.?ml"), "Expected pom.?ml to exist");
        assertFalse(new LocalFiles().exists(context, "unknown.file"), "Expected unknown not to exist");
        assertTrue(new LocalFiles().exists(context, dir.getAbsolutePath()), "directory");
    }

    /**
     * Unit test {@link LocalFiles#exists(EelContext, String)}
     */
    @Test
    public void test_exists_root() {
        EelContext context = EelContext.factory().build();

        assertTrue(new LocalFiles().exists(context, "/*"), "Expected files to exist in the root of file system/drive");
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount() {
        File sub = new File(dir, "sub");
        File subDir = new File(dir, "sub/dir");
        LocalFiles localFiles = new LocalFiles();

        assertEquals(3, localFiles.fileCount(sub, "*"), "All Files");
        assertEquals(3, localFiles.fileCount(sub, ""), "All Files - empty glob");
        assertEquals(2, localFiles.fileCount(sub, "*.txt"), "Text File");
        assertEquals(1, localFiles.fileCount(sub, "*.dat"), "Dat File");
        assertEquals(0, localFiles.fileCount(sub, "*.unknown"), "No Match");
        assertEquals(0, localFiles.fileCount(subDir, "*"), "Empty Dir");
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount_noDirectory() {
        Exception actual = assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(new File(dir + "unknown/dir"), "*"));

        assertTrue(actual.getMessage().startsWith("Can not read directory "), "Unexpected message: " + actual.getMessage());
    }

    /**
     * Unit test {@link LocalFiles#fileCount(File, String)}
     */
    @Test
    public void test_fileCount_notDirectory() {
        Exception actual = assertThrows(IOException.class,
                () -> new LocalFiles().fileCount(new File(dir + "sub/file.txt"), "*"));

        assertTrue(actual.getMessage().startsWith("Can not read directory "), "Unexpected message: " + actual.getMessage());
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

            assertEquals(Instant.parse("2000-01-01T00:00:00Z"), actual, "Unexpected create time");
        }
    }

    /**
     * Unit test {@link LocalFiles#createAt(File, Value)}
     */
    @Test
    public void test_createAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .createAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        assertEquals(DEFAULT_DATE, actual, "Unexpected create time");
    }

    /**
     * Unit test {@link LocalFiles#createAt(File, Value)}
     */
    @Test
    public void test_createAt_directory() {
        assertThrows(IOException.class,
            () -> new LocalFiles().createAt(new File(dir, "sub"), defaultDate));
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

            assertEquals(Instant.parse("2001-02-02T01:01:01Z"), actual, "Unexpected accessed time");
        }
    }

    /**
     * Unit test {@link LocalFiles#accessedAt(File, Value)}
     */
    @Test
    public void test_accessedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .accessedAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        assertEquals(DEFAULT_DATE, actual, "Unexpected create time");
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

            assertEquals(Instant.parse("2002-03-03T02:02:02Z"), actual, "Unexpected modified time");
        }
    }

    /**
     * Unit test {@link LocalFiles#modifiedAt(File, Value)}
     */
    @Test
    public void test_modifiedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .modifiedAt(new File("unknown.file"), Value.of(DEFAULT_DATE));

        assertEquals(DEFAULT_DATE, actual, "Unexpected create time");
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

            assertEquals(74565, actual, "Unexpected modified time");
        }
    }

    /**
     * Unit test {@link LocalFiles#fileSize(File, Value)}
     */
    @Test
    public void test_fileSize_missingFile() {
        long actual = new LocalFiles()
            .fileSize(new File("unknown.file"), Value.of(-999));

        assertEquals(-999, actual, "Unexpected create time");
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

            IOException actual = assertThrows(IOException.class,
                () -> new LocalFiles().fileSize(new File("pom.xml"), defaultNumber));

            String message = actual.getMessage();

            assertTrue(message.startsWith("Can not read attributes for:"), "Unexpected message: " + message);
            assertSame(cause, actual.getCause(), "Unexpected cause");
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

            String actual = new LocalFiles().firstCreated(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("1.txt"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstCreated(dir, "*", 1, defaultEmptyString);

            assertTrue(actual.endsWith("2.txt"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstCreated(dir, "2.*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("2.txt"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstCreated(dir, "2.*", 1, defaultEmptyString);

            assertTrue(actual.endsWith("2.jpg"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstCreated(dir, "*.jpg", 0, defaultEmptyString);

            assertTrue(actual.endsWith("2.jpg"), "Unexpected File found: " + actual);
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

            Exception actual = assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(dir, "*.unknown", 0, defaultEmptyString));

            assertTrue(actual.getMessage().matches("^No file in .* found with index 0 that matches '\\*.unknown'$"), "Unexpected message: " + actual.getMessage());
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

           Exception actual = assertThrows(IllegalArgumentException.class,
                () -> new LocalFiles().firstCreated(dir, "*", -1, defaultFileName));

            assertEquals("-1 is an invalid index", actual.getMessage(), "Unexpected File found: ");
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

            Exception actual = assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(dir, "*", 999, defaultEmptyString));

            assertTrue(actual.getMessage().matches("^No file in .* found with index 999 that matches '\\*'$"), "Unexpected message: " + actual.getMessage());
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

            String actual = new LocalFiles().firstCreated(dir, "*.unknown", 0, defaultFileName);

            assertEquals("myFile.txt", actual, "Unexpected File found: ");
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

            Exception actual = assertThrows(IOException.class,
                () -> new LocalFiles().firstCreated(dir, "*", 0, defaultEmptyString));

            assertTrue(actual.getMessage().startsWith("Can not read directory"), "Unexpected message:" + actual.getMessage());
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

            String actual = new LocalFiles().lastCreated(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("2.jpg"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstAccessed(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("1.txt"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().lastAccessed(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("2.jpg"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().firstModified(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("1.txt"), "Unexpected File found: " + actual);

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

            String actual = new LocalFiles().lastModified(dir, "*", 0, defaultEmptyString);

            assertTrue(actual.endsWith("2.jpg"), "Unexpected File found: " + actual);

            verify(mockAttributes.get("2.jpg")).lastModifiedTime();
        }
    }
}