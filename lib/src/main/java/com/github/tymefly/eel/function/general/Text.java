package com.github.tymefly.eel.function.general;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Value;
import com.github.tymefly.eel.annotation.VisibleForTesting;
import com.github.tymefly.eel.udf.DefaultArgument;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.StringUtils;
import com.github.tymefly.eel.validate.Preconditions;

/**
 * Functions for general-purpose text manipulation.
 * @since 1.0
 */
@PackagedEelFunction
public class Text {
    private static final String REMAINING_TEXT = "" + Integer.MAX_VALUE;

    /** Used by {@link #nthIndexOf} to determine which way to offset the returned index */
    @VisibleForTesting
    enum Direction {
        BEFORE,
        AFTER
    }


    /**
     * Returns a portion of the specified {@code text} based on the given {@code length}.
     * If {@code length} is positive, returns up to {@code length} characters from the start;
     * if {@code length} is negative, returns up to {@code length} characters from the end of the {@code text}.
     * @param text      the text from which characters are extracted
     * @param length    the number of characters to return (positive counts from the start, negative from the end)
     * @return          the extracted portion of the specified text according to the given length
     * @see #mid(String, int, int)
     * @see #right(String, int)
     * @see #before(String, String, int)
     * @since 1.0
     */
    @EelFunction("left")
    @Nonnull
    public String left(@Nonnull String text, int length) {
        return StringUtils.left(text, length);
    }

    /**
     * Returns a portion of the specified {@code text} based on the given {@code length}.
     * If {@code length} is positive, returns up {@code length} characters from the end;
     * if {@code length} is negative, returns up {@code length} characters from the start of the {@code text}.
     * @param text      the text from which characters are extracted
     * @param length    the number of characters to return (positive counts from the end, negative from the start)
     * @return          the extracted portion of the specified text according to the given length
     * @see #left(String, int)
     * @see #mid(String, int, int)
     * @see #after(String, String, int)
     * @since 1.0
     */
    @EelFunction("right")
    @Nonnull
    public String right(@Nonnull String text, int length) {
        return StringUtils.right(text, length);
    }

    /**
     * Returns the middle portion of the specified {@code text}.
     * <p>
     * This function's behaviour deliberately matches the behaviour used by bash.
     * @param text      the text from which characters are extracted
     * @param position  if positive, the zero-based index of the start; if negative, counted from the end of the text
     * @param length    if positive, the maximum number of characters to return; if negative, counted from the end
     * @return          the portion of the specified text starting at {@code position} and spanning up to {@code length}
     *                  characters
     * @see #left(String, int)
     * @see #right(String, int)
     * @see #between(String, String, int, int)
     * @since 1.0
     */
    @EelFunction("mid")
    @Nonnull
    public String mid(@Nonnull String text,
                      int position,
                      @DefaultArgument(value = REMAINING_TEXT, description = "The remaining text") int length) {
        return StringUtils.mid(text, position, length);
    }


