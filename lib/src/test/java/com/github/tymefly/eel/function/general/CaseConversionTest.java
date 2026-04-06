package com.github.tymefly.eel.function.general;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link CaseConversion}
 */
public class CaseConversionTest {

    /**
     * Unit test {@link CaseConversion#upper(String)}
     */
    @Test
    public void test_Upper() {
        assertEquals("", new CaseConversion().upper(""), "Empty String");
        assertEquals("ABC", new CaseConversion().upper("abc"), "already lower");
        assertEquals("ABC", new CaseConversion().upper("ABC"), "upper case");
        assertEquals("ABC ABC 123 @+2", new CaseConversion().upper("ABC abc 123 @+2"), "mixed types");
    }

    /**
     * Unit test {@link CaseConversion#lower(String)}
     */
    @Test
    public void test_Lower() {
        assertEquals("", new CaseConversion().lower(""), "Empty String");
        assertEquals("abc", new CaseConversion().lower("abc"), "already lower");
        assertEquals("abc", new CaseConversion().lower("ABC"), "upper case");
        assertEquals("abc abc 123 @+2", new CaseConversion().lower("ABC abc 123 @+2"), "mixed types");
    }

    /**
     * Unit test {@link CaseConversion#title(String)}
     */
    @Test
    public void test_title() {
        assertEquals("", new CaseConversion().title(""), "Empty String");
        assertEquals("Abc", new CaseConversion().title("abc"), "lower case");
        assertEquals("Abc", new CaseConversion().title("ABC"), "upper case");
        assertEquals("Abc Abc 123 @+2", new CaseConversion().title("ABC abc 123 @+2"), "mixed types");
    }
}