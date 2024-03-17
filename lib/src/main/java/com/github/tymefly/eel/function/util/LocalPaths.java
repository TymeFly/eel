package com.github.tymefly.eel.function.util;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL Functions that handle filesystem paths
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
     * Entry point for the {@code realPath} function, which returns the {@code path} in a canonicalised format
     * based on current operating system
     * <br>
     * The EEL syntax for this function is <code>realPath( path )</code>
     * @param path  a file path
     * @return      a canonicalized {@code path}
     * @throws IOException  if the canonicalised path could not be obtained. This could be because
     *                          the {@code path} is empty
     */
    @EelFunction(name = "realPath")
    @Nonnull
    public String realPath(@Nonnull String path) throws IOException {
        if (path.isEmpty()) {
            throw new IOException("Empty file path");
        }

        File location = new File(path.replace('\\', '/')).getCanonicalFile();

        return location.getAbsolutePath();
    }


    /**
     * Entry point for the {@code dirName} function, which returns a the {@code path} with its last non-slashed
     * component and trailing slashes removed
     * <br>
     * The EEL syntax for this function is <code>dirName( path )</code>
     * @param path  a file path
     * @return      the {@code path} with its last non-slash component and trailing slashes removed.
     *              If there is no leading path an empty string is returned.
     * @see #realPath(String)
     * @see #baseName(String, String)
     */
    @EelFunction(name = "dirName")
    @Nonnull
    public String dirName(@Nonnull String path) {
        int lastIndex = path.lastIndexOf(separator);
        String fileName = path.substring(0, Math.max(lastIndex, 0));

        return fileName;
    }
    

    /**
     * Entry point for the {@code baseName} function, which returns the {@code path} with any leading directory
     * components removed
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *     <li><code>baseName( path )</code> - 
     *          remove and leading directories from the {@code path}</li>
     *     <li><code>baseName( path, extension )</code> - 
     *          remove and leading directories and the trailing {@code extension} from the {@code path}</li>
     * </ul>
     * @param path       a file path
     * @param extension  optional extension to remove. This defaults to an empty string which removes nothing.
     * @return          {@code path} with any leading directory components and the optional {@code extension} removed
     * @see #dirName(String) 
     * @see #extension(String, int)
     */
    @EelFunction(name = "baseName")
    @Nonnull
    public String baseName(@Nonnull String path, @DefaultArgument("") @Nonnull String extension) {
        int lastIndex = path.lastIndexOf(separator);
        String fileName = path.substring(lastIndex + 1);

        if (fileName.endsWith(extension)) {
            fileName = fileName.substring(0, fileName.length() - extension.length());
        }

        return fileName;
    }


    /**
     * Entry point for the {@code extensions} function, which returns the file extensions from the {@code path}.
     * If there are extensions then the returned string will start with a dot ({@literal .}) character
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *      <li><code>extension( path )</code> -
     *          return all of the extensions that the file represented by {@code path} contains.</li>
     *      <li><code>extension( path, max )</code> -
     *          return at most {@literal max} from extensions that the file represented by {@code path}.
     *          These are counted from the right.</li>
     * </ul>
     * @param path  a file path
     * @param max   maximum number of extensions to return. {@literal -1} (the default) is used to return all
     *              extensions.
     * @return the extensions from the {@code path}
     * @see #baseName(String, String)
     */
    @EelFunction(name = "extension")
    @Nonnull
    public String extension(@Nonnull String path, @DefaultArgument("-1") int max) {
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
