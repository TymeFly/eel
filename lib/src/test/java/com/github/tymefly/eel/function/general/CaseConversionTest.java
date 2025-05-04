package com.github.tymefly.eel.function.general;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link CaseConversion}
 */
public class CaseConversionTest {

    /**
     * Unit test {@link CaseConversion#upper(String)}
     */
    @Test
    public void test_Upper() {
        Assert.assertEquals("Empty String", "", new CaseConversion().upper(""));
        Assert.assertEquals("already lower", "ABC", new CaseConversion().upper("abc"));
        Assert.assertEquals("upper case", "ABC", new CaseConversion().upper("ABC"));
        Assert.assertEquals("mixed types", "ABC ABC 123 @+2", new CaseConversion().upper("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link CaseConversion#lower(String)}
     */
    @Test
    public void test_Lower() {
        Assert.assertEquals("Empty String", "", new CaseConversion().lower(""));
        Assert.assertEquals("already lower", "abc", new CaseConversion().lower("abc"));
        Assert.assertEquals("upper case", "abc", new CaseConversion().lower("ABC"));
        Assert.assertEquals("mixed types", "abc abc 123 @+2", new CaseConversion().lower("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link CaseConversion#title(String)}
     */
    @Test
    public void test_title() {
        Assert.assertEquals("Empty String", "", new CaseConversion().title(""));
        Assert.assertEquals("lower case", "Abc", new CaseConversion().title("abc"));
        Assert.assertEquals("upper case", "Abc", new CaseConversion().title("ABC"));
        Assert.assertEquals("mixed types", "Abc Abc 123 @+2", new CaseConversion().title("ABC abc 123 @+2"));
    }
}