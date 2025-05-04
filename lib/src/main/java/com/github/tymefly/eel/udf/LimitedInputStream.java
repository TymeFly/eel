package com.github.tymefly.eel.udf;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;

/**
 * A Decorator for InputStream classes that will limit the amount of data read.
 * There is a safety issue that can occur if the application is needs to read data from an untrusted
 * external source. A malicious or badly implemented source might break the JVM by supplying a never ending
 * stream of data that will eventually use up all the resources available to the application.
 * This InputStream will combat this problem by limiting the amount of data the backing InputStream can read.
 * If too much data is read then a {@link IOException} is thrown.
 * {@link #available()} will return the lowest of either the bytes available to the backing stream or the number of
 * bytes than can be read before the exception is thrown.
 * This class does not support marks, so {@link #markSupported()} will always return {@literal false}
 */
public class LimitedInputStream extends InputStream {
    private final InputStream backing;
    private int remaining;


    /**
     * Construct a new LimitedInputStream with a specific data limit
     * @param context       the current EEL Context
     * @param backing       the backing reader
     */
    public LimitedInputStream(@Nonnull EelContext context, @Nonnull InputStream backing) {
        this.backing = backing;
        this.remaining = context.getIoLimit();
    }


    @Override
    public int available() throws IOException {
        return Math.min(remaining, backing.available());
    }

    @Override
    public int read() throws IOException {
        boolean valid = (remaining >= 0);
        int data;

        if (!valid) {
            data = -1;
        } else {
            data = backing.read();
            valid = (remaining-- != 0) || (data == -1);
        }

        if (!valid) {
            throw new IOException("Too much data has been read");
        }

        return data;
    }


    @Override
    public long skip(long skip) throws IOException {
        return super.skip(Math.min(remaining, skip));          // Allow function to return if limit is reached
    }

    @Override
    public void skipNBytes(long skip) throws IOException {
        super.skipNBytes(Math.min(remaining, skip));           // Allow function to return if limit is reached
    }

    @Override
    public void close() throws IOException {
        backing.close();
    }
}
