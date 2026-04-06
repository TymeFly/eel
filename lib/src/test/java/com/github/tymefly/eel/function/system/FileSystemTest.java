package com.github.tymefly.eel.function.system;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link FileSystem}
 */
public class FileSystemTest {
    private FileSystem fileSystem;


    @BeforeEach
    public void setUp() {
        Properties properties = new Properties();

        properties.setProperty("file.separator", "/");
        properties.setProperty("user.home", "/user/home/me");
        properties.setProperty("java.io.tmpdir", "/tmp");
        properties.setProperty("user.dir", "/user/home/me/application/root");

        fileSystem = new FileSystem(properties);
    }

    /**
     * Unit test {@link FileSystem#fileSeparator()}
     */
    @Test
    public void test_Separator() {
        assertEquals("/", fileSystem.fileSeparator(), "Unexpected file separator");
    }

    /**
     * Unit test {@link FileSystem#pwd()}
     */
    @Test
    public void test_pwd() {
        assertEquals("/user/home/me/application/root/", fileSystem.pwd(), "pwd");
    }

    /**
     * Unit test {@link FileSystem#home()}
     */
    @Test
    public void test_Home() {
        assertEquals("/user/home/me/", fileSystem.home(), "Unexpected home dir");
    }

    /**
     * Unit test {@link FileSystem#temp()}
     */
    @Test
    public void test_Temp() {
        assertEquals("/tmp/", fileSystem.temp(), "Unexpected temp dir");
    }
}