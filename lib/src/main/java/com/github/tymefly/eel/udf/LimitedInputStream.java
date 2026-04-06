package com.github.tymefly.eel.udf;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelContext;
/**
 * A decorator for an {@link InputStream} that limits the amount of data read.
 * This is useful when reading from untrusted external sources, where a malicious or faulty source
 * could supply an endless stream, potentially exhausting application resources.
 * This stream enforces a read limit from the backing {@link InputStream}. If the limit is exceeded,
 * an {@link IOException} is thrown. {@link #available()} returns the smaller of the bytes available
 * from the backing stream or the number of bytes remaining before the exception is thrown.
 * Marks are not supported; {@link #markSupported()} always returns {@literal false}.
 */
public class LimitedInputStream extends InputStream {
    private final InputStream backing;
    private int remaining;

    /**
     * Constructs a new LimitedInputStream with a specific data limit.
     * @param context  the current {@link EelContext}
     * @param backing  the backing input stream
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
