package com.github.tymefly.eel.function.system;

import java.util.Properties;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * An EEL functions that return local file system information
 */
@PackagedEelFunction
public class FileSystem {
    private final Properties properties;

    private String fileSeparator;
    private String pwd;
    private String home;
    private String temp;


    /**
     * Application constructor
     */
    public FileSystem() {
        this(System.getProperties());
    }

    /**
     * Unit Test constructor
     */
    @VisibleForTesting
    FileSystem(@Nonnull Properties properties) {
        this.properties = properties;
    }


    /**
     * Entry point for the {@code system.fileSeparator} function that returns the Operating System path separator
     * <br>
     * The EEL syntax for this function is <code>system.fileSeparator()</code>
     * @return the current users home directory
     */
    @Nonnull
    @EelFunction(name = "system.fileSeparator")
    public String fileSeparator() {
        if (fileSeparator == null) {
            fileSeparator = properties.getProperty("file.separator");
        }

        return fileSeparator;
    }

    /**
     * Entry point for the {@code pwd} function that returns the canonicalised path, that based on current
     * operating system, of the current working directory.
     * It is guaranteed this will have a trailing {@link #fileSeparator()}
     * <br>
     * The EEL syntax for this function is <code>pwd()</code>
     * @return      a canonicalised {@code path}
     */
    @EelFunction(name = "system.pwd")
    @Nonnull
    public String pwd() {
        if (pwd == null) {
            pwd  = readPath("user.dir");
        }

        return pwd;
    }


    /**
     * Entry point for the {@code system.home} function that returns the canonicalised path, that based on current
     * operating system,  of the users home directory.
     * It is guaranteed this will have a trailing {@link #fileSeparator()}
     * <br>
     * The EEL syntax for this function is <code>system.home()</code>
     * @return the current users home directory
     */
    @Nonnull
    @EelFunction(name = "system.home")
    public String home() {
        if (home == null) {
            home = readPath("user.home");
        }

        return home;
    }


    /**
     * Entry point for the {@code system.temp} function that returns the canonicalised path, that based on current
     * operating system,  of the system temp directory.
     * It is guaranteed this will have a trailing {@link #fileSeparator()}
     * <br>
     * The EEL syntax for this function is <code>system.temp()</code>
     * @return the system temp directory
     */
    @Nonnull
    @EelFunction(name = "system.temp")
    public String temp() {
        if (temp == null) {
            temp = readPath("java.io.tmpdir");
        }

        return temp;
    }


    @Nonnull
    private String readPath(@Nonnull String key) {
        String fileSeparator = fileSeparator();
        String directory = properties.getProperty(key, "");

        if (!directory.endsWith(fileSeparator)) {
            directory += fileSeparator;
        }

        return directory;
    }
}
