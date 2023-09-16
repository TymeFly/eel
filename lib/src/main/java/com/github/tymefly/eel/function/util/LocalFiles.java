package com.github.tymefly.eel.function.util;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.function.system.FileSystem;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * A collection of functions that examine files on the local file system.
 * Directories and other file system objects are not managed.
 */
@PackagedEelFunction
public class LocalFiles {
    private enum Direction {
        ASCENDING {
            @Override
            int compare(@Nonnull ZonedDateTime left, @Nonnull ZonedDateTime right) {
                return right.compareTo(left);
            }
        },
        DESCENDING {
            @Override
            int compare(@Nonnull ZonedDateTime left, @Nonnull ZonedDateTime right) {
                return left.compareTo(right);
            }
        };

        abstract int compare(@Nonnull ZonedDateTime left, @Nonnull ZonedDateTime right);
    }


    /**
     * Clumsy exception that is required because we need a Lambda function to throw a checked exception.
     * As this is unsupported we'll throw this unchecked exception and convert it to a checked exception later
     */
    private static class UncheckedIOException extends RuntimeException {
        UncheckedIOException(@Nonnull String message, @Nonnull Object... args) {
            super(String.format(message, args));
        }

        UncheckedIOException(@Nonnull String message, @Nonnull Throwable cause) {
            super(message, cause);
        }

        @Nonnull
        IOException asChecked() {
            IOException checked = new IOException(getMessage(), getCause());

            checked.setStackTrace(this.getStackTrace());

            return checked;
        }
    }


    private static class LazyFileSystem {
        private static final FileSystem INSTANCE = new FileSystem();
    }

    private static class LazyLocalPaths {
        private static final LocalPaths INSTANCE = new LocalPaths();
    }

    private static class LazyZone {
        private static final ZoneId LOCAL = ZonedDateTime.now().getZone();
    }

