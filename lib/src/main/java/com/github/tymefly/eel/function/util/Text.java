package com.github.tymefly.eel.function.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.StringUtils;

/**
 * Functions that manipulate Text.
 */
@PackagedEelFunction
public class Text {

    /**
     * Entry point for the {@code upper} function, which returns the {@code text} converted to upper case
     * <br>
     * The EEL syntax for this function is <code>upper( text )</code>
     * @param text  to convert to upper case
     * @return text in upper case
     * @see #lower(String) 
     * @see #title(String) 
     */
    @EelFunction(name = "upper")
    @Nonnull
    public String upper(@Nonnull String text) {
        return text.toUpperCase();
    }

    /**
     * Entry point for the {@code lower} function, which returns the {@code text} converted to lower case
     * <br>
     * The EEL syntax for this function is <code>lower( text )</code>
     * @param text  to convert to lower case
     * @return text in lower case
     * @see #upper(String) 
     * @see #title(String)
     */
    @EelFunction(name = "lower")
    @Nonnull
    public String lower(@Nonnull String text) {
        return text.toLowerCase();
    }
    

    /**
     * Entry point for the {@code title} function, which returns the {@code text} converted to title case.
     * <br>
     * The EEL syntax for this function is <code>title( text )</code>
     * @param text  to convert to lower case
     * @return text in title case
     * @see #upper(String)
     * @see #lower(String) 
     * @since 1.1
     */
    @EelFunction(name = "title")
    @Nonnull
    public String title(@Nonnull String text) {
        return StringUtils.toTitleCase(text);
    }


    /**
     * Entry point for the {@code left} function, which returns the first part of some text
     * <br>
     * The EEL syntax for this function is <code>left( text, count )</code>
     * @param text      Text from which the left most characters are returned
     * @param count     The maximum number of characters in the returned string. If count is greater than
     *                  the length of the string then the complete string is returned
     * @return          Up to {@code count} characters from the start of {@code text}
     * @see #mid(String, int, int) 
     * @see #right(String, int) 
     */
    @EelFunction(name = "left")
    @Nonnull
    public String left(@Nonnull String text, int count) {
        return StringUtils.left(text, count);
    }

    /**
     * Entry point for the {@code right} function, which returns the last part of some text
     * <br>
     * The EEL syntax for this function is <code>right( text, count )</code>
     * @param text      Text from which the right most characters are returned
     * @param count     The maximum number of characters in the returned string. If count is greater than
     *                  the length of the string then the complete string is returned
     * @return          up to {@code count} characters from the end of {@code text}
     * @see #left(String, int) 
     * @see #mid(String, int, int) 
     */
    @EelFunction(name = "right")
    @Nonnull
    public String right(@Nonnull String text, int count) {
        return StringUtils.right(text, count);
    }

    /**
     * Entry point for the {@code mid} function, which returns the middle part of some text
     * <br>
     * The EEL syntax for this function is <code>mid( text, offset, end )</code>
     * @param text      Text from which the right most characters are returned
     * @param offset    zero based index of first character to return
     * @param count     The maximum number of characters in the returned string. If count is greater than
     *                  the length of the string then the complete string is returned
     * @return          the middle part of {@code text}. If {@code start} is greater than {@code end}
     *                      then an empty string is returned
     * @see #left(String, int) 
     * @see #right(String, int) 
     */
    @EelFunction(name = "mid")
    @Nonnull
    public String mid(@Nonnull String text, int offset, int count) {
        return StringUtils.mid(text, offset, count);
    }

