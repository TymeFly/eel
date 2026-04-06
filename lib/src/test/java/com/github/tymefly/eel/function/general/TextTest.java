package com.github.tymefly.eel.function.general;

import com.github.tymefly.eel.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link Text}
 */
public class TextTest {
    private final Text text = new Text();
    private final Value minus1 = Value.of(-1);
    private final Value zero = Value.of(0);


    /**
     * Unit test {@link Text#left(String, int)}
     */
    @Test
    public void test_left() {
        assertEquals("abc", text.left("abcdef", 3), "Happy Path");
        assertEquals("abcdef", text.left("abcdef", 6), "full string");
        assertEquals("abcdef", text.left("abcdef", 7), "overflow");
        assertEquals("", text.left("abcdef", 0), "empty");
        assertEquals("abcd", text.left("abcdef", -2), "negative size");
    }

    /**
     * Unit test {@link Text#right(String, int)}
     */
    @Test
    public void test_right() {
        assertEquals("def", text.right("abcdef", 3), "Happy Path");
        assertEquals("abcdef", text.right("abcdef", 6), "full string");
        assertEquals("abcdef", text.right("abcdef", 7), "overflow");
        assertEquals("", text.right("abcdef", 0), "empty");
        assertEquals("cdef", text.right("abcdef", -2), "negative size");
    }

    /**
     * Unit test {@link Text#mid(String, int, int)}
     */
    @Test
    public void test_mid() {
        assertEquals("bc", text.mid("abcdef", 1, 2), "Happy Path");
        assertEquals("", text.mid("abcdef", 0, 0), "no chars");
        assertEquals("a", text.mid("abcdef", 0, 1), "single char");
        assertEquals("abcdef", text.mid("abcdef", 0, 6), "full string");
        assertEquals("f", text.mid("abcdef", -1, 6), "overflow");
        assertEquals("", text.mid("abcdef", 4, -2), "negative length");
    }


    /**
     * Unit test {@link Text#beforeFirst(String, String)}
     */
    @Test
    public void test_beforeFirst() {
        assertEquals("", text.beforeFirst("", "~"), "Empty Text");
        assertEquals("", text.beforeFirst("abcdef", ""), "Empty delimiter");
        assertEquals("abcdef", text.beforeFirst("abcdef", "~"), "No delimiter");
        assertEquals("abc", text.beforeFirst("abc~def", "~"), "One delimiter");
        assertEquals("ab", text.beforeFirst("ab~cd~ef", "~"), "Two delimiters");
        assertEquals("", text.beforeFirst("~abcdef", "~"), "Delimiter at start");
        assertEquals("", text.beforeFirst("~~abcdef", "~"), "Two Delimiters at start");
        assertEquals("abcdef", text.beforeFirst("abcdef~", "~"), "Delimiter at end");

        assertEquals("ab", text.beforeFirst("ab<=>cd<=>ef", "<=>"), "Long delimiter");
        assertEquals("", text.beforeFirst("ab~cd~ef", ""), "Empty delimiter");
    }

    /**
     * Unit test {@link Text#beforeLast(String, String)}
     */
    @Test
    public void test_beforeLast() {
        assertEquals("", text.beforeLast("", "~"), "Empty Text");
        assertEquals("abcdef", text.beforeLast("abcdef", ""), "Empty delimiter");
        assertEquals("abcdef", text.beforeLast("abcdef", "~"), "No delimiter");
        assertEquals("abc", text.beforeLast("abc~def", "~"), "One delimiter");
        assertEquals("ab~cd", text.beforeLast("ab~cd~ef", "~"), "Two delimiters");
        assertEquals("", text.beforeLast("~abcdef", "~"), "Delimiter at start");
        assertEquals("~", text.beforeLast("~~abcdef", "~"), "Two Delimiters at start");
        assertEquals("abcdef", text.beforeLast("abcdef~", "~"), "Delimiter at end");

        assertEquals("ab<=>cd", text.beforeLast("ab<=>cd<=>ef", "<=>"), "Long Delimiter");
        assertEquals("ab~cd~ef", text.beforeLast("ab~cd~ef", ""), "Empty delimiter");
    }

