package com.github.tymefly.eel.doc.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.doc.exception.EelDocException;

/**
 * Utility class containing functions to handle files
 */
public class FileUtils {
    private static final ClassLoader CLASS_LOADER = FileUtils.class.getClassLoader();
    private static final EelContext CONTEXT = EelContext.factory()
        .build();


    /** Hide utility class constructor */
    private FileUtils() {
    }


    /**
     * Returns {@literal true} only if {@code fileName} is a file that can be read
     * @param file      name of a file to test
     * @return {@literal true} only if {@code fileName} is a file that can be read
     */
    public static boolean canRead(@Nonnull File file) {
        return (file.isFile() && file.canRead());
    }


    /**
     * Read a file that contains multiple lines, each of which can be an EEL expression
     * @param file      the file to read
     * @return          a list of evaluated lines representing the content of the file
     * @throws EelDocException if the specified file does not exist, if it's not a file or cannot be read.
     */
    @Nonnull
    public static List<String> read(@Nonnull File file) throws EelDocException {
        Path path = file.toPath();
        List<String> content;

        try (
            Stream<String> lines = Files.lines(path)
        ) {
            content = lines
                .map(l -> Eel.compile(CONTEXT, l).evaluate().asText())
                .toList();
        } catch (IOException e) {
            throw new EelDocException("Failed to read file " + file.getAbsolutePath());
        }

        return content;
    }


    /**
     * Write the {@code content} to a new file. If the file already exists it will be overwritten; if it does not
     * exist it will be created along with any required parent directories
     * @param file          file to write.
     * @param content       content of the generated file
     * @param fileEncoding  File encoding
     * @throws EelDocException if the file could not be written
     */
    public static void write(@Nonnull File file,
                             @Nonnull String content,
                             @Nonnull Charset fileEncoding) throws EelDocException {
        Path path = file.toPath();

        createParentDirectory(path);

        try {
            Files.writeString(path,
                content,
                fileEncoding,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new EelDocException("Failed to write file '" + file + "'", e);
        }
    }


    /**
     * Copy a file from the classpath to another location. If the target already exists it will be overwritten
     * @param sourceName    file name as seen on the class path. This may include a path
     * @param target        file to write
     * @throws EelDocException if the file could not be copied
     */
    public static void copyResource(@Nonnull String sourceName, @Nonnull File target) throws EelDocException {
        Path path = target.toPath();

        createParentDirectory(path);

        try (
            InputStream in = CLASS_LOADER.getResourceAsStream(sourceName)
        ) {
            if (in == null) {
                throw new EelDocException("Missing resource '%s'", sourceName);
            }

            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new EelDocException("Failed to copy file '%s' to '%s'", sourceName, target.getAbsolutePath(), e);
        }
    }



    /**
     * Create any/all parent directories required for {@code path}. Does nothing if the directories already exist
     * @param path      child path
     * @throws EelDocException if the directories could not be created
     */
    private static void createParentDirectory(@Nonnull Path path) {
        Path parent = path.getParent();

        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new EelDocException("Failed to create directory '" + parent + "'", e);
            }
        }
    }
}
