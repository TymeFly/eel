package com.github.tymefly.eel.function.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.github.tymefly.eel.exception.EelFunctionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link LocalFiles}
 */
public class LocalFilesTest {
    private static final ZonedDateTime DEFAULT_DATE = ZonedDateTime.parse("2000-01-01T12:13:14Z");


    private BasicFileAttributes attributes;

    @Before
    public void setUp() {
        attributes = mock(BasicFileAttributes.class);

        when(attributes.creationTime())
            .thenReturn(FileTime.from(946684800, TimeUnit.SECONDS));
        when(attributes.lastAccessTime())
            .thenReturn(FileTime.from(981075661, TimeUnit.SECONDS));
        when(attributes.lastModifiedTime())
            .thenReturn(FileTime.from(1015120922, TimeUnit.SECONDS));
        when(attributes.size())
            .thenReturn(0x12345L);
    }



    /**
     * Unit test {@link LocalFiles#exists(String)}
     */
    @Test
    public void test_exists() {
        Assert.assertTrue("Expected POM to exist", new LocalFiles().exists("pom.xml"));
        Assert.assertFalse("Expected unknown not to exist", new LocalFiles().exists("unknown.file"));
    }


    /**
     * Unit test {@link LocalFiles#createAt(String, ZonedDateTime)}
     */
    @Test
    public void test_createAt() {
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
    public void test_createAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .createAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#accessedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_accessedAt() {
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
    public void test_accessedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .accessedAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#modifiedAt(String, ZonedDateTime)}
     */
    @Test
    public void test_modifiedAt() {
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
    public void test_modifiedAt_missingFile() {
        ZonedDateTime actual = new LocalFiles()
            .modifiedAt("unknown.file", DEFAULT_DATE);

        Assert.assertEquals("Unexpected create time", DEFAULT_DATE, actual);
    }


    /**
     * Unit test {@link LocalFiles#fileSize(String, long)}
     */
    @Test
    public void test_fileSize() {
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
    public void test_fileSize_missingFile() {
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

            EelFunctionException actual = Assert.assertThrows(EelFunctionException.class,
                () -> new LocalFiles().fileSize("pom.xml", -1));

            String message = actual.getMessage();

            Assert.assertTrue("Unexpected message: " + message, message.startsWith("Can not read attributes for:"));
            Assert.assertSame("Unexpected cause", cause, actual.getCause());
        }
    }
}