package com.github.tymefly.eel.udf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link LimitedInputStream}
 */
public class LimitedInputStreamTest {
    private LimitedInputStream overflow;
    private LimitedInputStream underflow;


    @BeforeEach
    public void setUp() {
        ByteArrayInputStream raw1 = new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream raw2 = new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8));

        overflow = new LimitedInputStream(EelContext.factory().withIoLimit(3).build(), raw1);
        underflow = new LimitedInputStream(EelContext.factory().withIoLimit(6).build(), raw2);
    }

    /**
     * Unit test {@link LimitedInputStream#read()}
     */
    @Test
    public void test_read_byte_underflow() throws Exception {
        assertEquals(5, underflow.available(), "#0 available()");
        assertEquals((byte) 'H', underflow.read(), "Unexpected Char 1");
        assertEquals(4, underflow.available(), "#1 available()");
        assertEquals((byte) 'e', underflow.read(), "Unexpected Char 2");
        assertEquals(3, underflow.available(), "#2 available()");
        assertEquals((byte) 'l', underflow.read(), "Unexpected Char 3");
        assertEquals(2, underflow.available(), "#3 available()");
        assertEquals((byte) 'l', underflow.read(), "Unexpected Char 4");
        assertEquals(1, underflow.available(), "#4 available()");
        assertEquals((byte) 'o', underflow.read(), "Unexpected Char 5");
        assertEquals(0, underflow.available(), "#5 available()");
        assertEquals(-1, underflow.read(), "Unexpected Char 5");
        assertEquals(0, underflow.available(), "#6 available()");
    }

    /**
     * Unit test {@link LimitedInputStream#read()}
     */
    @Test
    public void test_read_byte_overflow() throws Exception {
        assertEquals(3, overflow.available(), "#0 available()");
        assertEquals((byte) 'H', overflow.read(), "Unexpected Char 1");
        assertEquals(2, overflow.available(), "#1 available()");
        assertEquals((byte) 'e', overflow.read(), "Unexpected Char 2");
        assertEquals(1, overflow.available(), "#2 available()");
        assertEquals((byte) 'l', overflow.read(), "Unexpected Char 3");
        assertEquals(0, overflow.available(), "#3 available()");
        assertThrows(IOException.class, () -> overflow.read());
    }


    /**
     * Unit test {@link LimitedInputStream#read(byte[])}
     */
    @Test
    public void test_read_array() throws Exception {
        byte[] buffer = new byte[3];

        assertEquals(3, overflow.available(), "#0 available()");
        assertEquals(3, overflow.read(buffer), "Unexpected Size");
        assertArrayEquals("Hel".getBytes(StandardCharsets.UTF_8), buffer, "Unexpected Data");

        assertEquals(0, overflow.available(), "#1 available()");
        assertThrows(IOException.class, () -> overflow.read(buffer));
    }


    /**
     * Unit test {@link LimitedInputStream#read(byte[], int, int)}
     */
    @Test
    public void test_read_arrayWithOffset() throws Exception {
        byte[] buffer = new byte[3];

        assertEquals(3, overflow.available(), "#0 available()");
        assertEquals(1, overflow.read(buffer, 0, 1), "Unexpected Size#1");
        assertEquals(2, overflow.available(), "#1 available()");
        assertEquals(2, overflow.read(buffer, 1, 2), "Unexpected Size#2");
        assertArrayEquals("Hel".getBytes(StandardCharsets.UTF_8), buffer, "Unexpected Data");
        assertEquals(0, overflow.available(), "#3 available()");
        assertThrows(IOException.class, () -> overflow.read(buffer, 0, 1));
    }


    /**
     * Unit test {@link LimitedInputStream#readNBytes(int)}
     */
    @Test
    public void test_readNBytes() throws Exception {
        byte[] buffer = overflow.readNBytes(2);

        assertEquals(1, overflow.available(), "#0 available()");
        assertArrayEquals("He".getBytes(StandardCharsets.UTF_8), buffer, "Unexpected Data");
        assertThrows(IOException.class, () -> overflow.readNBytes(2));
    }


    /**
     * Unit test {@link LimitedInputStream#readNBytes(byte[], int, int)}
     */
    @Test
    public void test_readNBytes_withOffset() throws Exception {
        byte[] buffer = new byte[3];

        assertEquals(1, overflow.readNBytes(buffer, 0, 1), "Unexpected Size#1");
        assertEquals(2, overflow.readNBytes(buffer, 1, 2), "Unexpected Size#2");
        assertArrayEquals("Hel".getBytes(StandardCharsets.UTF_8), buffer, "Unexpected Data");
        assertThrows(IOException.class, () -> overflow.readNBytes(buffer, 0, 1));
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_readAllBytes_underflow() throws Exception {
        byte[] actual = underflow.readAllBytes();

        assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), actual, "Unexpected Data");
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_readAllBytes_overflow() {
        assertThrows(IOException.class, () -> overflow.readAllBytes());
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skip() throws Exception {
        assertEquals(2, overflow.skip(2), "Unexpected skip");

        assertEquals(1, overflow.available(), "#1 available()");
        assertEquals((byte) 'l', overflow.read(), "Unexpected Char");

        assertEquals(0, overflow.available(), "#2 available()");
        assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skip_beyondLimit() throws Exception {
        assertEquals(3, overflow.skip(4), "Unexpected skip");

        assertEquals(0, overflow.available(), "available()");
        assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skipNBytes() throws Exception {
        overflow.skipNBytes(2);

        assertEquals(1, overflow.available(), "#1 available()");
        assertEquals((byte) 'l', overflow.read(), "Unexpected Char");

        assertEquals(0, overflow.available(), "#2 available()");
        assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skipNBytes_beyondLimit() throws Exception {
        overflow.skipNBytes(4);

        assertEquals(0, overflow.available(), "available()");
        assertThrows(IOException.class, () -> overflow.read());
    }


    /**
     * Unit test {@link LimitedInputStream#transferTo(OutputStream)}
     */
    @Test
    public void test_transferTo_underflow() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        underflow.transferTo(out);

        assertEquals("Hello", out.toString(StandardCharsets.UTF_8), "Unexpected data");
    }

    /**
     * Unit test {@link LimitedInputStream#transferTo(OutputStream)}
     */
    @Test
    public void test_transferTo_overflow() {
        assertThrows(IOException.class, () -> overflow.transferTo(new ByteArrayOutputStream()));
    }

    /**
     * Unit test {@link LimitedInputStream#close()}
     */
    @Test
    public void test_close() throws IOException {
        InputStream backing = mock(InputStream.class);
        InputStream stream = new LimitedInputStream(EelContext.factory().withIoLimit(1).build(), backing);

        stream.close();

        verify(backing).close();
    }
}