    /**
     * Unit test {@link Text#afterFirst(String, String)}
     */
    @Test
    public void test_afterFirst() {
        assertEquals("", text.afterFirst("", "~"), "Empty Text");
        assertEquals("abcdef", text.afterFirst("abcdef", ""), "Empty delimiter");
        assertEquals("", text.afterFirst("abcdef", "~"), "No delimiter");
        assertEquals("def", text.afterFirst("abc~def", "~"), "One delimiter");
        assertEquals("cd~ef", text.afterFirst("ab~cd~ef", "~"), "Two delimiters");
        assertEquals("abcdef", text.afterFirst("~abcdef", "~"), "Delimiter at start");
        assertEquals("~abcdef", text.afterFirst("~~abcdef", "~"), "Two Delimiters at start");
        assertEquals("", text.afterFirst("abcdef~", "~"), "Delimiter at end");

        assertEquals("cd<=>ef", text.afterFirst("ab<=>cd<=>ef", "<=>"), "Long Delimiter");
        assertEquals("ab~cd~ef", text.afterFirst("ab~cd~ef", ""), "Empty delimiter");
    }

    /**
     * Unit test {@link Text#afterLast(String, String)}
     */
    @Test
    public void test_afterLast() {
        assertEquals("", text.afterLast("", "~"), "Empty Text");
        assertEquals("", text.afterLast("abcdef", ""), "Empty delimiter");
        assertEquals("", text.afterLast("abcdef", "~"), "No delimiter");
        assertEquals("def", text.afterLast("abc~def", "~"), "One delimiter");
        assertEquals("ef", text.afterLast("ab~cd~ef", "~"), "Two delimiters");
        assertEquals("abcdef", text.afterLast("~~abcdef", "~"), "Two Delimiters at start");
        assertEquals("abcdef", text.afterLast("~abcdef", "~"), "Delimiter at start");
        assertEquals("", text.afterLast("abcdef~", "~"), "Delimiter at end");

        assertEquals("ef", text.afterLast("ab<=>cd<=>ef", "<=>"), "Long Delimiter");
        assertEquals("", text.afterLast("ab~cd~ef", ""), "Empty delimiter");
    }

