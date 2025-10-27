package com.github.tymefly.eel.function.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.LimitedInputStream;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * A collection of functions that examine files on the local file system.
 * Directories and other file system objects are not managed.
 */
@PackagedEelFunction
public class FileIo {
    /**
     * Returns up to {@code count} lines from the start of the given UTF-8 text {@code file}.
     * Lines are delimited with {@literal \n}
     * @param context   the current EEL context.
     * @param file      the file on the local file system.
     * @param lines     the maximum number of lines to read.
     * @return          up to {@code count} lines from the start of the file.
     * @throws IOException if the file could not be read.
     * @since 3.0
     */
    @EelFunction("io.head")
    @Nonnull
    public String head(@Nonnull EelContext context,
                       @Nonnull File file,
                       @DefaultArgument("1") int lines) throws IOException {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";

        try (
            InputStream fileStream = new FileInputStream(file);
            InputStream limitedStream = new LimitedInputStream(context, fileStream);
            Reader streamReader = new InputStreamReader(limitedStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)
        ) {
            while (lines-- > 0) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                builder.append(delimiter)
                    .append(line);
                delimiter = "\n";
            }
        }

        return builder.toString();
    }

    /**
     * Returns up to {@code count} lines from the end of the given UTF-8 text {@code file}.
     * Lines are delimited with {@literal \n}
     * @param context   the current EEL context.
     * @param file      the file on the local file system.
     * @param lines     the maximum number of lines to read.
     * @return          up to {@code count} lines from the end of the file.
     * @throws IOException if the file could not be read.
     * @since 3.0
     */
    @EelFunction("io.tail")
    @Nonnull
    public String tail(@Nonnull EelContext context,
                       @Nonnull File file,
                       @DefaultArgument("1") int lines) throws IOException {
        String result;

        if (lines <= 0 ) {
            result = "";
        } else {
            StringBuilder builder = new StringBuilder();
            String delimiter = "";
            String[] buffer = new String[lines];
            int size = 0;
            int next = 0;

            try (                           // read the complete file into a circular buffer
                InputStream fileStream = new FileInputStream(file);
                InputStream limitedStream = new LimitedInputStream(context, fileStream);
                Reader streamReader = new InputStreamReader(limitedStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)
            ) {
                String line = reader.readLine();

                while (line != null) {
                    buffer[next++] = line;
                    next %= buffer.length;
                    size++;
                    line = reader.readLine();
                }
            }

            if (size >= lines) {            // buffer has wrapped => next is the oldest element, read full buffer
                size = lines;
            } else {                        // buffer hasn't wrapped => index 0 is the oldest, read size elements
                next = 0;
            }

            while (size-- != 0) {           // copy 'size' elements from buffer to builder. 'next' is the oldest element
                builder.append(delimiter)
                    .append(buffer[next++]);
                delimiter = "\n";
                next %= buffer.length;
            }

            result = builder.toString();
        }

        return result;
    }
}
