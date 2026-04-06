package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;


/**
 * Defines a transformation function that converts a file path into a {@link File}.
 * @since 3.0.2
 */
@FunctionalInterface
public interface FileFactory {
    /**
     * Builds a {@link File} from the supplied path.
     * @param path          the path to a file, which must not be empty.
     *                      There is no requirement for the file to exist
     * @return              the {@link File} associated with {@code path}
     * @throws IOException  if the file cannot be created
     */
    @Nonnull
    File build(@Nonnull String path) throws IOException;
}