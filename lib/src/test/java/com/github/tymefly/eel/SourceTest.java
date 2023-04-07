package com.github.tymefly.eel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelIOException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link Source}
 */
public class SourceTest {
    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_empty() {
        Source source = buildSource("");

        assertSource(1, source, 0, Source.END, Source.END);
    }

    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_singleChar() {
        int index = 0;
        Source source = buildSource("x");

        assertSource(++index, source, 1, 'x', Source.END);

        source.read();
        assertSource(++index, source, 2, Source.END, Source.END);
    }

    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_TwoChars() {
        int index = 0;
        Source source = buildSource("xy");

        assertSource(++index, source, 1, 'x', 'y');

        source.read();
        assertSource(++index, source, 2, 'y', Source.END);

        source.read();
        assertSource(++index, source, 3, Source.END, Source.END);
    }

    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_ThreeChars() {
        int index = 0;
        Source source = buildSource("xyz");

        assertSource(++index, source, 1, 'x', 'y');

        source.read();
        assertSource(++index, source, 2, 'y', 'z');

        source.read();
        assertSource(++index, source, 3, 'z', Source.END);

        source.read();
        assertSource(++index, source, 4, Source.END, Source.END);
    }

    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_readBeyondEndOfString() {
        int index = 0;
        Source source = buildSource("?");

        assertSource(++index, source, 1, '?', Source.END);

        source.read();
        assertSource(++index, source, 2, Source.END, Source.END);

        source.read();
        assertSource(++index, source, 2, Source.END, Source.END);

        source.read();
        assertSource(++index, source, 2, Source.END, Source.END);
    }

    /**
     * Unit test {@link Source}
     */
    @Test
    public void test_readBeyondLimit() {
        int index = 0;
        Source source = buildSource("1234567890", 3);

        assertSource(++index, source, 1, '1', '2');

        source.read();
        assertSource(++index, source, 2, '2', '3');

        source.read();
        assertSource(++index, source, 3, '3', '4');

        EelIOException actual = Assert.assertThrows(EelIOException.class, source::read);

        Assert.assertEquals("Unexpected message",
            "Attempt to read beyond maximum expression length of 3 bytes",
            actual.getMessage());
    }


    @Nonnull
    Source buildSource(@Nonnull String data) {
        return buildSource(data, 1024);
    }

    @Nonnull
    Source buildSource(@Nonnull String data, int maxLength) {
        byte[] raw = data.getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(raw);

        return new Source(stream, maxLength);
    }



    private void assertSource(int index, @Nonnull Source source, int expectedPosition, char expectedCurrent, char expectedNext) {
        Assert.assertEquals(index + ". Unexpected position", expectedPosition, source.position());
        Assert.assertEquals(index + ". Unexpected current", expectedCurrent, source.current());
        Assert.assertEquals(index + ". Unexpected next", expectedNext, source.next());
    }
}