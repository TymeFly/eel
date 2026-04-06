package com.github.tymefly.eel;

import java.time.ZonedDateTime;

import com.github.tymefly.eel.exception.EelRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link BuildTime}
 */
public class BuildTimeTest {
    private BuildTime buildTime;

    @BeforeEach
    public void setUp() {
        buildTime = new BuildTime("com/github/tymefly/eel/test.properties");
    }

    /**
     * Unit test {@link BuildTime#version()}
     */
    @Test
    public void test_version() {
        assertEquals("99.98", buildTime.version(), "Unexpected version");
    }

    /**
     * Unit test {@link BuildTime#buildDate()}
     */
    @Test
    public void test_buildDate() {
        assertEquals(ZonedDateTime.parse("2024-01-02T03:04:05Z"), buildTime.buildDate(), "Unexpected date");
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_MissingFile() {
       Exception actual = assertThrows(EelRuntimeException.class, () -> new BuildTime("unknown/file"));

       assertEquals("Can not find build time information", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_MalformedVersion() {
       Exception actual = assertThrows(EelRuntimeException.class,
            () -> new BuildTime("com/github/tymefly/eel/badVersion.properties"));

       assertEquals("Build time information for version is malformed ('Bad.Version')", actual.getMessage(), "Unexpected message");
    }

    /**
     * Unit test {@link BuildTime}
     */
    @Test
    public void test_malformedFile() {
       Exception actual = assertThrows(EelRuntimeException.class,
           () -> new BuildTime("com/github/tymefly/eel/empty.properties"));

       assertEquals("Can not find build time information", actual.getMessage(), "Unexpected message");
    }
}