package com.github.tymefly.eel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.annotation.VisibleForTesting;

import static java.lang.Math.max;

/**
 * Class to manage the expression source with a single character lookahead.
 */
class Source implements Input {
    private static final char UNDEFINED = (char) -1;
    private final int maxLength;
    private final InputStreamReader data;

    private int position;
    private char current;
    private char next;


    private Source(@Nonnull InputStream source, int maxLength) {
        this.data = new InputStreamReader(source, StandardCharsets.UTF_8);
        this.maxLength = max(maxLength, 2);     // make sure we can always read 'current' and 'next'
        this.current = UNDEFINED;
        this.next = UNDEFINED;
    }

    /** Initialise this class outside the constructor */
    @Nonnull
    private Source init() {
        read();
        read();
        position = (current == END ? 0 : 1);

        return this;
    }

    /**
     * Factory method for Source instances
     * @param source        source expression
     * @param maxLength     maximum valid length of the expression
     * @return              a new Source instance
     */
    @Nonnull
    static Source build(@Nonnull String source, int maxLength) {
        return build(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), maxLength);
    }

    /**
     * Factory method for Source instances
     * @param source        source expression
     * @param maxLength     maximum valid length of the expression
     * @return              a new Source instance
     */
    @Nonnull
    static Source build(@Nonnull InputStream source, int maxLength) {
        return new Source(source, maxLength)
            .init();
    }


    @Override
    public char read() {
        if (current != END) {
            position++;
            current = this.next;

            try {
                next = (char) data.read();
                next = (next == UNDEFINED ? END : this.next);
            } catch (IOException e) {
                throw new EelSourceException("Failed to read source after position " + position, e);
            }

            if ((position > maxLength) && (current != END)) {
                throw new EelSourceException(
                    "Attempt to read beyond maximum expression length of " + maxLength + " bytes");
            }
        }

        return current;
    }

    @Override
    public char current() {
        return current;
    }

    @Override
    public char next() {
        return next;
    }

    /**
     * Returns the 1-based position in the input of the current character.
     * @return the 1-based position in the input of the current character
     */
    int position() {
        return position;
    }


    /**
     * Maximum acceptable length of the expression
     * @return acceptable length of the expression
     */
    @VisibleForTesting
    int getMaxLength() {
        return maxLength;
    }
}
