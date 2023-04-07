package com.github.tymefly.eel.function.util;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelFunctionException;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * A collection of functions that return time stamps associated with file on the local file system.
 * Directories and other file system objects are not managed.
 */
@PackagedEelFunction
public class LocalFiles {
    private static class LazyZone {
        private static final ZoneId LOCAL = ZonedDateTime.now().getZone();
    }


    /**
     * Entry point for the {@code exists} function that returns {@literal true} only if the specified file exists
     * <br>
     * The EEL syntax for this function is <code>exists( path )</code>
     * @param path      the path to a file on the local
     * @return {@literal true} only if the file exists; if the file does not exist {@literal false} is returned
     */
    @EelFunction(name = "exists")
    public boolean exists(@Nonnull String path) {
        File file = new File(path);
        boolean exists = file.isFile();

        return exists;
    }

    /**
     * Entry point for the {@code createAt} function that returns the DATE that a file was created
     * <br>
     * The EEL syntax for this function is <code>createAt( path )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was created in the local time zone
     * @throws EelFunctionException if the time stamp could not be read
     */
    @EelFunction(name = "createAt")
    @Nonnull
    public ZonedDateTime createAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws EelFunctionException {
        return readTime(path, defaultValue, BasicFileAttributes::creationTime);
    }

    /**
     * Entry point for the {@code accessedAt} function that returns the DATE that a file was last accessed
     * <br>
     * The EEL syntax for this function is <code>accessedAt( path )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last accessed in the local time zone
     * @throws EelFunctionException if the time stamp could not be read
     */
    @EelFunction(name = "accessedAt")
    @Nonnull
    public ZonedDateTime accessedAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws EelFunctionException {
        return readTime(path, defaultValue, BasicFileAttributes::lastAccessTime);
    }

    /**
     * Entry point for the {@code modifiedAt} function that returns the DATE that a file was last modified
     * <br>
     * The EEL syntax for this function is <code>modifiedAt( path )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last modified in the local time zone
     * @throws EelFunctionException if the time stamp could not be read
     */
    @EelFunction(name = "modifiedAt")
    @Nonnull
    public ZonedDateTime modifiedAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws EelFunctionException {
        return readTime(path, defaultValue, BasicFileAttributes::lastModifiedTime);
    }

    /**
     * Entry point for the {@code fileSize} function that returns the DATE that returns the length of the file
     * in bytes
     * <br>
     * The EEL syntax for this function is <code>fileSize( path )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal -1}
     * @return the length of the file in bytes
     * @throws EelFunctionException if the length could not be read
     */
    @EelFunction(name = "fileSize")
    public long fileSize(@Nonnull String path,
            @DefaultArgument(of = "-1") long defaultValue) throws EelFunctionException {
        BasicFileAttributes attributes = readAttributes(path);

        return (attributes == null ? defaultValue : attributes.size());
    }


    @Nonnull
    private ZonedDateTime readTime(@Nonnull String path,
            @Nonnull ZonedDateTime defaultValue,
            @Nonnull Function<BasicFileAttributes, FileTime> reader) throws EelFunctionException {
        BasicFileAttributes attributes = readAttributes(path);
        ZonedDateTime result;

        if (attributes == null) {
            result = defaultValue;
        } else {
            Instant instant = reader.apply(attributes)
                    .toInstant();

            result = ZonedDateTime.ofInstant(instant, LazyZone.LOCAL);
        }

        return result;
    }


    @Nullable
    private BasicFileAttributes readAttributes(@Nonnull String path) throws EelFunctionException {
        File file = new File(path);
        BasicFileAttributes attributes;

        if (file.isFile()) {
            try {
                attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            } catch (IOException e) {
                throw new EelFunctionException("Can not read attributes for: " + file.getAbsolutePath(), e);
            }
        } else {
            attributes = null;
        }

        return attributes;
    }
}
