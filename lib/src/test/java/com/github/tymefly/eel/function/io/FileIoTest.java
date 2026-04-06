package com.github.tymefly.eel.function.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.tymefly.eel.EelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FileIo}
 */
public class FileIoTest {
    private EelContext context;
    private File shortFile;
    private File emptyFile;


    @BeforeEach
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
        assertThrows(FileNotFoundException.class, () -> new FileIo().head(context, new File("unknown.???"), 1));
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_readNothing() throws Exception {
        assertEquals("", new FileIo().head(context, shortFile, 0), "0 lines");
        assertEquals("", new FileIo().head(context, shortFile, -1), "negative lines");
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_empty() throws Exception {
        assertEquals("", new FileIo().head(context, emptyFile, 5), "read empty file");
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head() throws Exception {
        assertEquals("One", new FileIo().head(context, shortFile, 1), "1 line");
        assertEquals("One\nTwo", new FileIo().head(context, shortFile, 2), "2 lines");
        assertEquals("One\nTwo\nThree", new FileIo().head(context, shortFile, 3), "3 lines");
        assertEquals("One\nTwo\nThree\n", new FileIo().head(context, shortFile, 4), "4 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 5), "5 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 6), "6 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().head(context, shortFile, 7), "7 lines");
    }

    /**
     * Unit test {@link FileIo#head(EelContext, File, int)}
     */
    @Test
    public void test_head_beyondLimit() {
        when(context.getIoLimit())
            .thenReturn(2);

        assertThrows(IOException.class, () -> new FileIo().head(context, shortFile, 1));
    }



    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)}
     */
    @Test
    public void test_fail_missingFile() {
        assertThrows(FileNotFoundException.class, () -> new FileIo().tail(context, new File("unknown.???"), 1));
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_readNothing() throws Exception {
        assertEquals("", new FileIo().tail(context, shortFile, 0), "0 lines");
        assertEquals("", new FileIo().tail(context, shortFile, -1), "negative lines");
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_empty() throws Exception {
        assertEquals("", new FileIo().tail(context, emptyFile, 5), "read empty file");
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail() throws Exception {
        assertEquals("Five", new FileIo().tail(context, shortFile, 1), "1 line");
        assertEquals("\nFive", new FileIo().tail(context, shortFile, 2), "2 lines");
        assertEquals("Three\n\nFive", new FileIo().tail(context, shortFile, 3), "3 lines");
        assertEquals("Two\nThree\n\nFive", new FileIo().tail(context, shortFile, 4), "4 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 5), "5 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 6), "6 lines");
        assertEquals("One\nTwo\nThree\n\nFive", new FileIo().tail(context, shortFile, 7), "7 lines");
    }

    /**
     * Unit test {@link FileIo#tail(EelContext, File, int)} 
     */
    @Test
    public void test_tail_beyondLimit() {
        when(context.getIoLimit())
            .thenReturn(20);

        assertThrows(IOException.class, () -> new FileIo().tail(context, shortFile, 1));
    }
}
