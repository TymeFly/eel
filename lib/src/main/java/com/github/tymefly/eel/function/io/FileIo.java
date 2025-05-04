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
     * Entry point for the {@code head} function, which returns a number of lines from the start of a file.
     * If more than one line is returned then {@literal \n} is used to delimit lines. Files are assumed to be UTF-8
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>head( fileName )</code></li>
     *  <li><code>head( fileName, count )</code></li>
     * </ul>
     * @param context   The EEL Context
     * @param file      a file on the local file system
     * @param count     the maximum number of lines to read. This defaults to {@literal 1}
     * @return up to {@code count} lines from the start of the file
     * @throws IOException if the file could not be read
     * @since 3.0
     */
    @EelFunction("io.head")
    @Nonnull
    public String head(@Nonnull EelContext context,
                       @Nonnull File file,
                       @DefaultArgument("1") int count) throws IOException {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";

        try (
            InputStream fileStream = new FileInputStream(file);
            InputStream limitedStream = new LimitedInputStream(context, fileStream);
            Reader streamReader = new InputStreamReader(limitedStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)
        ) {
            while (count-- > 0) {
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
     * Entry point for the {@code tail} function, which returns a number of lines from the end of a file.
     * If more than one line is returned then {@literal \n} is used to delimit lines. Files are assumed to be UTF-8
     * <br>
     * The EEL syntax for this function is:
     * <ul>
     *  <li><code>tail( fileName )</code></li>
     *  <li><code>tail( fileName, count )</code></li>
     * </ul>
     * @param context   The EEL Context
     * @param file      a file on the local file system
     * @param count     the maximum number of lines to read. This defaults to {@literal 1}
     * @return up to {@code count} lines from the end of the file
     * @throws IOException if the file could not be read
     * @since 3.0
     */
    @EelFunction("io.tail")
    @Nonnull
    public String tail(@Nonnull EelContext context,
                       @Nonnull File file,
                       @DefaultArgument("1") int count) throws IOException {
        String result;

        if (count <= 0 ) {
            result = "";
        } else {
            StringBuilder builder = new StringBuilder();
            String delimiter = "";
            String[] buffer = new String[count];
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

            if (size >= count) {            // buffer has wrapped => next is the oldest element, read full buffer
                size = count;
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
