package com.github.tymefly.eel.function.util;

import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.udf.EelLambda;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Text}
 */
public class TextTest {
    private Text text;
    private EelLambda minus1 = () -> EelValue.of(-1);
    private EelLambda zero = () -> EelValue.of(0);

    @Before
    public void setUp() {
        text = new Text();
    }

    
    /**
     * Unit test {@link Text#upper(String)}
     */
    @Test
    public void test_Upper() {
        Assert.assertEquals("Empty String", "", text.upper(""));
        Assert.assertEquals("already lower", "ABC", text.upper("abc"));
        Assert.assertEquals("upper case", "ABC", text.upper("ABC"));
        Assert.assertEquals("mixed types", "ABC ABC 123 @+2", text.upper("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#lower(String)}
     */
    @Test
    public void test_Lower() {
        Assert.assertEquals("Empty String", "", text.lower(""));
        Assert.assertEquals("already lower", "abc", text.lower("abc"));
        Assert.assertEquals("upper case", "abc", text.lower("ABC"));
        Assert.assertEquals("mixed types", "abc abc 123 @+2", text.lower("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#title(String)}
     */
    @Test
    public void test_title() {
        Assert.assertEquals("Empty String", "", text.title(""));
        Assert.assertEquals("lower case", "Abc", text.title("abc"));
        Assert.assertEquals("upper case", "Abc", text.title("ABC"));
        Assert.assertEquals("mixed types", "Abc Abc 123 @+2", text.title("ABC abc 123 @+2"));
    }

    /**
     * Unit test {@link Text#left(String, int)}
     */
    @Test
    public void test_left() {
        Assert.assertEquals("Happy Path", "abc", text.left("abcdef", 3));
        Assert.assertEquals("full string", "abcdef", text.left("abcdef", 6));
        Assert.assertEquals("overflow", "abcdef", text.left("abcdef", 7));
        Assert.assertEquals("empty", "", text.left("abcdef", 0));
        Assert.assertEquals("negative size", "", text.left("abcdef", -2));
    }

    /**
     * Unit test {@link Text#right(String, int)}
     */
    @Test
    public void test_right() {
        Assert.assertEquals("Happy Path", "def", text.right("abcdef", 3));
        Assert.assertEquals("full string", "abcdef", text.right("abcdef", 6));
        Assert.assertEquals("overflow", "abcdef", text.right("abcdef", 7));
        Assert.assertEquals("empty", "", text.right("abcdef", 0));
        Assert.assertEquals("negative size", "", text.right("abcdef", -2));
    }

    /**
     * Unit test {@link Text#mid(String, int, int)}
     */
    @Test
    public void test_mid() {
        Assert.assertEquals("Happy Path", "bc", text.mid("abcdef", 1, 2));
        Assert.assertEquals("no chars", "", text.mid("abcdef", 0, 0));
        Assert.assertEquals("single char", "a", text.mid("abcdef", 0, 1));
        Assert.assertEquals("full string", "abcdef", text.mid("abcdef", 0, 6));
        Assert.assertEquals("overflow", "abcdef", text.mid("abcdef", -1, 6));
        Assert.assertEquals("negative size", "", text.mid("abcdef", 4, -2));
    }


    /**
     * Unit test {@link Text#beforeFirst(String, String)}
     */
    @Test
    public void test_beforeFirst() {
        Assert.assertEquals("Empty Text", "", text.beforeFirst("", "~"));
        Assert.assertEquals("Empty delimiter", "", text.beforeFirst("abcdef", ""));
        Assert.assertEquals("No delimiter", "abcdef", text.beforeFirst("abcdef", "~"));
        Assert.assertEquals("One delimiter", "abc", text.beforeFirst("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ab", text.beforeFirst("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "", text.beforeFirst("~abcdef", "~"));
        Assert.assertEquals("Two Delimiters at start", "", text.beforeFirst("~~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "abcdef", text.beforeFirst("abcdef~", "~"));

        Assert.assertEquals("Long delimiter", "ab", text.beforeFirst("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "", text.beforeFirst("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#beforeLast(String, String)}
     */
    @Test
    public void test_beforeLast() {
        Assert.assertEquals("Empty Text", "", text.beforeLast("", "~"));
        Assert.assertEquals("Empty delimiter", "abcdef", text.beforeLast("abcdef", ""));
        Assert.assertEquals("No delimiter", "abcdef", text.beforeLast("abcdef", "~"));
        Assert.assertEquals("One delimiter", "abc", text.beforeLast("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ab~cd", text.beforeLast("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "", text.beforeLast("~abcdef", "~"));
        Assert.assertEquals("Two Delimiters at start", "~", text.beforeLast("~~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "abcdef", text.beforeLast("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "ab<=>cd", text.beforeLast("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "ab~cd~ef", text.beforeLast("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#afterFirst(String, String)}
     */
    @Test
    public void test_afterFirst() {
        Assert.assertEquals("Empty Text", "", text.afterFirst("", "~"));
        Assert.assertEquals("Empty delimiter", "abcdef", text.afterFirst("abcdef", ""));
        Assert.assertEquals("No delimiter", "", text.afterFirst("abcdef", "~"));
        Assert.assertEquals("One delimiter", "def", text.afterFirst("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "cd~ef", text.afterFirst("ab~cd~ef", "~"));
        Assert.assertEquals("Delimiter at start", "abcdef", text.afterFirst("~abcdef", "~"));
        Assert.assertEquals("Two Delimiters at start", "~abcdef", text.afterFirst("~~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "", text.afterFirst("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "cd<=>ef", text.afterFirst("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "ab~cd~ef", text.afterFirst("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#afterLast(String, String)}
     */
    @Test
    public void test_afterLast() {
        Assert.assertEquals("Empty Text", "", text.afterLast("", "~"));
        Assert.assertEquals("Empty delimiter", "", text.afterLast("abcdef", ""));
        Assert.assertEquals("No delimiter", "", text.afterLast("abcdef", "~"));
        Assert.assertEquals("One delimiter", "def", text.afterLast("abc~def", "~"));
        Assert.assertEquals("Two delimiters", "ef", text.afterLast("ab~cd~ef", "~"));
        Assert.assertEquals("Two Delimiters at start", "abcdef", text.afterLast("~~abcdef", "~"));
        Assert.assertEquals("Delimiter at start", "abcdef", text.afterLast("~abcdef", "~"));
        Assert.assertEquals("Delimiter at end", "", text.afterLast("abcdef~", "~"));

        Assert.assertEquals("Long Delimiter", "ef", text.afterLast("ab<=>cd<=>ef", "<=>"));
        Assert.assertEquals("Empty delimiter", "", text.afterLast("ab~cd~ef", ""));
    }

    /**
     * Unit test {@link Text#before(String, String, int)}
     */
    @Test
    public void test_before() {
        Assert.assertEquals("Empty Text", "", text.before("", "~", 1));
        Assert.assertEquals("Empty delimiter", "", text.before("abcdef", "", 1));
        Assert.assertEquals("No delimiter", "abcdef", text.before("abcdef", "~", 1));
        Assert.assertEquals("One delimiter", "abc", text.before("abc~def", "~", 1));
        Assert.assertEquals("Two delimiters", "ab", text.before("ab~cd~ef", "~", 1));
        Assert.assertEquals("Delimiter at start", "", text.before("~abcdef", "~", 1));
        Assert.assertEquals("Two Delimiters at start", "", text.before("~~abcdef", "~", 1));
        Assert.assertEquals("Delimiter at end", "abcdef", text.before("abcdef~", "~", 1));

        Assert.assertEquals("Long Delimiter", "ab<=><=>cd", text.before("ab<=><=>cd<=>ef<=>gh", "<=>", 3));
        Assert.assertEquals("Repeating Delimiter", "ab@@@@cd", text.before("ab@@@@cd@@ef@@gh", "@@", 3));

        Assert.assertEquals("count = 0", "", text.before("~~ab~~cd~~ef", "~~", 0));
        Assert.assertEquals("count = 1", "", text.before("~~ab~~cd~~ef", "~~", 1));
        Assert.assertEquals("count = 2", "~~ab", text.before("~~ab~~cd~~ef", "~~", 2));
        Assert.assertEquals("count = 3", "~~ab~~cd", text.before("~~ab~~cd~~ef", "~~", 3));
        Assert.assertEquals("count = 4", "~~ab~~cd~~ef", text.before("~~ab~~cd~~ef", "~~", 4));
        Assert.assertEquals("count = 5", "~~ab~~cd~~ef", text.before("~~ab~~cd~~ef", "~~", 5));

        IllegalArgumentException actual =
            Assert.assertThrows("Negative Count", IllegalArgumentException.class, () -> text.before("~~ab~~cd~~ef", "~~", -1));
        Assert.assertEquals("Unexpected error", "Invalid index: -1", actual.getMessage());
    }

    /**
     * Unit test {@link Text#after(String, String, int)}
     */
    @Test
    public void test_after() {
        Assert.assertEquals("Empty Text", "", text.after("", "~", 1));
        Assert.assertEquals("Empty delimiter", "abcdef", text.after("abcdef", "", 1));
        Assert.assertEquals("No delimiter", "", text.after("abcdef", "~", 1));
        Assert.assertEquals("One delimiter", "def", text.after("abc~def", "~", 1));
        Assert.assertEquals("Two delimiters", "cd~ef", text.after("ab~cd~ef", "~", 1));
        Assert.assertEquals("Delimiter at start", "abcdef", text.after("~abcdef", "~", 1));
        Assert.assertEquals("Two Delimiters at start", "~abcdef", text.after("~~abcdef", "~", 1));
        Assert.assertEquals("Delimiter at end", "", text.after("abcdef~", "~", 1));

        Assert.assertEquals("Long Delimiter", "ef<=>gh", text.after("ab<=><=>cd<=>ef<=>gh", "<=>", 3));
        Assert.assertEquals("Repeating Delimiter", "ef@@gh", text.after("ab@@@@cd@@ef@@gh", "@@", 3));

        Assert.assertEquals("count = 0", "~~ab~~cd~~ef", text.after("~~ab~~cd~~ef", "~~", 0));
        Assert.assertEquals("count = 1", "ab~~cd~~ef", text.after("~~ab~~cd~~ef", "~~", 1));
        Assert.assertEquals("count = 2", "cd~~ef", text.after("~~ab~~cd~~ef", "~~", 2));
        Assert.assertEquals("count = 3", "ef", text.after("~~ab~~cd~~ef", "~~", 3));
        Assert.assertEquals("count = 4", "", text.after("~~ab~~cd~~ef", "~~", 4));
        Assert.assertEquals("count = 5", "", text.after("~~ab~~cd~~ef", "~~", 5));

        IllegalArgumentException actual =
            Assert.assertThrows("Negative Count", IllegalArgumentException.class, () -> text.after("~~ab~~cd~~ef", "~~", -1));
        Assert.assertEquals("Unexpected error", "Invalid index: -1", actual.getMessage());
    }

    /**
     * Unit test {@link Text#between(String, String, int, int)}
     */
    @Test
    public void test_between() {
        Assert.assertEquals("Empty Text", "", text.between("", "~", 1, 2));
        Assert.assertEquals("Empty delimiter", "", text.between("abcdef", "", 1, 2));
        Assert.assertEquals("One delimiter", "def", text.between("abc~def", "~", 1, 2));
        Assert.assertEquals("Two delimiters", "cd", text.between("ab~cd~ef", "~", 1, 2));
        Assert.assertEquals("Delimiter at start", "abcdef", text.between("~abcdef", "~", 1, 2));
        Assert.assertEquals("Two Delimiters at start", "", text.between("~~abcdef", "~", 1, 2));
        Assert.assertEquals("Delimiter at end", "", text.between("abcdef~", "~", 1, 2));

        Assert.assertEquals("Long Delimiter", "ef<=>gh", text.between("ab<=><=>cd<=>ef<=>gh", "<=>", 3, 5));
        Assert.assertEquals("Repeating Delimiter", "ef@@gh", text.between("ab@@@@cd@@ef@@gh", "@@", 3, 5));

        Assert.assertEquals("0 to 1", "", text.between("~~ab~~cd~~ef", "~~", 0, 1));
        Assert.assertEquals("1 to 2", "ab", text.between("~~ab~~cd~~ef", "~~", 1, 2));
        Assert.assertEquals("2 to 3", "cd", text.between("~~ab~~cd~~ef", "~~", 2, 3));
        Assert.assertEquals("3 to 4", "ef", text.between("~~ab~~cd~~ef", "~~", 3, 4));
        Assert.assertEquals("4 to 5", "", text.between("~~ab~~cd~~ef", "~~", 4, 5));

        IllegalArgumentException actual =
            Assert.assertThrows("Negative Index", IllegalArgumentException.class, () -> text.between("~~ab~~cd~~ef", "~~", -1, 2));
        Assert.assertEquals("Unexpected error", "Invalid index: -1", actual.getMessage());
    }

    /**
     * Unit test {@link Text#contains(String, String)} 
     */
    @Test
    public void contains() {
        Assert.assertEquals("Empty Text and and Empty delimiter", 0, text.contains("", ""));
        Assert.assertEquals("With Text and and Empty delimiter", 12, text.contains("ab~~cd~ef~gh", ""));
        Assert.assertEquals("Short Delimiter", 4, text.contains("ab~~cd~ef~gh", "~"));
        Assert.assertEquals("Long Delimiter", 4, text.contains("ab<=><=>cd<=>ef<=>gh", "<=>"));
        Assert.assertEquals("Repeating Delimiter", 4, text.contains("ab@@@@cd@@ef@@gh", "@@"));
    }



    /**
     * Unit test {@link Text#extract(String, String)}
     */
    @Test
    public void test_extract() {
        Assert.assertEquals("Not Found", "", text.extract("Hello World", ".*~(.*)~.*"));
        Assert.assertEquals("Found", "capture", text.extract("Hello ~capture~ World", ".*~(.*)~.*"));
        Assert.assertEquals("multiple groups",
            "FirstSecondThird",
            text.extract("prefix /First/Second/Third/ postfix", ".*/(.*)/(.*)/(.*)/.*"));
    }


    /**
     * Unit test {@link Text#matches(String, String)}
     */
    @Test
    public void test_matches() {
        Assert.assertFalse("Not Found", text.matches("Hello World", ".*~(.*)~.*"));
        Assert.assertTrue("Found", text.matches("Hello ~find me~ World", ".*~(.*)~.*"));
    }

    /**
     * Unit test {@link Text#replace(String, String, String)}
     */
    @Test
    public void test_replace() {
        Assert.assertEquals("Empty String", "", text.replace("", " ", "#"));
        Assert.assertEquals("single char replacement", "Hello#World#!", text.replace("Hello World !", " ", "#"));
        Assert.assertEquals("two char replacement", "Hello__World__!", text.replace("Hello World !", " ", "__"));
        Assert.assertEquals("Word removal", " World !", text.replace("Hello World !", "Hello", ""));
    }

    /**
     * Unit test {@link Text#replaceEx(String, String, String)}
     */
    @Test
    public void test_replaceEx() {
        Assert.assertEquals("Empty String", "", text.replaceEx("", " ", "#"));
        Assert.assertEquals("single char replacement", "Hello#World#!", text.replaceEx("Hello World !", " ", "#"));
        Assert.assertEquals("two char replacement", "Hello__World__!", text.replaceEx("Hello World !", " ", "__"));
        Assert.assertEquals("Word removal", " World !", text.replaceEx("Hello World !", "Hello", ""));
        Assert.assertEquals("replace uppers", "ello orld !", text.replaceEx("Hello World !", "[A-Z]", ""));
    }

    /**
     * Unit test {@link Text#trim(String)}
     */
    @Test
    public void test_trim() {
        Assert.assertEquals("Empty String", "", text.trim(""));
        Assert.assertEquals("No White Space", "abc", text.trim("abc"));
        Assert.assertEquals("Internal Space", "ab cd", text.trim("ab cd"));
        Assert.assertEquals("Internal Tab", "ab\tcd", text.trim("ab\tcd"));
        Assert.assertEquals("Leading Space", "a", text.trim(" a"));
        Assert.assertEquals("Trailing Space", "a", text.trim("a "));
        Assert.assertEquals("Leading Tab", "a", text.trim("\ta"));
        Assert.assertEquals("Trailing Tab", "a", text.trim("a\t"));
        Assert.assertEquals("Trailing and trailing spaces", "ab \tcd", text.trim(" \t ab \tcd\t \t"));
    }

    /**
     * Unit test {@link Text#isEmpty(String)}
     */
    @Test
    public void test_isEmpty() {
        Assert.assertTrue("Empty String", text.isEmpty(""));
        Assert.assertFalse("Whitespace String", text.isEmpty(" \t "));
        Assert.assertFalse("Non-empty String", text.isEmpty("x"));
    }

    /**
     * Unit test {@link Text#isBlank(String)}
     */
    @Test
    public void test_isBlank() {
        Assert.assertTrue("Empty String", text.isBlank(""));
        Assert.assertTrue("Whitespace String", text.isBlank(" \t "));
        Assert.assertFalse("Non-empty String", text.isBlank("x"));
    }


    /**
     * Unit test {@link Text#len(String)}
     */
    @Test
    public void test_len() {
        Assert.assertEquals("Empty String", 0, text.len(""));
        Assert.assertEquals("Single character", 1, text.len("a"));
        Assert.assertEquals("Whitespace", 3, text.len(" \t "));
        Assert.assertEquals("Multiple characters", 4, text.len("abcd"));
    }

    /**
     * Unit test {@link Text#indexOf(String, String, EelLambda)}
     */
    @Test
    public void test_indexOf() {
        Assert.assertEquals("Empty String", -1, text.indexOf("", "cd", minus1));
        Assert.assertEquals("Empty search", 0, text.indexOf("abcdefabcdef", "", minus1));
        Assert.assertEquals("Found", 2, text.indexOf("abcdefabcdef", "cd", minus1));
        Assert.assertEquals("#1 Not found", -1, text.indexOf("abcdefabcdef", "dc", minus1));
        Assert.assertEquals("#2 Not found", 0, text.indexOf("abcdefabcdef", "dc", zero));
    }

    /**
     * Unit test {@link Text#lastIndexOf(String, String, EelLambda)}
     */
    @Test
    public void test_lastIndexOf() {
        Assert.assertEquals("Empty String", -1, text.lastIndexOf("", "cd", minus1));
        Assert.assertEquals("Empty search", 12, text.lastIndexOf("abcdefabcdef", "", minus1));
        Assert.assertEquals("Found", 8, text.lastIndexOf("abcdefabcdef", "cd", minus1));
        Assert.assertEquals("#1 Not found", -1, text.lastIndexOf("abcdefabcdef", "dc", minus1));
        Assert.assertEquals("#2 Not found", 0, text.lastIndexOf("abcdefabcdef", "dc", zero));
    }


    /**
     * Unit test {@link Text#nthIndexOf(String, String, int, Text.Direction)} 
     */
    @Test
    public void test_nthIndexOf_after() {
        Assert.assertEquals("empty text", 0, text.nthIndexOf("", "~", 1, Text.Direction.AFTER));
        Assert.assertEquals("empty delimiter", 0, text.nthIndexOf("ab~cd~ef~gh", "", 1, Text.Direction.AFTER));

        Assert.assertEquals("0th element", 0, text.nthIndexOf("ab~cd~ef~gh", "~", 0, Text.Direction.AFTER));
        Assert.assertEquals("1st element", 3, text.nthIndexOf("ab~cd~ef~gh", "~", 1, Text.Direction.AFTER));
        Assert.assertEquals("2nd element", 6, text.nthIndexOf("ab~cd~ef~gh", "~", 2, Text.Direction.AFTER));
        Assert.assertEquals("3rd element", 9, text.nthIndexOf("ab~cd~ef~gh", "~", 3, Text.Direction.AFTER));
        Assert.assertEquals("4th element", 11, text.nthIndexOf("ab~cd~ef~gh", "~", 4, Text.Direction.AFTER));
        Assert.assertEquals("5th element", 11, text.nthIndexOf("ab~cd~ef~gh", "~", 5, Text.Direction.AFTER));

        Assert.assertEquals("Long delimiter", 8, text.nthIndexOf("ab~.cd~.ef~.gh", "~.", 2, Text.Direction.AFTER));
        Assert.assertEquals("Same char delimiter", 8, text.nthIndexOf("ab@@cd@@ef@@gh", "@@", 2, Text.Direction.AFTER));
        Assert.assertEquals("extended delimiter", 9, text.nthIndexOf("ab@@@cd@@@ef@@@gh", "@@", 2, Text.Direction.AFTER));
    }

    /**
     * Unit test {@link Text#nthIndexOf(String, String, int, Text.Direction)}
     */
    @Test
    public void test_nthIndexOf_before() {
        Assert.assertEquals("empty text", 0, text.nthIndexOf("", "~", 1, Text.Direction.BEFORE));
        Assert.assertEquals("empty delimiter", 0, text.nthIndexOf("ab~cd~ef~gh", "", 1, Text.Direction.BEFORE));
        Assert.assertEquals("0th element", 0, text.nthIndexOf("ab~cd~ef~gh", "~", 0, Text.Direction.BEFORE));
        Assert.assertEquals("1st element", 2, text.nthIndexOf("ab~cd~ef~gh", "~", 1, Text.Direction.BEFORE));
        Assert.assertEquals("2nd element", 5, text.nthIndexOf("ab~cd~ef~gh", "~", 2, Text.Direction.BEFORE));
        Assert.assertEquals("3rd element", 8, text.nthIndexOf("ab~cd~ef~gh", "~", 3, Text.Direction.BEFORE));
        Assert.assertEquals("4th element", 11, text.nthIndexOf("ab~cd~ef~gh", "~", 4, Text.Direction.BEFORE));
        Assert.assertEquals("5th element", 11, text.nthIndexOf("ab~cd~ef~gh", "~", 5, Text.Direction.BEFORE));

        Assert.assertEquals("Long delimiter", 6, text.nthIndexOf("ab~.cd~.ef~.gh", "~.", 2, Text.Direction.BEFORE));
        Assert.assertEquals("Same char delimiter", 6, text.nthIndexOf("ab@@cd@@ef@@gh", "@@", 2, Text.Direction.BEFORE));
        Assert.assertEquals("extended delimiter", 7, text.nthIndexOf("ab@@@cd@@@ef@@@gh", "@@", 2, Text.Direction.BEFORE));
    }
}