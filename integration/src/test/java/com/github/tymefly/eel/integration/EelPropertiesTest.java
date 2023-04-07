package com.github.tymefly.eel.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Unit test for {@link EelProperties}
 */
public class EelPropertiesTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule().enableLog().muteForSuccessfulTests();


    /**
     * Unit test {@link EelProperties#load(InputStream)}
     */
    @Test
    public void test_load_stream() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/sample.properties");
        Properties expected = new Properties();

        expected.setProperty("date", "20010203T0405");
        expected.setProperty("fixed", "Hello World");
        expected.setProperty("root", "/root/3/data");
        expected.setProperty("sub1", "/root/3/data/sub1");
        expected.setProperty("sub2", "/root/3/data/sub2");
        expected.setProperty("log", "/root/3/data/log/2001/02/03/04/");
        expected.setProperty("math", "3.141592920353982");

        Properties actual = new EelProperties().load(stream);

        Assert.assertEquals("Unexpected properties loaded", expected, actual);
    }

    /**
     * Unit test {@link EelProperties#load(InputStream)}
     */
    @Test
    public void test_load_reader() throws IOException {
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

        Properties actual = new EelProperties().load(reader);

        Assert.assertEquals("Unexpected properties loaded", expected, actual);
    }


    /**
     * Unit test {@link EelProperties#load(InputStream)}
     */
    @Test
    public void test_load_reader_customContext() throws IOException {
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

        Properties actual = new EelProperties(context).load(reader);

        Assert.assertEquals("Unexpected properties loaded", expected, actual);
    }


    /**
     * Unit test {@link EelProperties#load(InputStream)}
     */
    @Test
    public void test_load_forwardReference() {
        InputStream stream = getClass().getResourceAsStream("/forwards.properties");

        Assert.assertThrows(UnknownEelPropertyException.class, () -> new EelProperties().load(stream));
    }
}