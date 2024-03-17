package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelRuntimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link BuildTime}
 */
public class BuildTimeTest {
    private BuildTime buildTime;

    @Before
    public void setUp() {
        buildTime = new BuildTime("com/github/tymefly/eel/test.properties");
    }

    /**
     * Unit test {@link BuildTime#version()}
     */
    @Test
    public void test_version() {
        Assert.assertEquals("Unexpected version", "99.98", buildTime.version());
    }

    /**
     * Unit test {@link BuildTime#buildDate()}
     */
    @Test
    public void test_buildDate() {
        Assert.assertEquals("Unexpected date", ZonedDateTime.parse("2024-01-02T03:04:05Z"), buildTime.buildDate());
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_MissingFile() {
       Exception actual = Assert.assertThrows(EelRuntimeException.class, () -> new BuildTime("unknown/file"));

       Assert.assertEquals("Unexpected message", "Can not find build time information", actual.getMessage());
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_MalformedVersion() {
       Exception actual = Assert.assertThrows(EelRuntimeException.class,
            () -> new BuildTime("com/github/tymefly/eel/badVersion.properties"));

       Assert.assertEquals("Unexpected message",
           "Build time information for version is malformed ('Bad.Version')",
           actual.getMessage());
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_malformedFile() {
       Exception actual = Assert.assertThrows(EelRuntimeException.class,
           () -> new BuildTime("com/github/tymefly/eel/empty.properties"));

       Assert.assertEquals("Unexpected message", "Failed to load EEL build time information", actual.getMessage());
    }
}