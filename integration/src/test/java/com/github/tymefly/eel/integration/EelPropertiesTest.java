package com.github.tymefly.eel.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link EelProperties}
 */
@ExtendWith(SystemStubsExtension.class)
class EelPropertiesTest {
    @SystemStub
    private SystemOut stdOut;

    @SystemStub
    private SystemErr stdErr;

    /**
     * Unit test {@link EelProperties#load(InputStream)}
     */
    @Test
    void test_load_stream() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/sample.properties");
        Properties expected = new Properties();

        expected.setProperty("date", "20010203T0405");
        expected.setProperty("fixed", "Hello World");
        expected.setProperty("root", "/root/3/data");
        expected.setProperty("sub1", "/root/3/data/sub1");
        expected.setProperty("sub2", "/root/3/data/sub2");
        expected.setProperty("log", "/root/3/data/log/2001/02/03/04/");
        expected.setProperty("math", "3.141592920353982");
        expected.setProperty("chained", "3");

        Properties actual = new EelProperties().load(stream);

        assertEquals(expected, actual, "Unexpected properties loaded");
    }

    /**
     * Unit test {@link EelProperties#load(Reader)}
     */
    @Test
    void test_load_reader() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/sample.properties");
        Reader reader = new InputStreamReader(stream);
        Properties expected = new Properties();

        expected.setProperty("date", "20010203T0405");
        expected.setProperty("fixed", "Hello World");
        expected.setProperty("root", "/root/3/data");
        expected.setProperty("sub1", "/root/3/data/sub1");
        expected.setProperty("sub2", "/root/3/data/sub2");
        expected.setProperty("log", "/root/3/data/log/2001/02/03/04/");
        expected.setProperty("math", "3.141592920353982");
        expected.setProperty("chained", "3");

        Properties actual = new EelProperties().load(reader);

        assertEquals(expected, actual, "Unexpected properties loaded");
    }

    /**
     * Unit test {@link EelProperties#load(Reader)} with custom EelContext
     */
    @Test
    void test_load_reader_customContext() throws IOException {
        EelContext context = EelContext.factory()
            .withPrecision(7)
            .build();
        InputStream stream = getClass().getResourceAsStream("/sample.properties");
        Reader reader = new InputStreamReader(stream);
        Properties expected = new Properties();

        expected.setProperty("date", "20010203T0405");
        expected.setProperty("fixed", "Hello World");
        expected.setProperty("root", "/root/3/data");
        expected.setProperty("sub1", "/root/3/data/sub1");
        expected.setProperty("sub2", "/root/3/data/sub2");
        expected.setProperty("log", "/root/3/data/log/2001/02/03/04/");
        expected.setProperty("math", "3.141593");
        expected.setProperty("chained", "3");

        Properties actual = new EelProperties(context).load(reader);

        assertEquals(expected, actual, "Unexpected properties loaded");
    }

    /**
     * Unit test {@link EelProperties#load(InputStream)} forward reference
     */
    @Test
    void test_load_forwardReference() throws Exception {
        try (
            InputStream stream = getClass().getResourceAsStream("/forwards.properties")
        ) {
            assertThrows(UnknownEelPropertyException.class, () -> new EelProperties().load(stream));
        }
    }
}
