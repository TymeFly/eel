package com.github.tymefly.eel.function.system;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link FileSystem}
 */
public class FileSystemTest {
    private FileSystem fileSystem;


    @Before
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
        Assert.assertEquals("Unexpected file separator", "/", fileSystem.fileSeparator());
    }

    /**
     * Unit test {@link FileSystem#pwd()}
     */
    @Test
    public void test_pwd() {
        Assert.assertEquals("pwd", "/user/home/me/application/root/", fileSystem.pwd());
    }

    /**
     * Unit test {@link FileSystem#home()}
     */
    @Test
    public void test_Home() {
        Assert.assertEquals("Unexpected home dir", "/user/home/me/", fileSystem.home());
    }

    /**
     * Unit test {@link FileSystem#temp()}
     */
    @Test
    public void test_Temp() {
        Assert.assertEquals("Unexpected temp dir", "/tmp/", fileSystem.temp());
    }
}