    /**
     * Entry point for the {@code beforeFirst} function, which returns all the text before the first occurrence
     * of the {@code delimiter}
     * <br>
     * The EEL syntax for this function is <code>beforeFirst( text, delimiter )</code>
     * @param text      Text from which characters are returned
     * @param delimiter The delimiter which should appear in the {@code test}
     * @return          The text before the first occurrence of the {@code delimiter}
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     */
    @EelFunction(name = "beforeFirst")
    @Nonnull
    public String beforeFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Entry point for the {@code afterFirst} function, which returns all the text after the first occurrence
     * of the {@code delimiter}
     * <br>
     * The EEL syntax for this function is <code>afterFirst( text, delimiter )</code>
     * @param text      Text from which characters are returned
     * @param delimiter The delimiter which should appear in the {@code test}
     * @return          The text after the first occurrence of the {@code delimiter}
     * @see #beforeFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     */
    @EelFunction(name = "afterFirst")
    @Nonnull
    public String afterFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? text : text.substring(index + delimiter.length()));
    }

    /**
     * Entry point for the {@code beforeLast} function, which returns all the text before the last occurrence
     * of the {@code delimiter}
     * <br>
     * The EEL syntax for this function is <code>beforeLast( text, delimiter )</code>
     * @param text      Text from which characters are returned
     * @param delimiter The delimiter which should appear in the {@code test}
     * @return          The text before the last occurrence of the {@code delimiter}
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #afterLast(String, String)
     */
    @EelFunction(name = "beforeLast")
    @Nonnull
    public String beforeLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Entry point for the {@code beforeFirst} function, which returns all the text after the last occurrence
     * of the {@code delimiter}
     * <br>
     * The EEL syntax for this function is <code>afterLast( text, delimiter )</code>
     * @param text      Text from which characters are returned
     * @param delimiter The delimiter which should appear in the {@code test}
     * @return          The text after the last occurrence of the {@code delimiter}
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     */
    @EelFunction(name = "afterLast")
    @Nonnull
    public String afterLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? text : text.substring(index + delimiter.length()));
    }

    /**
     * Entry point for the {@code extract} function that extracts data from some text based on a {@code regEx}
     * <br>
     * The EEL syntax for this function is <code>extract( text, regEx )</code>
     * @param text      Full string
     * @param regEx     the regular expression which should contain some matching groups
     * @return          the extracted text
     * @see #matches(String, String)
     */
    @EelFunction(name = "extract")
    public String extract(@Nonnull String text, @Nonnull String regEx) {
        String result;
        Matcher matcher = Pattern.compile(regEx).matcher(text);

        if (!matcher.matches()) {
            result = "";
        } else if (matcher.groupCount() == 1) {
            result = matcher.group(1);
        } else {
            StringBuilder builder = new StringBuilder();
            int index = 0;
            int count = matcher.groupCount();

            while (index++ != count) {
                builder.append(matcher.group(index));
            }

            result = builder.toString();
        }

        return result;
    }

    
    /**
     * Entry point for the {@code matches} function that checks to see if the {@code text} matches a {@code regEx}
     * <br>
     * The EEL syntax for this function is <code>matches( text, regEx )</code>
     * @param text      Text to check
     * @param regEx     the regular expression to which the {@code text} checked
     * @return          {@literal true} only if the {@code text} matches the {@code regEx}
     * @see #extract(String, String) 
     */
    @EelFunction(name = "matches")
    public boolean matches(@Nonnull String text, @Nonnull String regEx) {
        return text.matches(regEx);
    }


    /**
     * Entry point for the {@code replace} function that replaces all instances of {@code from} in the
     * {@code text} with {@code to}
     * <br>
     * The EEL syntax for this function is <code>replace( text, from, to )</code>
     * @param text      Text to check
     * @param from      Text to replace
     * @param to        Text to substitute
     * @return          the original {@code text} with all instances of {@code from} replaced by {@code to}
     * @see #replaceEx(String, String, String)
     */
    @EelFunction(name = "replace")
    @Nonnull
    public String replace(@Nonnull String text, @Nonnull String from, @Nonnull String to) {
        return text.replace(from, to);
    }

    
    /**
     * Entry point for the {@code replace} function that replaces all instances of {@code regEx} in the
     * {@code text} with {@code to}
     * <br>
     * The EEL syntax for this function is <code>replaceEx( text, regEx, to )</code>
     * @param text      Text to check
     * @param regEx     Regular Expression to search for
     * @param to        Text to substitute
     * @return          the original {@code text} with all instances of {@code from} replaced by {@code to}
     * @see #replace(String, String, String) 
     */
    @EelFunction(name = "replaceEx")
    @Nonnull
    public String replaceEx(@Nonnull String text, @Nonnull String regEx, @Nonnull String to) {
        return text.replaceAll(regEx, to);
    }
    

    /**
     * Entry point for the {@code trim} function, which returns the {@code text} with any leading and
     * trailing spaces removed
     * <br>
     * The EEL syntax for this function is <code>trim( text )</code>
     * @param text  to convert to upper case
     * @return text with no leading or trailing spaces
     */
    @EelFunction(name = "trim")
    @Nonnull
    public String trim(@Nonnull String text) {
        return text.trim();
    }


    /**
     * Entry point for the {@code len} function, which returns the length of a string, including leading and 
     * trailing spaces
     * <br>
     * The EEL syntax for this function is <code>len( text )</code>
     * @param text  text to check
     * @return the length of {@code text}, including leading and trailing spaces
     * @see #isEmpty(String)
     * @see #isBlank(String)
     */
    @EelFunction(name = "len")
    public int len(@Nonnull String text) {
        return text.length();
    }

    /**
     * Entry point for the {@code isEmpty} function, which returns the true only if the {@code text} is empty
     * <br>
     * The EEL syntax for this function is <code>isEmpty( text )</code>
     * @param text  text to check
     * @return the {@literal true} only if the {@code text} is empty
     * @see #len(String)
     * @see #isBlank(String)
     */
    @EelFunction(name = "isEmpty")
    public boolean isEmpty(@Nonnull String text) {
        return text.isEmpty();
    }

    /**
     * Entry point for the {@code isEmpty} function, which returns the true only if the {@code text} is empty or
     * contains whitespace
     * <br>
     * The EEL syntax for this function is <code>isBlank( text )</code>
     * @param text  text to check
     * @return the {@literal true} only if the {@code text} is empty or contains whitespace
     * @see #len(String)
     * @see #isEmpty(String)
     * @since 1.1
     */
    @EelFunction(name = "isBlank")
    public boolean isBlank(@Nonnull String text) {
        return text.isBlank();
    }

    /**
     * Entry point for the {@code indexOf} function, which returns the 0 based index of the first occurrence of
     * {@code subString} in {@code text}, or -1 if {@code text} does not contain {@code subString}
     * <br>
     * The EEL syntax for this function is <code>indexOf( text, subString )</code>
     * @param text      text to check
     * @param subString The substring to search for
     * @return returns the 0 based index of the first occurrence of {@code subString} in {@code text}
     * @see #len(String)
     */
    @EelFunction(name = "indexOf")
    public int indexOf(@Nonnull String text, @Nonnull String subString) {
        return text.indexOf(subString);
    }

    /**
     * Entry point for the {@code indexOf} lastIndexOf that returns the 0 based index of the last occurrence of
     * {@code subString} in {@code text}, or -1 if {@code text} does not contain {@code subString}
     * <br>
     * The EEL syntax for this function is <code>lastIndexOf( text, subString )</code>
     * @param text      text to check
     * @param subString The substring to search for
     * @return returns the 0 based index of the last occurrence of {@code subString} in {@code text}
     * @see #len(String)
     */
    @EelFunction(name = "lastIndexOf")
    public int lastIndexOf(@Nonnull String text, @Nonnull String subString) {
        return text.lastIndexOf(subString);
    }
}
