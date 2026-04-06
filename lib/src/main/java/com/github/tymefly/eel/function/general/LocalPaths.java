package com.github.tymefly.eel.function.general;

import java.io.File;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that handle filesystem paths.
 * <b>Note</b>: As EEL manipulates file paths rather than accessing files or their metadata, it is safe to pass
 * the paths as strings.
 * @since 1.0
 */
@PackagedEelFunction
public class LocalPaths {
    private final boolean isWindows;

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
        this.isWindows = (separator == '\\');
    }


    /**
     * Returns the {@code path} in canonical format based on the current operating system.
     * @param context   the current EEL context
     * @param path      a file path; must not be empty
     * @return          the canonical {@code path}
     * @throws Exception if the canonical path cannot be obtained, for example, if the {@code path} is empty
     * @since 1.0
     */
    @EelFunction("realPath")
    @Nonnull
    public String realPath(@Nonnull EelContext context, @Nonnull String path) throws Exception {
        String location = context.getFile(path)
            .getCanonicalPath();

        return location;
    }


    /**
     * Returns the {@code path} with its last non-slash component and trailing slashes removed.
     * @param path  a file path; must not be empty
     * @return      the {@code path} with its last non-slash component and trailing slashes removed;
     *              if there is no leading path, an empty string is returned
     * @see #realPath(EelContext, String)
     * @see #baseName(String, String)
     * @since 1.0
     */
    @EelFunction("dirName")
    @Nonnull
    public String dirName(@Nonnull String path) {
        path = normalise(path);

        int lastIndex = path.lastIndexOf('/');

        if (lastIndex < 0) {
            path = "";
        } else if (lastIndex == 0) {
            path = "/";
        } else {
            path = path.substring(0, lastIndex);
        }

        return (isWindows ? path.replace('/', '\\') : path);            // undo the normalise
    }

    /**
     * Returns the {@code path} with any leading directory components removed.
     * @param path      a file path; must not be empty
     * @param extension an optional extension to remove; defaults to an empty string, which removes nothing
     * @return          the {@code path} with any leading directory components and the optional
     *                      {@code extension} removed
     * @see #dirName(String)
     * @see #extension(String, int)
     * @since 1.0
     */
    @EelFunction("baseName")
    @Nonnull
    public String baseName(@Nonnull String path,
               @DefaultArgument(value = "", description = "Empty text") @Nonnull String extension) {
        path = normalise(path);

        int lastIndex = path.lastIndexOf('/');
        String name = (lastIndex < 0) ? path : path.substring(lastIndex + 1);

        if (!extension.isEmpty() && name.endsWith(extension)) {
            name = name.substring(0, name.length() - extension.length());
        }

        return name;
    }


    /**
     * Returns the file extensions from the filename in the specified {@code path}, including any leading
     * dot ('{@literal .}') characters. If there are no extensions, empty text is returned.
     * @param path  the file path; must not be empty
     * @param max   the maximum number of extensions to return
     * @return      the extensions from the specified {@code path}
     * @see #baseName(String, String)
     * @since 1.0
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


    /**
     * Helper function that will normalise a path. This will be a *nix style path with no trailing slashes
     * @param path          path to normalise
     * @return              a normalised path
     */
    @Nonnull
    private String normalise(@Nonnull String path) {
        if (isWindows) {
            path = path.replace('\\', '/');
        }

        // Trim trailing separators
        int end = path.length();
        while ((end > 1) && (path.charAt(end - 1) == '/')) {
            end--;
        }

        path = path.substring(0, end);

        return path;
    }
}
