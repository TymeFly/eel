package com.github.tymefly.eel.function.general;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.function.system.FileSystem;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * A collection of functions that examine files on the local file system.
 * Directories and other file system objects are not managed.
 */
@PackagedEelFunction
public class LocalFiles {
    /** Default value used by file searching functions to indicate an exception should be thrown if no file is found */
    public static final String DEFAULT_THROW_EXCEPTION = "**THROW_IOEXCEPTION**";


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
        private static final Value LOCAL =
            Value.of(ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), LazyZone.LOCAL));
    }


    /**
     * Entry point for the {@code exists} function, which returns {@literal true} only if the specified file exists.
     * <br>
     * The EEL syntax for this function is <code>exists( path )</code>
     * @param path      a file that might exist on the local. This may include a globbing pattern
     * @return {@literal true} only if the file exists; if the file does not exist {@literal false} is returned
     */
    @EelFunction("exists")
    public boolean exists(@Nonnull String path) {
        File directory = new File(LazyLocalPaths.INSTANCE.dirName(path));
        String glob = LazyLocalPaths.INSTANCE.baseName(path, "");
        long count = fileCount(directory, glob);
        boolean exists = (count != 0);

        return exists;
    }

    /**
     * Entry point for the {@code fileCount} function, which returns the number of files in the {@code directory}
     * that match the {@code glob} pattern. Subdirectory names, and any files they may contain, will not be counted.
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>fileCount( directory )</code></li>
     *  <li><code>fileCount( directory, glob )</code></li>
     * </ul>
     * @param directory the path to a directory on the local file system
     * @param glob      globing expression of files to consider. This defaults to {@code "*"}
     * @return the length of the file in bytes
     * @since 1.1
     */
    @EelFunction("fileCount")
    public long fileCount(@Nonnull File directory, @DefaultArgument("*") @Nonnull String glob) {
        PathMatcher matcher = getPathMatcher(directory, glob);
        long count;

        try (
            Stream<Path> list = listDirectory(directory)
        ) {
            count = list.filter(p -> p.toFile().isFile())
                .filter(matcher::matches)
                .count();
        }

        return count;
    }


    /**
     * Entry point for the {@code fileSize} function, which returns the DATE that returns the length of the file
     * in bytes
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>fileSize( path )</code></li>
     *  <li><code>fileSize( path, defaultDate )</code></li>
     * </ul>
     * @param file           A file on the local file system
     * @param defaultValue   A function that provides the value returned if the file does not exist.
     *                          This defaults to the literal value {@literal -1}
     * @return the length of the file in bytes
     */
    @EelFunction("fileSize")
    public long fileSize(@Nonnull File file, @DefaultArgument("-1") @Nonnull Value defaultValue) {
        BasicFileAttributes attributes = readAttributes(file.toPath());

        return (attributes == null ? defaultValue.asLong() : attributes.size());
    }


    /**
     * Entry point for the {@code createAt} function, which returns the DATE that a file was created
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>createAt( path )</code></li>
     *  <li><code>createAt( path, defaultDate )</code></li>
     * </ul>
     * @param file          A file on the local file system
     * @param defaultValue  A function that provides the value returned if the file does not exist.
     *                          This defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was created in the local time zone
     */
    @EelFunction("createAt")
    @Nonnull
    public ZonedDateTime createAt(@Nonnull File file, @DefaultArgument("1970") @Nonnull Value defaultValue) {
        return readTimeChecked(file.toPath(), defaultValue, BasicFileAttributes::creationTime);
    }

    /**
     * Entry point for the {@code accessedAt} function, which returns the DATE that a file was last accessed
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>accessedAt( path )</code></li>
     *  <li><code>accessedAt( path, defaultDate )</code></li>
     * </ul>
     * @param file          A file on the local file system
     * @param defaultValue  A function that provides the value returned if the file does not exist.
     *                          This defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last accessed in the local time zone
     */
    @EelFunction("accessedAt")
    @Nonnull
    public ZonedDateTime accessedAt(@Nonnull File file, @DefaultArgument("1970") @Nonnull Value defaultValue) {
        return readTimeChecked(file.toPath(), defaultValue, BasicFileAttributes::lastAccessTime);
    }

    /**
     * Entry point for the {@code modifiedAt} function, which returns the DATE that a file was last modified
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>modifiedAt( path )</code></li>
     *  <li><code>modifiedAt( path, defaultDate )</code></li>
     * </ul>
     * @param file          A file on the local file system
     * @param defaultValue  A function that provides the value returned if the file does not exist.
     *                          This defaults to {@literal 1970-01-01 00:00:00Z}
     * @return the time the file was last modified in the local time zone
     */
    @EelFunction("modifiedAt")
    @Nonnull
    public ZonedDateTime modifiedAt(@Nonnull File file, @DefaultArgument("1970") @Nonnull Value defaultValue) {
        return readTimeChecked(file.toPath(), defaultValue, BasicFileAttributes::lastModifiedTime);
    }


    /**
     * Entry point for the {@code firstCreated} function, which returns the full path to the file in the {@code path}
     * that was created first. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>firstCreated( dir )</code></li>
     *  <li><code>firstCreated( dir, glob )</code></li>
     *  <li><code>firstCreated( dir, glob, index )</code></li>
     *  <li><code>firstCreated( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}
     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                          the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("firstCreated")
    @Nonnull
    public String firstCreated(@Nonnull File dir,
           @DefaultArgument("*") @Nonnull String glob,
           @DefaultArgument("0") int index,
           @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::creationTime, Direction.DESCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }

    /**
     * Entry point for the {@code lastCreated} function, which returns the full path to the file in the {@code path}
     * that was created most recently. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>lastCreated( dir )</code></li>
     *  <li><code>lastCreated( dir, glob )</code></li>
     *  <li><code>lastCreated( dir, glob, index )</code></li>
     *  <li><code>lastCreated( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}
     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                      the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("lastCreated")
    @Nonnull
    public String lastCreated(@Nonnull File dir,
            @DefaultArgument("*") @Nonnull String glob,
            @DefaultArgument("0") int index,
            @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::creationTime, Direction.ASCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }


    /**
     * Entry point for the {@code firstAccessed} function, which returns the full path to the file in the {@code path}
     * that was last accessed first. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>firstAccessed( dir )</code></li>
     *  <li><code>firstAccessed( dir, glob )</code></li>
     *  <li><code>firstAccessed( dir, glob, index )</code></li>
     *  <li><code>firstAccessed( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}
     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                      the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("firstAccessed")
    @Nonnull
    public String firstAccessed(@Nonnull File dir,
            @DefaultArgument("*") @Nonnull String glob,
            @DefaultArgument("0") int index,
            @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastAccessTime, Direction.DESCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }

    /**
     * Entry point for the {@code lastAccessed} function, which returns the full path to the file in the {@code path}
     * that was last accessed most recently. Files in subdirectories are not considered
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>lastAccessed( dir )</code></li>
     *  <li><code>lastAccessed( dir, glob )</code></li>
     *  <li><code>lastAccessed( dir, glob, index )</code></li>
     *  <li><code>lastAccessed( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}

     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                      the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("lastAccessed")
    @Nonnull
    public String lastAccessed(@Nonnull File dir,
            @DefaultArgument("*") @Nonnull String glob,
            @DefaultArgument("0") int index,
            @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastAccessTime, Direction.ASCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }


    /**
     * Entry point for the {@code firstModified} function, which returns the full path to the file in the {@code path}
     * that was modified first. Files in subdirectories are not considered
     * <br>
     * <ul>
     *  <li><code>firstModified( dir )</code></li>
     *  <li><code>firstModified( dir, glob )</code></li>
     *  <li><code>firstModified( dir, glob, index )</code></li>
     *  <li><code>firstModified( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}
     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                      the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("firstModified")
    @Nonnull
    public String firstModified(@Nonnull File dir,
            @DefaultArgument("*") @Nonnull String glob,
            @DefaultArgument("0") int index,
            @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastModifiedTime, Direction.DESCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }


    /**
     * Entry point for the {@code lastModified} function, which returns the full path to the file in the {@code path}
     * that was modified most recently. Files in subdirectories are not considered
     * <br>
     * <ul>
     *  <li><code>lastModified( dir )</code></li>
     *  <li><code>lastModified( dir, glob )</code></li>
     *  <li><code>lastModified( dir, glob, index )</code></li>
     *  <li><code>lastModified( dir, glob, index, defaultValue )</code></li>
     * </ul>
     * @param dir           a directory on the local file system
     * @param glob          globing expression of the file to consider. This defaults to {@code "*"}
     * @param index         0 based index of the file to retrieve. This defaults to {@code "*"}
     * @param defaultValue  A function that provides the value returned if no files exist in the {@code path} that match
     *                      the {@code glob} pattern. This defaults to throwing an {@link IOException}
     * @return the full path to a file in {@code path}
     * @see #fileCount
     * @since 1.1
     */
    @EelFunction("lastModified")
    @Nonnull
    public String lastModified(@Nonnull File dir,
            @DefaultArgument("*") @Nonnull String glob,
            @DefaultArgument("0") int index,
            @DefaultArgument(DEFAULT_THROW_EXCEPTION) @Nonnull Value defaultValue) {
        Comparator<Path> comparator = fileTimeComparator(BasicFileAttributes::lastModifiedTime, Direction.ASCENDING);

        return findFile(dir, glob, index, comparator, defaultValue);
    }


    @VisibleForTesting
    @Nonnull
    Comparator<Path> fileTimeComparator(@Nonnull Function<BasicFileAttributes, FileTime> attribute,
                                        @Nonnull Direction direction) {
        Comparator<Path> comparator = (leftPath, rightPath) -> {
            ZonedDateTime leftTime = readTime(leftPath, LazyStartEpoch.LOCAL, attribute);
            ZonedDateTime rightTime = readTime(rightPath, LazyStartEpoch.LOCAL, attribute);

            return direction.compare(leftTime, rightTime);
        };

        return comparator;
    }

    @Nonnull
    private PathMatcher getPathMatcher(@Nonnull File directory, @Nonnull String glob) {
        String path = null;

        try {
            path = directory.getCanonicalPath();
        } catch (IOException e) {
            throwAsUnchecked(e);
        }

        if ("\\".equals(LazyFileSystem.INSTANCE.fileSeparator())) {             // Flip slashes in Windows file paths
            path = path.replace('\\', '/');
        }

        PathMatcher matcher = FileSystems.getDefault()
            .getPathMatcher("glob:" + path + "/" + glob);

        return matcher;
    }


    @Nonnull
    private String findFile(@Nonnull File path,
                            @Nonnull String glob,
                            int index,
                            @Nonnull Comparator<Path> comparator,
                            @Nonnull Value defaultValue) {
        Preconditions.checkArgument((index >= 0), "%d is an invalid index", index);

        PathMatcher matcher = getPathMatcher(path, glob);
        String fullPath;

        try (
            Stream<Path> list = listDirectory(path)
        ) {
            fullPath = list.filter(p -> p.toFile().isFile())
                .filter(matcher::matches)
                .sorted(comparator)
                .map(Path::toFile)
                .map(this::canonicalisePath)
                .skip(index)
                .findFirst()
                .orElseGet(() -> evaluateDefault(defaultValue, path, glob, index));
        }

        return fullPath;
    }


    @Nonnull
    private String canonicalisePath(@Nonnull File file) {
        String path = null;                // To satisfy compiler checks

        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            throwAsUnchecked(e);
        }

        return path;
    }

    @Nonnull
    private String evaluateDefault(@Nonnull Value defaultValue,
                                   @Nonnull File directory,
                                   @Nonnull String glob,
                                   int index) {
        String evaluated = defaultValue.asText();

        if (DEFAULT_THROW_EXCEPTION.equals(evaluated)) {
            IOException checked = new IOException("No file in " + directory.getAbsolutePath() +
                " found with index " + index + " that matches '" + glob + "'");

            throwAsUnchecked(checked);
        }

        return evaluated;
    }


    @Nonnull
    private ZonedDateTime readTimeChecked(@Nonnull Path path,
            @Nonnull Value defaultValue,
            @Nonnull Function<BasicFileAttributes, FileTime> reader) {
        return readTime(path, defaultValue, reader);
    }

    @Nonnull
    private ZonedDateTime readTime(@Nonnull Path path,
            @Nonnull Value defaultValue,
            @Nonnull Function<BasicFileAttributes, FileTime> attribute) {
        BasicFileAttributes attributes = readAttributes(path);
        ZonedDateTime result;

        if (attributes == null) {
            result = defaultValue.asDate();
        } else {
            Instant instant = attribute.apply(attributes)
                .toInstant();

            result = ZonedDateTime.ofInstant(instant, LazyZone.LOCAL);
        }

        return result;
    }


    @Nullable
    private BasicFileAttributes readAttributes(@Nonnull Path path) {
        BasicFileAttributes attributes = null;
        File file = path.toFile();

        if (!file.exists()) {
            // do nothing - attributes is already null
        } else if (file.isFile()) {
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                throwAsUnchecked(new IOException("Can not read attributes for: " + file.getAbsolutePath(), e));
            }
        } else {
            throwAsUnchecked(new IOException(file.getAbsolutePath() + " is not a file "));
        }

        return attributes;
    }


    @Nonnull
    private Stream<Path> listDirectory(@Nonnull File directory) {
        Stream<Path> list = null;                            // To satisfy compiler checks

        try {
            Path path = directory.getCanonicalFile().toPath();
            list = Files.list(path);
        } catch (IOException e) {
            IOException checked = new IOException("Can not read directory " + directory.getAbsolutePath(), e);

            throwAsUnchecked(checked);
        }

        return list;
    }

    /**
     * Ugly kludge to throw a checked exception as if it were unchecked.
     * This is required because we need Lambda functions to throw checked exceptions.
     * @param checked   A checked exception
     * @param <E>       Type of the checked exception
     * @throws E        a checked exception
     */
    private static <E extends Throwable> void throwAsUnchecked(@Nonnull Exception checked) throws E {
        throw (E) checked;
    }
}
