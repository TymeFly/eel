package com.github.tymefly.eel.function.format;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link PrintF}
 */
public class PrintFTest {
    private ZonedDateTime date;
    private PrintF printf;


    @Before
    public void setUp() {
        printf = new PrintF();
         date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_plainText() {
        Assert.assertEquals("Example1", "Hello World", printf.printf("Hello World"));
    }


    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_percent_conversion() {
        Assert.assertEquals("Example1", "%", printf.printf("%%"));
        Assert.assertEquals("Example2", "Percent sign is %", printf.printf("Percent sign is %%"));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_s_conversion() {
        EelValue arg = EelValue.of("hello, world");

        Assert.assertEquals("Example1", ":hello, world:", printf.printf(":%s:", arg));
        Assert.assertEquals("Example2", ":hello, world:", printf.printf(":%10s:", arg));
        Assert.assertEquals("Example3", ":hello, wor:", printf.printf(":%.10s:", arg));
        Assert.assertEquals("Example4", ":hello, world:", printf.printf(":%-10s:", arg));
        Assert.assertEquals("Example5", ":hello, world:", printf.printf(":%.15s:", arg));
        Assert.assertEquals("Example6", ":hello, world   :", printf.printf(":%-15s:", arg));
        Assert.assertEquals("Example7", ":     hello, wor:", printf.printf(":%15.10s:", arg));
        Assert.assertEquals("Example8", ":hello, wor     :", printf.printf(":%-15.10s:", arg));
    }


    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_c_conversion() {
        EelValue arg = EelValue.of("###");

        Assert.assertEquals("Example1", ":#:", printf.printf(":%c:", arg));
        Assert.assertEquals("Example2", ":  #:", printf.printf(":%3c:", arg));
        Assert.assertEquals("Example3", ":#  :", printf.printf(":%-3c:", arg));
        Assert.assertEquals("Example4", ":#    :", printf.printf(":%-5c:", arg));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_d_conversion() {
        EelValue arg = EelValue.of(12.3);

        Assert.assertEquals("Example1", ":12:", printf.printf(":%d:", arg));
        Assert.assertEquals("Example2", ": 12:", printf.printf(":%3d:", arg));
        Assert.assertEquals("Example3", ":12 :", printf.printf(":%-3d:", arg));
        Assert.assertEquals("Example4", ":12   :", printf.printf(":%-5d:", arg));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_f_conversion() {
        EelValue arg = EelValue.of(12.3);

        Assert.assertEquals("Example1", ":12.300000:", printf.printf(":%f:", arg));
        Assert.assertEquals("Example2", ":12.300000:", printf.printf(":%3f:", arg));
        Assert.assertEquals("Example3", ":12.300:", printf.printf(":%.3f:", arg));
        Assert.assertEquals("Example4", ":12.300000:", printf.printf(":%-3f:", arg));
        Assert.assertEquals("Example5", ":12.30000:", printf.printf(":%.5f:", arg));
        Assert.assertEquals("Example6", ":12.300000:", printf.printf(":%-5f:", arg));
        Assert.assertEquals("Example7", ":12.300:", printf.printf(":%5.3f:", arg));
        Assert.assertEquals("Example8", ":12.300:", printf.printf(":%-5.3f:", arg));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_t_conversion() {
        EelValue arg = EelValue.of(date);

        Assert.assertEquals("Example1", ":04:05:06:", printf.printf(":%tT:", arg));
        Assert.assertEquals("Example1", ":2000-02-03:", printf.printf(":%tF:", arg));
        Assert.assertEquals("Example3",
            ":Year = 2000, Month = 02 day = 3:",
            printf.printf(":Year = %1$tY, Month = %1$tm day = %1$te:", arg));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_b_conversion() {
        EelValue arg = EelValue.of(true);

        Assert.assertEquals("Example1", ":true:", printf.printf(":%b:", arg));
        Assert.assertEquals("Example2", ":true:", printf.printf(":%3b:", arg));
        Assert.assertEquals("Example3", ":tru:", printf.printf(":%.3b:", arg));
        Assert.assertEquals("Example4", ":true:", printf.printf(":%-3b:", arg));
        Assert.assertEquals("Example5", ":true:", printf.printf(":%.7b:", arg));
        Assert.assertEquals("Example6", ":true   :", printf.printf(":%-7b:", arg));
        Assert.assertEquals("Example7", ":    tru:", printf.printf(":%7.3b:", arg));
        Assert.assertEquals("Example8", ":tru    :", printf.printf(":%-7.3b:", arg));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_multipleTypes() {
        EelValue arg1 = EelValue.of(12.3);
        EelValue arg2 = EelValue.of(true);
        EelValue arg3 = EelValue.of("Hello World");
        EelValue arg4 = EelValue.of(date);

        Assert.assertEquals("Example1",
            "Result: 0012% - true\nHello World_04:05:06\n",
            printf.printf("Result: %04d%% - %b%n%s_%tT%n", arg1, arg2, arg3, arg4).replace("\r", ""));
    }


    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_malformedSpecifier() {
        Assert.assertThrows("Bad specifier",
            UnknownFormatConversionException.class,
            () -> printf.printf("Hello %@"));

        Assert.assertThrows("Truncated",
            UnknownFormatConversionException.class,
            () -> printf.printf("42%"));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_missingArguments() {
        Assert.assertThrows("Expecting two numbers",
            MissingFormatArgumentException.class,
            () -> printf.printf("%d %d", EelValue.of(12.3)));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_badConversionChar() {
        Assert.assertThrows("'z' is not a valid conversion type",
            UnknownFormatConversionException.class,
            () -> printf.printf("%z", EelValue.of(12.3)));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_emptyStringToChar() {
        Assert.assertThrows("Can't convert empty string to char",
            EelConvertException.class,
            () -> printf.printf("Empty String: %c", EelValue.of("")));
    }

    /**
     * Unit test {@link PrintF#printf(String, EelValue...)}
     */
    @Test
    public void test_BadEelType() {
        Assert.assertThrows("Text can not be converted to number",
            EelConvertException.class,
            () -> printf.printf("%d", EelValue.of("Hello")));
    }
}