    /**
     * Unit test {@link Text#before(String, String, int)}
     */
    @Test
    public void test_before() {
        assertEquals("", text.before("", "~", 1), "Empty Text");
        assertEquals("", text.before("abcdef", "", 1), "Empty delimiter");
        assertEquals("abcdef", text.before("abcdef", "~", 1), "No delimiter");
        assertEquals("abc", text.before("abc~def", "~", 1), "One delimiter");
        assertEquals("ab", text.before("ab~cd~ef", "~", 1), "Two delimiters");
        assertEquals("", text.before("~abcdef", "~", 1), "Delimiter at start");
        assertEquals("", text.before("~~abcdef", "~", 1), "Two Delimiters at start");
        assertEquals("abcdef", text.before("abcdef~", "~", 1), "Delimiter at end");

        assertEquals("ab<=><=>cd", text.before("ab<=><=>cd<=>ef<=>gh", "<=>", 3), "Long Delimiter");
        assertEquals("ab@@@@cd", text.before("ab@@@@cd@@ef@@gh", "@@", 3), "Repeating Delimiter");

        assertEquals("", text.before("~~ab~~cd~~ef", "~~", 0), "count = 0");
        assertEquals("", text.before("~~ab~~cd~~ef", "~~", 1), "count = 1");
        assertEquals("~~ab", text.before("~~ab~~cd~~ef", "~~", 2), "count = 2");
        assertEquals("~~ab~~cd", text.before("~~ab~~cd~~ef", "~~", 3), "count = 3");
        assertEquals("~~ab~~cd~~ef", text.before("~~ab~~cd~~ef", "~~", 4), "count = 4");
        assertEquals("~~ab~~cd~~ef", text.before("~~ab~~cd~~ef", "~~", 5), "count = 5");

        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
            () -> text.before("~~ab~~cd~~ef", "~~", -1),
            "Negative Count");
        assertEquals("Invalid index: -1", actual.getMessage(), "Unexpected error");
    }

    /**
     * Unit test {@link Text#after(String, String, int)}
     */
    @Test
    public void test_after() {
        assertEquals("", text.after("", "~", 1), "Empty Text");
        assertEquals("abcdef", text.after("abcdef", "", 1), "Empty delimiter");
        assertEquals("", text.after("abcdef", "~", 1), "No delimiter");
        assertEquals("def", text.after("abc~def", "~", 1), "One delimiter");
        assertEquals("cd~ef", text.after("ab~cd~ef", "~", 1), "Two delimiters");
        assertEquals("abcdef", text.after("~abcdef", "~", 1), "Delimiter at start");
        assertEquals("~abcdef", text.after("~~abcdef", "~", 1), "Two Delimiters at start");
        assertEquals("", text.after("abcdef~", "~", 1), "Delimiter at end");

        assertEquals("ef<=>gh", text.after("ab<=><=>cd<=>ef<=>gh", "<=>", 3), "Long Delimiter");
        assertEquals("ef@@gh", text.after("ab@@@@cd@@ef@@gh", "@@", 3), "Repeating Delimiter");

        assertEquals("~~ab~~cd~~ef", text.after("~~ab~~cd~~ef", "~~", 0), "count = 0");
        assertEquals("ab~~cd~~ef", text.after("~~ab~~cd~~ef", "~~", 1), "count = 1");
        assertEquals("cd~~ef", text.after("~~ab~~cd~~ef", "~~", 2), "count = 2");
        assertEquals("ef", text.after("~~ab~~cd~~ef", "~~", 3), "count = 3");
        assertEquals("", text.after("~~ab~~cd~~ef", "~~", 4), "count = 4");
        assertEquals("", text.after("~~ab~~cd~~ef", "~~", 5), "count = 5");

        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
            () -> text.after("~~ab~~cd~~ef", "~~", -1),
            "Negative Count");
        assertEquals("Invalid index: -1", actual.getMessage(), "Unexpected error");
    }

    /**
     * Unit test {@link Text#between(String, String, int, int)}
     */
    @Test
    public void test_between() {
        assertEquals("", text.between("", "~", 1, 2), "Empty Text");
        assertEquals("", text.between("abcdef", "", 1, 2), "Empty delimiter");
        assertEquals("def", text.between("abc~def", "~", 1, 2), "One delimiter");
        assertEquals("cd", text.between("ab~cd~ef", "~", 1, 2), "Two delimiters");
        assertEquals("abcdef", text.between("~abcdef", "~", 1, 2), "Delimiter at start");
        assertEquals("", text.between("~~abcdef", "~", 1, 2), "Two Delimiters at start");
        assertEquals("", text.between("abcdef~", "~", 1, 2), "Delimiter at end");

        assertEquals("ef<=>gh", text.between("ab<=><=>cd<=>ef<=>gh", "<=>", 3, 5), "Long Delimiter");
        assertEquals("ef@@gh", text.between("ab@@@@cd@@ef@@gh", "@@", 3, 5), "Repeating Delimiter");

        assertEquals("", text.between("~~ab~~cd~~ef", "~~", 0, 1), "0 to 1");
        assertEquals("ab", text.between("~~ab~~cd~~ef", "~~", 1, 2), "1 to 2");
        assertEquals("cd", text.between("~~ab~~cd~~ef", "~~", 2, 3), "2 to 3");
        assertEquals("ef", text.between("~~ab~~cd~~ef", "~~", 3, 4), "3 to 4");
        assertEquals("", text.between("~~ab~~cd~~ef", "~~", 4, 5), "4 to 5");

        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
            () -> text.between("~~ab~~cd~~ef", "~~", -1, 2),
            "Negative Index");
        assertEquals("Invalid index: -1", actual.getMessage(), "Unexpected error");
    }

    /**
     * Unit test {@link Text#contains(String, String)} 
     */
    @Test
    public void contains() {
        assertEquals(0, text.contains("", ""), "Empty Text and and Empty delimiter");
        assertEquals(12, text.contains("ab~~cd~ef~gh", ""), "With Text and and Empty delimiter");
        assertEquals(4, text.contains("ab~~cd~ef~gh", "~"), "Short Delimiter");
        assertEquals(4, text.contains("ab<=><=>cd<=>ef<=>gh", "<=>"), "Long Delimiter");
        assertEquals(4, text.contains("ab@@@@cd@@ef@@gh", "@@"), "Repeating Delimiter");
    }



    /**
     * Unit test {@link Text#extract(String, String)}
     */
    @Test
    public void test_extract() {
        assertEquals("", text.extract("Hello World", ".*~(.*)~.*"), "Not Found");
        assertEquals("capture", text.extract("Hello ~capture~ World", ".*~(.*)~.*"), "Found");
        assertEquals("FirstSecondThird", text.extract("prefix /First/Second/Third/ postfix", ".*/(.*)/(.*)/(.*)/.*"), "multiple groups");
    }


    /**
     * Unit test {@link Text#matches(String, String)}
     */
    @Test
    public void test_matches() {
        assertFalse(text.matches("Hello World", ".*~(.*)~.*"), "Not Found");
        assertTrue(text.matches("Hello ~find me~ World", ".*~(.*)~.*"), "Found");
    }

    /**
     * Unit test {@link Text#replace(String, String, String)}
     */
    @Test
    public void test_replace() {
        assertEquals("", text.replace("", " ", "#"), "Empty String");
        assertEquals("Hello#World#!", text.replace("Hello World !", " ", "#"), "single char replacement");
        assertEquals("Hello__World__!", text.replace("Hello World !", " ", "__"), "two char replacement");
        assertEquals(" World !", text.replace("Hello World !", "Hello", ""), "Word removal");
    }

    /**
     * Unit test {@link Text#replaceEx(String, String, String)}
     */
    @Test
    public void test_replaceEx() {
        assertEquals("", text.replaceEx("", " ", "#"), "Empty String");
        assertEquals("Hello#World#!", text.replaceEx("Hello World !", " ", "#"), "single char replacement");
        assertEquals("Hello__World__!", text.replaceEx("Hello World !", " ", "__"), "two char replacement");
        assertEquals(" World !", text.replaceEx("Hello World !", "Hello", ""), "Word removal");
        assertEquals("ello orld !", text.replaceEx("Hello World !", "[A-Z]", ""), "replace uppers");
    }

    /**
     * Unit test {@link Text#trim(String)}
     */
    @Test
    public void test_trim() {
        assertEquals("", text.trim(""), "Empty String");
        assertEquals("abc", text.trim("abc"), "No White Space");
        assertEquals("ab cd", text.trim("ab cd"), "Internal Space");
        assertEquals("ab\tcd", text.trim("ab\tcd"), "Internal Tab");
        assertEquals("a", text.trim(" a"), "Leading Space");
        assertEquals("a", text.trim("a "), "Trailing Space");
        assertEquals("a", text.trim("\ta"), "Leading Tab");
        assertEquals("a", text.trim("a\t"), "Trailing Tab");
        assertEquals("ab \tcd", text.trim(" \t ab \tcd\t \t"), "Trailing and trailing spaces");
    }

    /**
     * Unit test {@link Text#isEmpty(String)}
     */
    @Test
    public void test_isEmpty() {
        assertTrue(text.isEmpty(""), "Empty String");
        assertFalse(text.isEmpty(" \t "), "Whitespace String");
        assertFalse(text.isEmpty("x"), "Non-empty String");
    }

    /**
     * Unit test {@link Text#isBlank(String)}
     */
    @Test
    public void test_isBlank() {
        assertTrue(text.isBlank(""), "Empty String");
        assertTrue(text.isBlank(" \t "), "Whitespace String");
        assertFalse(text.isBlank("x"), "Non-empty String");
    }


    /**
     * Unit test {@link Text#len(String)}
     */
    @Test
    public void test_len() {
        assertEquals(0, text.len(""), "Empty String");
        assertEquals(1, text.len("a"), "Single character");
        assertEquals(3, text.len(" \t "), "Whitespace");
        assertEquals(4, text.len("abcd"), "Multiple characters");
    }

    /**
     * Unit test {@link Text#indexOf(String, String, Value)}
     */
    @Test
    public void test_indexOf() {
        assertEquals(-1, text.indexOf("", "cd", minus1), "Empty String");
        assertEquals(0, text.indexOf("abcdefabcdef", "", minus1), "Empty search");
        assertEquals(2, text.indexOf("abcdefabcdef", "cd", minus1), "Found");
        assertEquals(-1, text.indexOf("abcdefabcdef", "dc", minus1), "#1 Not found");
        assertEquals(0, text.indexOf("abcdefabcdef", "dc", zero), "#2 Not found");
    }

    /**
     * Unit test {@link Text#lastIndexOf(String, String, Value)}
     */
    @Test
    public void test_lastIndexOf() {
        assertEquals(-1, text.lastIndexOf("", "cd", minus1), "Empty String");
        assertEquals(12, text.lastIndexOf("abcdefabcdef", "", minus1), "Empty search");
        assertEquals(8, text.lastIndexOf("abcdefabcdef", "cd", minus1), "Found");
        assertEquals(-1, text.lastIndexOf("abcdefabcdef", "dc", minus1), "#1 Not found");
        assertEquals(0, text.lastIndexOf("abcdefabcdef", "dc", zero), "#2 Not found");
    }


    /**
     * Unit test {@link Text#nthIndexOf(String, String, int, Text.Direction)} 
     */
    @Test
    public void test_nthIndexOf_after() {
        assertEquals(0, text.nthIndexOf("", "~", 1, Text.Direction.AFTER), "empty text");
        assertEquals(0, text.nthIndexOf("ab~cd~ef~gh", "", 1, Text.Direction.AFTER), "empty delimiter");

        assertEquals(0, text.nthIndexOf("ab~cd~ef~gh", "~", 0, Text.Direction.AFTER), "0th element");
        assertEquals(3, text.nthIndexOf("ab~cd~ef~gh", "~", 1, Text.Direction.AFTER), "1st element");
        assertEquals(6, text.nthIndexOf("ab~cd~ef~gh", "~", 2, Text.Direction.AFTER), "2nd element");
        assertEquals(9, text.nthIndexOf("ab~cd~ef~gh", "~", 3, Text.Direction.AFTER), "3rd element");
        assertEquals(11, text.nthIndexOf("ab~cd~ef~gh", "~", 4, Text.Direction.AFTER), "4th element");
        assertEquals(11, text.nthIndexOf("ab~cd~ef~gh", "~", 5, Text.Direction.AFTER), "5th element");

        assertEquals(8, text.nthIndexOf("ab~.cd~.ef~.gh", "~.", 2, Text.Direction.AFTER), "Long delimiter");
        assertEquals(8, text.nthIndexOf("ab@@cd@@ef@@gh", "@@", 2, Text.Direction.AFTER), "Same char delimiter");
        assertEquals(9, text.nthIndexOf("ab@@@cd@@@ef@@@gh", "@@", 2, Text.Direction.AFTER), "extended delimiter");
    }

    /**
     * Unit test {@link Text#nthIndexOf(String, String, int, Text.Direction)}
     */
    @Test
    public void test_nthIndexOf_before() {
        assertEquals(0, text.nthIndexOf("", "~", 1, Text.Direction.BEFORE), "empty text");
        assertEquals(0, text.nthIndexOf("ab~cd~ef~gh", "", 1, Text.Direction.BEFORE), "empty delimiter");
        assertEquals(0, text.nthIndexOf("ab~cd~ef~gh", "~", 0, Text.Direction.BEFORE), "0th element");
        assertEquals(2, text.nthIndexOf("ab~cd~ef~gh", "~", 1, Text.Direction.BEFORE), "1st element");
        assertEquals(5, text.nthIndexOf("ab~cd~ef~gh", "~", 2, Text.Direction.BEFORE), "2nd element");
        assertEquals(8, text.nthIndexOf("ab~cd~ef~gh", "~", 3, Text.Direction.BEFORE), "3rd element");
        assertEquals(11, text.nthIndexOf("ab~cd~ef~gh", "~", 4, Text.Direction.BEFORE), "4th element");
        assertEquals(11, text.nthIndexOf("ab~cd~ef~gh", "~", 5, Text.Direction.BEFORE), "5th element");

        assertEquals(6, text.nthIndexOf("ab~.cd~.ef~.gh", "~.", 2, Text.Direction.BEFORE), "Long delimiter");
        assertEquals(6, text.nthIndexOf("ab@@cd@@ef@@gh", "@@", 2, Text.Direction.BEFORE), "Same char delimiter");
        assertEquals(7, text.nthIndexOf("ab@@@cd@@@ef@@@gh", "@@", 2, Text.Direction.BEFORE), "extended delimiter");
    }
}