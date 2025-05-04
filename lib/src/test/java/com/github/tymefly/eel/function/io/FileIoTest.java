package com.github.tymefly.eel.function.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.tymefly.eel.EelContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FileIo}
 */
public class FileIoTest {
    private EelContext context;
    private File shortFile;
    private File emptyFile;


    @Before
    public void setUp() throws Exception {
        context = spy(EelContext.factory().build());

        when(context.getIoLimit())
            .thenReturn(100);

        shortFile = new File(getClass().getClassLoader()
            .getResource("short.txt")
            .toURI());
        emptyFile = new File(getClass().getClassLoader()
            .getResource("empty.txt")
            .toURI());
    }


    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_missingFile() {
        Assert.assertThrows(FileNotFoundException.class, () -> new FileIo().head(context, new File("unknown.???"), 1));
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_readNothing() throws Exception {
        Assert.assertEquals("0 lines", "", new FileIo().head(context, shortFile, 0));
        Assert.assertEquals("negative lines", "", new FileIo().head(context, shortFile, -1));
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_empty() throws Exception {
        Assert.assertEquals("read empty file", "", new FileIo().head(context, emptyFile, 5));
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head() throws Exception {
        Assert.assertEquals("1 line", "One", new FileIo().head(context, shortFile, 1));
        Assert.assertEquals("2 lines", "One\nTwo", new FileIo().head(context, shortFile, 2));
        Assert.assertEquals("3 lines", "One\nTwo\nThree", new FileIo().head(context, shortFile, 3));
        Assert.assertEquals("4 lines", "One\nTwo\nThree\n", new FileIo().head(context, shortFile, 4));
        Assert.assertEquals("5 lines", "One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 5));
        Assert.assertEquals("6 lines", "One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 6));
        Assert.assertEquals("7 lines", "One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 7));
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_beyondLimit() {
        when(context.getIoLimit())
            .thenReturn(2);

        Assert.assertThrows(IOException.class, () -> new FileIo().head(context, shortFile, 1));
    }



    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)}
     */
    @Test
    public void test_fail_missingFile() {
        Assert.assertThrows(FileNotFoundException.class, () -> new FileIo().tail(context, new File("unknown.???"), 1));
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_readNothing() throws Exception {
        Assert.assertEquals("0 lines", "", new FileIo().tail(context, shortFile, 0));
        Assert.assertEquals("negative lines", "", new FileIo().tail(context, shortFile, -1));
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_empty() throws Exception {
        Assert.assertEquals("read empty file", "", new FileIo().tail(context, emptyFile, 5));
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail() throws Exception {
        Assert.assertEquals("1 line", "Five", new FileIo().tail(context, shortFile, 1));
        Assert.assertEquals("2 lines", "\nFive", new FileIo().tail(context, shortFile, 2));
        Assert.assertEquals("3 lines", "Three\n\nFive", new FileIo().tail(context, shortFile, 3));
        Assert.assertEquals("4 lines", "Two\nThree\n\nFive", new FileIo().tail(context, shortFile, 4));
        Assert.assertEquals("5 lines", "One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 5));
        Assert.assertEquals("6 lines", "One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 6));
        Assert.assertEquals("7 lines", "One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 7));
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_beyondLimit() {
        when(context.getIoLimit())
            .thenReturn(20);

        Assert.assertThrows(IOException.class, () -> new FileIo().tail(context, shortFile, 1));
    }

}
