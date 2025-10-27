package com.github.tymefly.eel.function.system;

import java.util.Properties;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that return local file system information
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
     * Returns the operating system path separator as text.
     * This usually is either a forward slash ({@literal '/'}) or a backslash ({@literal '\'}).
     * @return           the operating system path separator as text
     */
    @Nonnull
    @EelFunction("system.fileSeparator")
    public String fileSeparator() {
        if (fileSeparator == null) {
            fileSeparator = properties.getProperty("file.separator");
        }

        return fileSeparator;
    }

    /**
     * Returns the canonicalised path of the current working directory.
     * The result always includes a trailing {@link #fileSeparator()}.
     * @return the canonicalised path of the current working directory.
     */
    @EelFunction("system.pwd")
    @Nonnull
    public String pwd() {
        if (pwd == null) {
            pwd = readPath("user.dir");
        }

        return pwd;
    }


    /**
     * Returns the canonicalised path representing the current user's home directory.
     * The result always includes a trailing {@link #fileSeparator()}.
     * @return the canonicalised path of the current user's home directory.
     */
    @Nonnull
    @EelFunction("system.home")
    public String home() {
        if (home == null) {
            home = readPath("user.home");
        }

        return home;
    }


    /**
     * Returns the canonicalised path of the temporary directory.
     * The result always includes a trailing {@link #fileSeparator()}.
     * @return the canonicalised path of the temporary directory.
     */
    @Nonnull
    @EelFunction("system.temp")
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