    /**
     * Returns all text preceding the first occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @return          the text before the first occurrence of {@code delimiter}; if the delimiter does not
     *                  occur, the entire text is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 1.0
     */
    @EelFunction("beforeFirst")
    @Nonnull
    public String beforeFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Returns all text following the first occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @return          the text after the first occurrence of {@code delimiter}; if the delimiter does not
     *                  occur, an empty string is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 2.0
     */
    @EelFunction("afterFirst")
    @Nonnull
    public String afterFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? "" : text.substring(index + delimiter.length()));
    }

    /**
     * Returns all text preceding the last occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @return          the text before the last occurrence of {@code delimiter}; if the delimiter does not
     *                  occur, the entire text is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 1.0
     */
    @EelFunction("beforeLast")
    @Nonnull
    public String beforeLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Returns all text following the last occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @return          the text after the last occurrence of {@code delimiter}; if the delimiter does not
     *                  occur, an empty text is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #contains(String, String)
     * @since 2.0
     */
    @EelFunction("afterLast")
    @Nonnull
    public String afterLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? "" : text.substring(index + delimiter.length()));
    }


    /**
     * Returns all text preceding the {@code count}'th occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @param count     the occurrence of {@code delimiter} to consider; must not be negative
     * @return          the text before the {@code count}'th occurrence of {@code delimiter}; if the delimiter
     *                  does not occur {@code count} times, the entire text is returned
     * @see #after(String, String, int)
     * @see #between(String, String, int, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #left(String, int)
     * @since 2.0
     */
    @EelFunction("before")
    @Nonnull
    public String before(@Nonnull String text, @Nonnull String delimiter, int count) {
        int index = nthIndexOf(text, delimiter, count, Direction.BEFORE);

        return text.substring(0, index);
    }

    /**
     * Returns all text following the {@code count}'th occurrence of the specified {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @param count     the occurrence of {@code delimiter} to consider; must not be negative
     * @return          the text after the {@code count}'th occurrence of {@code delimiter}; if the delimiter
     *                  does not occur {@code count} times, an empty string is returned
     * @see #before(String, String, int)
     * @see #between(String, String, int, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #right(String, int)
     * @since 2.0
     */
    @EelFunction("after")
    @Nonnull
    public String after(@Nonnull String text, @Nonnull String delimiter, int count) {
        int index = nthIndexOf(text, delimiter, count, Direction.AFTER);

        return text.substring(index);
    }

    /**
     * Returns all text between the {@code start}'th and {@code end}'th occurrence of the specified
     * {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter to locate within the text
     * @param start     the first occurrence of {@code delimiter} to consider; must not be negative
     * @param end       the last occurrence of {@code delimiter} to consider; must not be negative
     * @return          the text between the {@code start}'th and {@code end}'th occurrences of {@code delimiter};
     *                  if {@code delimiter} does not occur {@code end} times, an empty string is returned
     * @see #before(String, String, int)
     * @see #after(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #mid(String, int, int)
     * @since 2.0
     */
    @EelFunction("between")
    @Nonnull
    public String between(@Nonnull String text, @Nonnull String delimiter, int start, int end) {
        int startIndex = nthIndexOf(text, delimiter, start, Direction.AFTER);
        int endIndex = nthIndexOf(text, delimiter, end, Direction.BEFORE);

        return text.substring(startIndex, endIndex);
    }

    /**
     * Returns the number of times the specified {@code search} phrase occurs in the {@code text}.
     * @param text      the text to be searched
     * @param search    the phrase to count within the text
     * @return          the number of instances of {@code search} in {@code text}; if {@code search} is empty,
     *                  the value returned is the length of the text
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 2.0
     */
    @EelFunction("contains")
    public int contains(@Nonnull String text, @Nonnull String search) {
        int count;

        if (search.isEmpty()) {
            count = text.length();
        } else {
            count = 0;

            int start = 0;
            while (start >= 0) {
                start = text.indexOf(search, start);

                if (start > 0) {
                    start += search.length();

                    count++;
                }
            }
        }

        return count;
    }


    /**
     * Extracts characters from the specified {@code text} using a Java-style regular expression.
     * @param text      the input text from which characters are extracted
     * @param regEx     the regular expression pattern to match; must include capturing groups
     * @return          the text extracted based on the specified {@code regEx}
     * @see #matches(String, String)
     * @since 1.0
     */
    @EelFunction("extract")
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
     * Checks whether the specified {@code text} matches the given Java-style regular expression.
     * @param text      the text to check for a match
     * @param regEx     the regular expression pattern to match against
     * @return          {@literal true} if the {@code text} matches {@code regEx}; otherwise {@literal false}
     * @see #extract(String, String)
     * @since 1.0
     */
    @EelFunction("matches")
    public boolean matches(@Nonnull String text, @Nonnull String regEx) {
        return text.matches(regEx);
    }

        /**
     * Replaces all occurrences of the literal {@code from} in the specified {@code text} with the literal {@code to}.
     * @param text      the text in which replacements are made
     * @param from      the literal text to be replaced
     * @param to        the literal text to substitute for {@code from}
     * @return          the original {@code text} with all instances of {@code from} replaced by {@code to}
     * @see #replaceEx(String, String, String)
     * @since 1.0
     */
    @EelFunction("replace")
    @Nonnull
    public String replace(@Nonnull String text, @Nonnull String from, @Nonnull String to) {
        return text.replace(from, to);
    }

    /**
     * Replaces each part of the specified {@code text} that matches the Java-style regular expression
     * {@code regEx} with the literal text {@code to}.
     * @param text      the text in which replacements are made
     * @param regEx     the regular expression to search for
     * @param to        the literal text to substitute for matches
     * @return          the original {@code text} with all instances matching {@code regEx} replaced by {@code to}
     * @see #replace(String, String, String)
     * @since 1.0
     */
    @EelFunction("replaceEx")
    @Nonnull
    public String replaceEx(@Nonnull String text, @Nonnull String regEx, @Nonnull String to) {
        return text.replaceAll(regEx, to);
    }
    

    /**
     * Returns the {@code text} with leading and trailing spaces removed.
     * @param text      the text to trim
     * @return          the {@code text} with no leading or trailing spaces
     * @since 1.0
     */
    @EelFunction("trim")
    @Nonnull
    public String trim(@Nonnull String text) {
        return text.trim();
    }


    /**
     * Returns the length of the specified {@code text}, including leading and trailing spaces.
     * @param text      the text whose length is to be measured
     * @return          the length of the specified {@code text}, including any leading and trailing spaces
     * @see #isEmpty(String) for checking if the text is empty
     * @see #isBlank(String) for checking if the text is blank (contains only spaces)
     * @since 1.0
     */
    @EelFunction("len")
    public int len(@Nonnull String text) {
        return text.length();
    }

    /**
     * Returns {@literal true} if the specified {@code text} is empty.
     * Text containing one or more spaces is not considered empty.
     * @param text      the text to check
     * @return          {@literal true} if the {@code text} contains no characters
     * @see #len(String) for getting the length of the text
     * @see #isBlank(String) for checking if the text is blank (contains only spaces)
     * @since 1.0
     */
    @EelFunction("isEmpty")
    public boolean isEmpty(@Nonnull String text) {
        return text.isEmpty();
    }

    /**
     * Returns {@literal true} if the specified {@code text} is empty or contains only whitespace.
     * @param text      the text to check
     * @return          {@literal true} if the {@code text} is empty or contains only whitespace
     * @see #len(String)
     * @see #isEmpty(String)
     * @since 1.1
     */
    @EelFunction("isBlank")
    public boolean isBlank(@Nonnull String text) {
        return text.isBlank();
    }

    /**
     * Returns the 0-based index of the first occurrence of {@code subText} in the specified {@code text},
     * or {@code defaultValue} if {@code subText} does not occur.
     * @param text          the text to check
     * @param subText       the text to search for
     * @param defaultValue  the value to return if {@code subText} is not found
     * @return              the 0-based index of the first occurrence of {@code subText} in {@code text}
     * @see #len(String)
     * @see #lastIndexOf(String, String, Value)
     * @since 1.0
     */
    @EelFunction("indexOf")
    public int indexOf(@Nonnull String text,
                       @Nonnull String subText,
                       @Nonnull @DefaultArgument("-1") Value defaultValue) {
        int index = text.indexOf(subText);

        return (index == -1 ? defaultValue.asInt() : index);
    }

    /**
     * Returns the 0-based index of the last occurrence of {@code subString} in the specified {@code text},
     * or {@code defaultValue} if {@code subString} does not occur.
     * @param text          the text to check
     * @param subString     the text to search for
     * @param defaultValue  the value to return if {@code subString} is not found
     * @return              the 0-based index of the last occurrence of {@code subString} in {@code text}
     * @see #len(String)
     * @see #indexOf(String, String, Value)
     * @since 1.0
     */
    @EelFunction("lastIndexOf")
    public int lastIndexOf(@Nonnull String text,
                       @Nonnull String subString,
                       @Nonnull @DefaultArgument("-1") Value defaultValue) {
        int index = text.lastIndexOf(subString);

        return (index == -1 ? defaultValue.asInt() : index);
    }


    @VisibleForTesting
    int nthIndexOf(@Nonnull String text, @Nonnull String delimiter, int index, @Nonnull Direction direction) {
        Preconditions.checkArgument((index >= 0), "Invalid index: %d", index);

        int start = 0;
        int offset = 0;
        int length = delimiter.length();

        while ((index > 0) && (start >= 0)) {
            start = text.indexOf(delimiter, start + offset);
            offset = length;

            index--;
        }

        if (start == -1) {                                  // Not enough delimiters in the text
            start = text.length();
        } else if (direction == Direction.AFTER) {          // Found the delimiter, offset
            start += offset;
        } else if (direction == Direction.BEFORE) {         // Found the delimiter
            // No adjustment required
        } else {                                            // Should not happen
            throw new IllegalArgumentException("Unexpected direction: " + direction);
        }

        return start;
    }
}
