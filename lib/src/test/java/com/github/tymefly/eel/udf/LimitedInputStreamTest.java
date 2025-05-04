package com.github.tymefly.eel.udf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link LimitedInputStream}
 */
public class LimitedInputStreamTest {
    private LimitedInputStream overflow;
    private LimitedInputStream underflow;


    @Before
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
        Assert.assertEquals("#0 available()", 5, underflow.available());
        Assert.assertEquals("Unexpected Char 1", (byte) 'H', underflow.read());
        Assert.assertEquals("#1 available()", 4, underflow.available());
        Assert.assertEquals("Unexpected Char 2", (byte) 'e', underflow.read());
        Assert.assertEquals("#2 available()", 3, underflow.available());
        Assert.assertEquals("Unexpected Char 3", (byte) 'l', underflow.read());
        Assert.assertEquals("#3 available()", 2, underflow.available());
        Assert.assertEquals("Unexpected Char 4", (byte) 'l', underflow.read());
        Assert.assertEquals("#4 available()", 1, underflow.available());
        Assert.assertEquals("Unexpected Char 5", (byte) 'o', underflow.read());
        Assert.assertEquals("#5 available()", 0, underflow.available());
        Assert.assertEquals("Unexpected Char 5", -1, underflow.read());
        Assert.assertEquals("#6 available()", 0, underflow.available());
    }

    /**
     * Unit test {@link LimitedInputStream#read()}
     */
    @Test
    public void test_read_byte_overflow() throws Exception {
        Assert.assertEquals("#0 available()", 3, overflow.available());
        Assert.assertEquals("Unexpected Char 1", (byte) 'H', overflow.read());
        Assert.assertEquals("#1 available()", 2, overflow.available());
        Assert.assertEquals("Unexpected Char 2", (byte) 'e', overflow.read());
        Assert.assertEquals("#2 available()", 1, overflow.available());
        Assert.assertEquals("Unexpected Char 3", (byte) 'l', overflow.read());
        Assert.assertEquals("#3 available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read());
    }


    /**
     * Unit test {@link LimitedInputStream#read(byte[])}
     */
    @Test
    public void test_read_array() throws Exception {
        byte[] buffer = new byte[3];

        Assert.assertEquals("#0 available()", 3, overflow.available());
        Assert.assertEquals("Unexpected Size", 3, overflow.read(buffer));
        Assert.assertArrayEquals("Unexpected Data", "Hel".getBytes(StandardCharsets.UTF_8), buffer);

        Assert.assertEquals("#1 available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read(buffer));
    }


    /**
     * Unit test {@link LimitedInputStream#read(byte[], int, int)}
     */
    @Test
    public void test_read_arrayWithOffset() throws Exception {
        byte[] buffer = new byte[3];

        Assert.assertEquals("#0 available()", 3, overflow.available());
        Assert.assertEquals("Unexpected Size#1", 1, overflow.read(buffer, 0, 1));
        Assert.assertEquals("#1 available()", 2, overflow.available());
        Assert.assertEquals("Unexpected Size#2", 2, overflow.read(buffer, 1, 2));
        Assert.assertArrayEquals("Unexpected Data", "Hel".getBytes(StandardCharsets.UTF_8), buffer);
        Assert.assertEquals("#3 available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read(buffer, 0, 1));
    }


    /**
     * Unit test {@link LimitedInputStream#readNBytes(int)}
     */
    @Test
    public void test_readNBytes() throws Exception {
        byte[] buffer = overflow.readNBytes(2);

        Assert.assertEquals("#0 available()", 1, overflow.available());
        Assert.assertArrayEquals("Unexpected Data", "He".getBytes(StandardCharsets.UTF_8), buffer);
        Assert.assertThrows(IOException.class, () -> overflow.readNBytes(2));
    }


    /**
     * Unit test {@link LimitedInputStream#readNBytes(byte[], int, int)}
     */
    @Test
    public void test_readNBytes_withOffset() throws Exception {
        byte[] buffer = new byte[3];

        Assert.assertEquals("Unexpected Size#1", 1, overflow.readNBytes(buffer, 0, 1));
        Assert.assertEquals("Unexpected Size#2", 2, overflow.readNBytes(buffer, 1, 2));
        Assert.assertArrayEquals("Unexpected Data", "Hel".getBytes(StandardCharsets.UTF_8), buffer);
        Assert.assertThrows(IOException.class, () -> overflow.readNBytes(buffer, 0, 1));
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_readAllBytes_underflow() throws Exception {
        byte[] actual = underflow.readAllBytes();

        Assert.assertArrayEquals("Unexpected Data", "Hello".getBytes(StandardCharsets.UTF_8), actual);
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_readAllBytes_overflow() {
        Assert.assertThrows(IOException.class, () -> overflow.readAllBytes());
    }


    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skip() throws Exception {
        Assert.assertEquals("Unexpected skip", 2, overflow.skip(2));

        Assert.assertEquals("#1 available()", 1, overflow.available());
        Assert.assertEquals("Unexpected Char", (byte) 'l', overflow.read());

        Assert.assertEquals("#2 available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skip_beyondLimit() throws Exception {
        Assert.assertEquals("Unexpected skip", 3, overflow.skip(4));

        Assert.assertEquals("available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skipNBytes() throws Exception {
        overflow.skipNBytes(2);

        Assert.assertEquals("#1 available()", 1, overflow.available());
        Assert.assertEquals("Unexpected Char", (byte) 'l', overflow.read());

        Assert.assertEquals("#2 available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read());
    }

    /**
     * Unit test {@link LimitedInputStream#readAllBytes()}
     */
    @Test
    public void test_skipNBytes_beyondLimit() throws Exception {
        overflow.skipNBytes(4);

        Assert.assertEquals("available()", 0, overflow.available());
        Assert.assertThrows(IOException.class, () -> overflow.read());
    }


    /**
     * Unit test {@link LimitedInputStream#transferTo(OutputStream)}
     */
    @Test
    public void test_transferTo_underflow() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        underflow.transferTo(out);

        Assert.assertEquals("Unexpected data", "Hello", out.toString(StandardCharsets.UTF_8));
    }

    /**
     * Unit test {@link LimitedInputStream#transferTo(OutputStream)}
     */
    @Test
    public void test_transferTo_overflow() {
        Assert.assertThrows(IOException.class,
                () -> overflow.transferTo(new ByteArrayOutputStream()));
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