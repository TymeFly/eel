package com.github.tymefly.eel.function.util;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL Functions that handle filesystem paths
 */
@PackagedEelFunction
public class Paths {
    /**
     * Entry point for the {@code realPath} function that returns the {@code path} in a canonicalised format
     * based on current operating system
     * <br>
     * The EEL syntax for this function is <code>realPath( path )</code>
     * @param path  a file path
     * @return      a canonicalized {@code path}
     * @throws IOException if the canonicalised path could not be obtained.
     */
    @EelFunction(name = "realPath")
    @Nonnull
    public String realPath(@Nonnull String path) throws IOException {
        File location = new File(path).getCanonicalFile();

        return location.getAbsolutePath();
    }


    /**
     * Entry point for the {@code dirName} function that returns a canonicalised {@code path}, based on current
     * operating system, with its last non-slashed component and trailing slashes removed
     * <br>
     * The EEL syntax for this function is <code>dirName( path )</code>
     * @param path  a file path
     * @return      a canonicalized {@code path} with its last non-slash component and trailing slashes removed.
     *              If there is no leading path an empty string is returned.
     * @throws IOException if the canonicalised directory name could not be obtained.
     * @see #realPath(String)
     * @see #baseName(String, String)
     */
    @EelFunction(name = "dirName")
    @Nonnull
    public String dirName(@Nonnull String path) throws IOException {
        File parent = new File(path).getParentFile();

        return (parent == null ? "" : parent.getCanonicalPath());
    }
    

    /**
     * Entry point for the {@code baseName} function that returns the {@code path} with any leading directory
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
    public String baseName(@Nonnull String path, @DefaultArgument(of = "") @Nonnull String extension) {
        String fileName = new File(path).getName();

        if (fileName.endsWith(extension)) {
            fileName = fileName.substring(0, fileName.length() - extension.length());
        }

        return fileName;
    }


    /**
     * Entry point for the {@code extensions} function that returns the file extensions from the {@code path}.
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
    public String extension(@Nonnull String path, @DefaultArgument(of = "-1") int max) {
        String result;
        String fileName = baseName(path, "");
        int index = fileName.length();
        int found = index;

        while ((index != 0) && (max != 0)) {
            if (fileName.charAt(--index) == '.') {
                found = index;
                max--;
            }
        }

        result = fileName.substring(found);

        return result;
    }
}
