package com.github.tymefly.eel.function.general;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.exception.EelConvertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link PrintF}
 */
public class PrintFTest {
    private ZonedDateTime date;
    private PrintF printf;


    @BeforeEach
    public void setUp() {
        printf = new PrintF();
        date = ZonedDateTime.of(2000, 2, 3, 4, 5, 6, 123_456_789, ZoneOffset.UTC);
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_plainText() {
        assertEquals("Hello World", printf.printf("Hello World"), "Example1");
    }


    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_percent_conversion() {
        assertEquals("%", printf.printf("%%"), "Example1");
        assertEquals("Percent sign is %", printf.printf("Percent sign is %%"), "Example2");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_s_conversion() {
        Value arg = Value.of("hello, world");

        assertEquals(":hello, world:", printf.printf(":%s:", arg), "Example1");
        assertEquals(":hello, world:", printf.printf(":%10s:", arg), "Example2");
        assertEquals(":hello, wor:", printf.printf(":%.10s:", arg), "Example3");
        assertEquals(":hello, world:", printf.printf(":%-10s:", arg), "Example4");
        assertEquals(":hello, world:", printf.printf(":%.15s:", arg), "Example5");
        assertEquals(":hello, world   :", printf.printf(":%-15s:", arg), "Example6");
        assertEquals(":     hello, wor:", printf.printf(":%15.10s:", arg), "Example7");
        assertEquals(":hello, wor     :", printf.printf(":%-15.10s:", arg), "Example8");
    }


    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_c_conversion() {
        Value arg = Value.of("###");

        assertEquals(":#:", printf.printf(":%c:", arg), "Example1");
        assertEquals(":  #:", printf.printf(":%3c:", arg), "Example2");
        assertEquals(":#  :", printf.printf(":%-3c:", arg), "Example3");
        assertEquals(":#    :", printf.printf(":%-5c:", arg), "Example4");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_d_conversion() {
        Value arg = Value.of(12.3);

        assertEquals(":12:", printf.printf(":%d:", arg), "Example1");
        assertEquals(": 12:", printf.printf(":%3d:", arg), "Example2");
        assertEquals(":12 :", printf.printf(":%-3d:", arg), "Example3");
        assertEquals(":12   :", printf.printf(":%-5d:", arg), "Example4");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_f_conversion() {
        Value arg = Value.of(12.3);

        assertEquals(":12.300000:", printf.printf(":%f:", arg), "Example1");
        assertEquals(":12.300000:", printf.printf(":%3f:", arg), "Example2");
        assertEquals(":12.300:", printf.printf(":%.3f:", arg), "Example3");
        assertEquals(":12.300000:", printf.printf(":%-3f:", arg), "Example4");
        assertEquals(":12.30000:", printf.printf(":%.5f:", arg), "Example5");
        assertEquals(":12.300000:", printf.printf(":%-5f:", arg), "Example6");
        assertEquals(":12.300:", printf.printf(":%5.3f:", arg), "Example7");
        assertEquals(":12.300:", printf.printf(":%-5.3f:", arg), "Example8");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_t_conversion() {
        Value arg = Value.of(date);

        assertEquals(":04:05:06:", printf.printf(":%tT:", arg), "Example1");
        assertEquals(":2000-02-03:", printf.printf(":%tF:", arg), "Example1");
        assertEquals(":Year = 2000, Month = 02 day = 3:", printf.printf(":Year = %1$tY, Month = %1$tm day = %1$te:", arg), "Example3");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_b_conversion() {
        Value arg = Value.of(true);

        assertEquals(":true:", printf.printf(":%b:", arg), "Example1");
        assertEquals(":true:", printf.printf(":%3b:", arg), "Example2");
        assertEquals(":tru:", printf.printf(":%.3b:", arg), "Example3");
        assertEquals(":true:", printf.printf(":%-3b:", arg), "Example4");
        assertEquals(":true:", printf.printf(":%.7b:", arg), "Example5");
        assertEquals(":true   :", printf.printf(":%-7b:", arg), "Example6");
        assertEquals(":    tru:", printf.printf(":%7.3b:", arg), "Example7");
        assertEquals(":tru    :", printf.printf(":%-7.3b:", arg), "Example8");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_multipleTypes() {
        Value arg1 = Value.of(12.3);
        Value arg2 = Value.of(true);
        Value arg3 = Value.of("Hello World");
        Value arg4 = Value.of(date);

        assertEquals("Result: 0012% - true\nHello World_04:05:06\n", printf.printf("Result: %04d%% - %b%n%s_%tT%n", arg1, arg2, arg3, arg4).replace("\r", ""), "Example1");
    }


    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_malformedSpecifier() {
        assertThrows(UnknownFormatConversionException.class,
            () -> printf.printf("Hello %@"),
            "Bad specifier");

        assertThrows(UnknownFormatConversionException.class,
            () -> printf.printf("42%"),
            "Truncated");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_missingArguments() {
        assertThrows(MissingFormatArgumentException.class,
            () -> printf.printf("%d %d", Value.of(12.3)),
            "Expecting two numbers");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_badConversionChar() {
        assertThrows(UnknownFormatConversionException.class,
            () -> printf.printf("%z", Value.of(12.3)),
            "'z' is not a valid conversion type");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_emptyStringToChar() {
        assertThrows(EelConvertException.class,
            () -> printf.printf("Empty String: %c", Value.of("")),"Can't convert empty string to char");
    }

    /**
     * Unit test {@link PrintF#printf(String, Value...)}
     */
    @Test
    public void test_BadEelType() {
        assertThrows(EelConvertException.class,
            () -> printf.printf("%d", Value.of("Hello")),
            "Text can not be converted to number");
    }
}