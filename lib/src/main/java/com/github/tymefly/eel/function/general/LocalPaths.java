package com.github.tymefly.eel.function.general;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL Functions that handle filesystem paths.
 * <br>
 * As EEL is manipulating file paths rather than accessing files or their meta-data, it's safe to pass the paths
 * as strings
 */
@PackagedEelFunction
public class LocalPaths {
    private final char separator;

    /**
     * Constructor
     */
    public LocalPaths() {
        this(File.separatorChar);
    }


    /**
     * Unit test constructor
     * @param separator path name-separator character - either {@literal \} or {@literal /}
     */
    @VisibleForTesting
    LocalPaths(char separator) {
        this.separator = separator;
    }

    /**
     * Returns the {@code path} in canonical format based on the current operating system.
     * @param path  a file path
     * @return      the canonical {@code path}
     * @throws IOException if the canonical path cannot be obtained, for example, if the {@code path} is empty
     */
    @EelFunction("realPath")
    @Nonnull
    public String realPath(@Nonnull String path) throws IOException {
        if (path.isEmpty()) {
            throw new IOException("Empty file path");
        }

        File location = new File(path.replace('\\', '/')).getCanonicalFile();

        return location.getCanonicalPath();
    }


    /**
     * Returns the {@code path} with its last non-slash component and trailing slashes removed.
     * @param path  a file path
     * @return      the {@code path} with its last non-slash component and trailing slashes removed;
     *              if there is no leading path, an empty string is returned
     * @see #realPath(String)
     * @see #baseName(String, String)
     */
    @EelFunction("dirName")
    @Nonnull
    public String dirName(@Nonnull String path) {
        int lastIndex = path.lastIndexOf(separator);
        String fileName = path.substring(0, Math.max(lastIndex, 0));

        return fileName;
    }
    

    /**
     * Returns the {@code path} with any leading directory components removed.
     * @param path      a file path
     * @param extension an optional extension to remove; defaults to an empty string, which removes nothing
     * @return the {@code path} with any leading directory components and the optional {@code extension} removed
     * @see #dirName(String)
     * @see #extension(String, int)
     */
    @EelFunction("baseName")
    @Nonnull
    public String baseName(@Nonnull String path,
                           @DefaultArgument(value = "", description = "Empty text") @Nonnull String extension) {
        int lastIndex = path.lastIndexOf(separator);
        String fileName = path.substring(lastIndex + 1);

        if (fileName.endsWith(extension)) {
            fileName = fileName.substring(0, fileName.length() - extension.length());
        }

        return fileName;
    }


    /**
     * Returns the file extensions from the filename in the specified {@code path}, including any leading
     * dot ('{@literal .}') characters. If there are no extensions, empty text is returned.
     * @param path  the file path
     * @param max   the maximum number of extensions to return
     * @return      the extensions from the specified {@code path}
     * @see #baseName(String, String)
     */
    @EelFunction("extension")
    @Nonnull
    public String extension(@Nonnull String path,
                            @DefaultArgument(value = "-1", description = "All extensions") int max) {
        String result;
        String fileName = baseName(path, "");
        int index = fileName.length();
        int found = index;

        while ((index != 0) && (max != 0)) {
            if (fileName.charAt(--index) == '.') {
                found = index + 1;
                max--;
            }
        }

        result = fileName.substring(found);

        return result;
    }
}