    private static class LazyStartEpoch {
        private static final ZonedDateTime LOCAL =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), LazyZone.LOCAL);
    }


    /**
     * Entry point for the {@code exists} function, which returns {@literal true} only if the specified file exists
     * <br>
     * The EEL syntax for this function is <code>exists( path )</code>
     * @param path      the path to a file on the local
     * @return {@literal true} only if the file exists; if the file does not exist {@literal false} is returned
     * @throws IOException if it is not possible to determine if the file exists
     */
    @EelFunction(name = "exists")
    public boolean exists(@Nonnull String path) throws IOException {
        String directory = LazyLocalPaths.INSTANCE.dirName(path);
        String glob = LazyLocalPaths.INSTANCE.baseName(path, "");
        long count = fileCount(directory, glob);
        boolean exists = (count != 0);

        return exists;
    }

    /**
     * Entry point for the {@code fileCount} function, which returns the number of files in the {@code directory}
     * that match the {@code glob} pattern. Subdirectory names, and any files they may contain, will not be counted.
     * <br>
     * The EEL syntax for this function is <code>fileCount( directory {, glob } )</code>
     * @param directory the path to a directory on the local file system
     * @param glob      globing expression of file to consider. The defaults to {@code "*"}
     * @return the length of the file in bytes
     * @throws IOException if the length could not be read
     * @since 1.1
     */
    @EelFunction(name = "fileCount")
    public long fileCount(@Nonnull String directory,
            @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Path path = Paths.get(directory);
        PathMatcher matcher = getPathMatcher(directory, glob);
        long count;

        try {
            count = Files.list(path)
                .filter(p -> p.toFile().isFile())
                .filter(matcher::matches)
                .count();
        } catch (IOException e) {
            throw new IOException("Can not read directory " + directory, e);
        }

        return count;
    }


    /**
     * Entry point for the {@code fileSize} function, which returns the DATE that returns the length of the file
     * in bytes
     * <br>
     * The EEL syntax for this function is <code>fileSize( path {, defaultDate } )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal -1}
     * @return the length of the file in bytes
     * @throws IOException if the length could not be read
     */
    @EelFunction(name = "fileSize")
    public long fileSize(@Nonnull String path,
            @DefaultArgument(of = "-1") long defaultValue) throws IOException {
        BasicFileAttributes attributes;

        try {
            attributes = readAttributes(Paths.get(path));
        } catch (UncheckedIOException e) {
            throw e.asChecked();
        }

        return (attributes == null ? defaultValue : attributes.size());
    }


    /**
     * Entry point for the {@code createAt} function, which returns the DATE that a file was created
     * <br>
     * The EEL syntax for this function is <code>createAt( path {, defaultDate } )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was created in the local time zone
     * @throws IOException if the time stamp could not be read
     */
    @EelFunction(name = "createAt")
    @Nonnull
    public ZonedDateTime createAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws IOException {
        return readTimeChecked(Paths.get(path), defaultValue, BasicFileAttributes::creationTime);
    }

    /**
     * Entry point for the {@code accessedAt} function, which returns the DATE that a file was last accessed
     * <br>
     * The EEL syntax for this function is <code>accessedAt( path {, defaultDate } )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last accessed in the local time zone
     * @throws IOException if the time stamp could not be read
     */
    @EelFunction(name = "accessedAt")
    @Nonnull
    public ZonedDateTime accessedAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws IOException {
        return readTimeChecked(Paths.get(path), defaultValue, BasicFileAttributes::lastAccessTime);
    }

    /**
     * Entry point for the {@code modifiedAt} function, which returns the DATE that a file was last modified
     * <br>
     * The EEL syntax for this function is <code>modifiedAt( path {, defaultDate } )</code>
     * @param path          the path to a file on the local
     * @param defaultValue  Value returned if the file does not exist. The defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last modified in the local time zone
     * @throws IOException if the time stamp could not be read
     */
    @EelFunction(name = "modifiedAt")
    @Nonnull
    public ZonedDateTime modifiedAt(@Nonnull String path,
            @Nonnull @DefaultArgument(of = "1970") ZonedDateTime defaultValue) throws IOException {
        return readTimeChecked(Paths.get(path), defaultValue, BasicFileAttributes::lastModifiedTime);
    }


    /**
     * Entry point for the {@code firstCreated} function, which returns the full path to the file in the {@code path}
     * that was created first. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>firstCreated( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException the time of the oldest file could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "firstCreated")
    @Nonnull
    public String firstCreated(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::creationTime, Direction.DESCENDING);

        return findFile(path, glob, comparator);
    }

    /**
     * Entry point for the {@code lastCreated} function, which returns the full path to the file in the {@code path}
     * that was created most recently. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>lastCreated( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException the time of the most recent file could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "lastCreated")
    @Nonnull
    public String lastCreated(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::creationTime, Direction.ASCENDING);

        return findFile(path, glob, comparator);
    }


    /**
     * Entry point for the {@code firstAccessed} function, which returns the full path to the file in the {@code path}
     * that was last accessed first. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>firstAccessed( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException if the first time could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "firstAccessed")
    @Nonnull
    public String firstAccessed(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastAccessTime, Direction.DESCENDING);

        return findFile(path, glob, comparator);
    }

    /**
     * Entry point for the {@code lastAccessed} function, which returns the full path to the file in the {@code path}
     * that was last accessed most recently. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>lastAccessed( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException if the last time could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "lastAccessed")
    @Nonnull
    public String lastAccessed(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastAccessTime, Direction.ASCENDING);

        return findFile(path, glob, comparator);
    }


    /**
     * Entry point for the {@code firstModified} function, which returns the full path to the file in the {@code path}
     * that was modified first. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>firstModified( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException if the first time could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "firstModified")
    @Nonnull
    public String firstModified(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastModifiedTime, Direction.DESCENDING);

        return findFile(path, glob, comparator);
    }


    /**
     * Entry point for the {@code lastModified} function, which returns the full path to the file in the {@code path}
     * that was modified most recently. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is <code>lastModified( path {, glob } )</code>
     * @param path  the path to a directory on the local file system
     * @param glob  globing expression of file to consider. The defaults to {@code "*"}
     * @return the full path to a file in {@code path}
     * @throws IOException if the last time could not be obtained. This might be because
     *          there are no files in the {@code path} that math the {@code glob} pattern
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction(name = "lastModified")
    @Nonnull
    public String lastModified(@Nonnull String path,
           @DefaultArgument(of = "*") @Nonnull String glob) throws IOException {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastModifiedTime, Direction.ASCENDING);

        return findFile(path, glob, comparator);
    }


    @VisibleForTesting
    @Nonnull
    Comparator<Path> fileTimeComparator(@Nonnull Function<BasicFileAttributes, FileTime> attribute,
                                        @Nonnull Direction direction) throws UncheckedIOException {
        Comparator<Path> comparator = (leftPath, rightPath) -> {
            ZonedDateTime leftTime = readTime(leftPath, LazyStartEpoch.LOCAL, attribute);
            ZonedDateTime rightTime = readTime(rightPath, LazyStartEpoch.LOCAL, attribute);

            return direction.compare(leftTime, rightTime);
        };

        return comparator;
    }

    @Nonnull
    private PathMatcher getPathMatcher(@Nonnull String directory, @Nonnull String glob) {
        String separator;

        if ("\\".equals(LazyFileSystem.INSTANCE.fileSeparator())) {             // Flip slashes in Windows file paths
            directory = directory.replace('\\', '/');
        }

        if (directory.isEmpty() || directory.endsWith("/")) {
            separator = "";
        } else {
            separator = "/";
        }

        PathMatcher matcher = FileSystems.getDefault()
            .getPathMatcher("glob:" + directory + separator + glob);

        return matcher;
    }

    @Nonnull
    private String findFile(@Nonnull String directory,
                            @Nonnull String glob,
                            @Nonnull Comparator<Path> comparator) throws IOException {
        PathMatcher matcher = getPathMatcher(directory, glob);
        Path path = Paths.get(directory);
        String fullPath;

        try {
            fullPath = Files.list(path)
                .filter(p -> p.toFile().isFile())
                .filter(matcher::matches)
                .sorted(comparator)
                .map(Path::toString)
                .findFirst()
                .orElseThrow(() -> new UncheckedIOException("There are no matching files in " + directory));
        } catch (IOException e) {
            throw new IOException("Can not read directory " + directory, e);
        } catch (UncheckedIOException e) {
            throw e.asChecked();
        }

        return fullPath;
    }


    @Nonnull
    private ZonedDateTime readTimeChecked(@Nonnull Path path,
            @Nonnull ZonedDateTime defaultValue,
            @Nonnull Function<BasicFileAttributes, FileTime> reader) throws IOException {
        try {
            return readTime(path, defaultValue, reader);
        } catch (UncheckedIOException e) {
            throw e.asChecked();
        }
    }

    @Nonnull
    private ZonedDateTime readTime(@Nonnull Path path,
            @Nonnull ZonedDateTime defaultValue,
            @Nonnull Function<BasicFileAttributes, FileTime> attribute) throws UncheckedIOException {
        BasicFileAttributes attributes = readAttributes(path);
        ZonedDateTime result;

        if (attributes == null) {
            result = defaultValue;
        } else {
            Instant instant = attribute.apply(attributes)
                .toInstant();

            result = ZonedDateTime.ofInstant(instant, LazyZone.LOCAL);
        }

        return result;
    }


    @Nullable
    private BasicFileAttributes readAttributes(@Nonnull Path path) throws UncheckedIOException {
        BasicFileAttributes attributes;
        File file = path.toFile();

        if (!file.exists()) {
            attributes = null;
        } else if (file.isFile()) {
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                throw new UncheckedIOException("Can not read attributes for: " + file.getAbsolutePath(), e);
            }
        } else {
            throw new UncheckedIOException("%s is not a file ", file.getAbsolutePath());
        }

        return attributes;
    }
}
