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
 * General purpose Text functions
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
     * Returns the first portion of the specified {@code text} according to the given {@code length}.
     * If {@code length} is positive, up to that many characters are returned from the start;
     * if {@code length} is negative, that many characters are returned from the end of the {@code text}.
     * @param text      the text from which characters are obtained
     * @param length    the number of characters to return (positive from start, negative from end)
     * @return          a portion of the specified {@code text} according to the given {@code length}
     * @see #mid(String, int, int)
     * @see #right(String, int)
     * @see #before(String, String, int)
     */
    @EelFunction("left")
    @Nonnull
    public String left(@Nonnull String text, int length) {
        return StringUtils.left(text, length);
    }

    /**
     * Returns the last portion of the specified {@code text} according to the given {@code length}.
     * If {@code length} is positive, up to that many characters are returned from the end;
     * if {@code length} is negative, that many characters are returned from the start of the {@code text}.
     * @param text      the text from which characters are obtained
     * @param length    the number of characters to return (positive from end, negative from start)
     * @return          a portion of the specified {@code text} according to the given {@code length}
     * @see #left(String, int)
     * @see #mid(String, int, int)
     * @see #after(String, String, int)
     */
    @EelFunction("right")
    @Nonnull
    public String right(@Nonnull String text, int length) {
        return StringUtils.right(text, length);
    }

    /**
     * Returns the middle portion of the specified {@code text}.
     * <p>
     * The specification of this function deliberately matches the behaviour used by bash.
     * @param text      the text from which characters are obtained
     * @param position  if positive, the zero-based index of the start of the {@code text};
     *                  if negative, the start index is counted from the end of the {@code text}
     * @param length    if positive, the maximum number of characters to return;
     *                  if negative, the end index is counted from the end of the {@code text}
     * @return          a portion of the specified {@code text} starting at {@code position} and spanning up to
     *                      {@code length} characters
     * @see #left(String, int)
     * @see #right(String, int)
     * @see #between(String, String, int, int)
     */
    @EelFunction("mid")
    @Nonnull
    public String mid(@Nonnull String text,
                      int position,
                      @DefaultArgument(value = REMAINING_TEXT, description = "The remaining text") int length) {
        return StringUtils.mid(text, position, length);
    }


    /**
     * Returns all the text before the first occurrence of the {@code delimiter}.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @return          the text before the first occurrence of the {@code delimiter};
     *                      if the {@code delimiter} does not occur in the {@code text}, the full
     *                      {@code text} is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     */
    @EelFunction("beforeFirst")
    @Nonnull
    public String beforeFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Returns all the text after the first occurrence of the {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @return          the text after the first occurrence of the {@code delimiter};
     *                  if the {@code delimiter} does not occur in the {@code text}, an empty
     *                  string is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 2.0.0
     */
    @EelFunction("afterFirst")
    @Nonnull
    public String afterFirst(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.indexOf(delimiter);

        return (index == -1 ? "" : text.substring(index + delimiter.length()));
    }

    /**
     * Returns all the text before the last occurrence of the {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @return          the text before the last occurrence of the {@code delimiter};
     *                  if the {@code delimiter} does not occur in the {@code text}, the full
     *                  {@code text} is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     */
    @EelFunction("beforeLast")
    @Nonnull
    public String beforeLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? text : text.substring(0, index));
    }

    /**
     * Returns all the text after the last occurrence of the {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @return          the text after the last occurrence of the {@code delimiter};
     *                  if the {@code delimiter} does not occur in the {@code text}, an empty
     *                  text is returned
     * @see #after(String, String, int)
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #contains(String, String)
     * @since 2.0.0
     */
    @EelFunction("afterLast")
    @Nonnull
    public String afterLast(@Nonnull String text, @Nonnull String delimiter) {
        int index = text.lastIndexOf(delimiter);

        return (index == -1 ? "" : text.substring(index + delimiter.length()));
    }


    /**
     * Returns all the text before the {@code count}'th occurrence of the {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @param count     the occurrence count of {@code delimiter} in the {@code text}; must not be negative
     * @return          the text before the {@code count}'th occurrence of the {@code delimiter};
     *                  if the {@code delimiter} does not occur {@code count} times, the full
     *                  {@code text} is returned
     * @see #after(String, String, int)
     * @see #between(String, String, int, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #left(String, int)
     * @since 2.0.0
     */

    @EelFunction("before")
    @Nonnull
    public String before(@Nonnull String text, @Nonnull String delimiter, int count) {
        int index = nthIndexOf(text, delimiter, count, Direction.BEFORE);

        return text.substring(0, index);
    }

    /**
     * Returns all the text after the {@code count}'th occurrence of the {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @param count     the occurrence count of {@code delimiter} in the {@code text}; must not be negative
     * @return          the text after the {@code count}'th occurrence of the {@code delimiter};
     *                  if the {@code delimiter} does not occur {@code count} times, an empty string
     *                  is returned
     * @see #before(String, String, int)
     * @see #between(String, String, int, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #right(String, int)
     * @since 2.0.0
     */
    @EelFunction("after")
    @Nonnull
    public String after(@Nonnull String text, @Nonnull String delimiter, int count) {
        int index = nthIndexOf(text, delimiter, count, Direction.AFTER);

        return text.substring(index);
    }

    /**
     * Returns all the text between the {@code start}'th and the {@code end}'th occurrence of the
     * {@code delimiter} text.
     * @param text      the text from which characters are returned
     * @param delimiter the delimiter which should appear in the {@code text}
     * @param start     the first occurrence of {@code delimiter} in the {@code text}; must not be negative
     * @param end       the last occurrence count of {@code delimiter} in the {@code text}; must not be negative
     * @return          the text between the {@code start}'th and {@code end}'th occurrences of the
     *                  {@code delimiter}; if the {@code delimiter} does not occur {@code end} times,
     *                  an empty string is returned
     * @see #before(String, String, int)
     * @see #after(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @see #mid(String, int, int)
     * @since 2.0.0
     */
    @EelFunction("between")
    @Nonnull
    public String between(@Nonnull String text, @Nonnull String delimiter, int start, int end) {
        int startIndex = nthIndexOf(text, delimiter, start, Direction.AFTER);
        int endIndex = nthIndexOf(text, delimiter, end, Direction.BEFORE);

        return text.substring(startIndex, endIndex);
    }

    /**
     * Returns the number of times that the {@code search} phrase occurs in the {@code text}.
     * @param text      the text to be searched
     * @param search    the phrase to be counted
     * @return          the number of instances of {@code search} in {@code text};
     *                  if {@code search} is empty text, the value returned is the length of the {@code text}
     * @see #before(String, String, int)
     * @see #beforeFirst(String, String)
     * @see #afterFirst(String, String)
     * @see #beforeLast(String, String)
     * @see #afterLast(String, String)
     * @see #contains(String, String)
     * @since 2.0.0
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
     * Extracts characters from the specified {@code text} based on a Java Regular Expression.
     * @param text      the full text from which to extract data
     * @param regEx     the regular expression which should contain matching groups
     * @return          the extracted text
     * @see #matches(String, String)
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
     * Checks whether the specified {@code text} matches a Java regular expression.
     * @param text      the text to check
     * @param regEx     the regular expression against which the {@code text} is checked
     * @return          {@literal true} only if the {@code text} matches the {@code regEx}
     * @see #extract(String, String)
     */
    @EelFunction("matches")
    public boolean matches(@Nonnull String text, @Nonnull String regEx) {
        return text.matches(regEx);
    }


    /**
     * Replaces all instances of the literal {@code from} in the specified {@code text} with the literal {@code to}.
     * @param text      the text in which replacements will be made
     * @param from      the literal text to be replaced
     * @param to        the literal text to substitute
     * @return          the original {@code text} with all instances of {@code from} replaced by
     *                  {@code to}
     * @see #replaceEx(String, String, String)
     */
    @EelFunction("replace")
    @Nonnull
    public String replace(@Nonnull String text, @Nonnull String from, @Nonnull String to) {
        return text.replace(from, to);
    }

    
    /**
     * Replaces each part of the specified {@code text} that matches the Java regular expression
     * {@code regEx} with the literal text {@code to}.
     * @param text      the text in which replacements will be made
     * @param regEx     the regular expression to search for
     * @param to        the literal text to substitute
     * @return          the original {@code text} with all instances matching {@code regEx} replaced by
     *                  {@code to}
     * @see #replace(String, String, String)
     */
    @EelFunction("replaceEx")
    @Nonnull
    public String replaceEx(@Nonnull String text, @Nonnull String regEx, @Nonnull String to) {
        return text.replaceAll(regEx, to);
    }
    

    /**
     * Returns the {@code text} with any leading and trailing spaces removed.
     * @param text      the text to trim
     * @return          the {@code text} with no leading or trailing spaces
     */
    @EelFunction("trim")
    @Nonnull
    public String trim(@Nonnull String text) {
        return text.trim();
    }


    /**
     * Returns the length of the specified {@code text}, including any leading and trailing spaces.
     * @param text      the text to measure
     * @return          the length of {@code text}, including leading and trailing spaces
     * @see #isEmpty(String)
     * @see #isBlank(String)
     */
    @EelFunction("len")
    public int len(@Nonnull String text) {
        return text.length();
    }

    /**
     * Returns {@literal true} only if the specified {@code text} is empty.
     * Text that contains one or more spaces is not considered empty.
     * @param text      the text to check
     * @return          {@literal true} only if the {@code text} is empty
     * @see #len(String)
     * @see #isBlank(String)
     */
    @EelFunction("isEmpty")
    public boolean isEmpty(@Nonnull String text) {
        return text.isEmpty();
    }

    /**
     * Returns {@literal true} if the specified {@code text} is empty
     * or contains only whitespace.
     * @param text      the text to check
     * @return          {@literal true} if {@code text} is empty or contains only whitespace
     * @see #len(String)
     * @see #isEmpty(String)
     * @since 1.1
     */
    @EelFunction("isBlank")
    public boolean isBlank(@Nonnull String text) {
        return text.isBlank();
    }

    /**
     * Returns the 0-based index of the first occurrence of {@code subString} in the specified {@code text},
     * or the {@code defaultValue} if {@code subString} does not occur.
     * @param text          the text to check
     * @param subString     the text to search for
     * @param defaultValue  the value to return if {@code subString} is not found
     * @return              the 0-based index of the first occurrence of {@code subString} in {@code text}
     * @see #len(String)
     * @see #lastIndexOf(String, String, Value)
     */
    @EelFunction("indexOf")
    public int indexOf(@Nonnull String text,
                       @Nonnull String subString,
                       @Nonnull @DefaultArgument("-1") Value defaultValue) {
        int index = text.indexOf(subString);

        return (index == -1 ? defaultValue.asInt() : index);
    }

    /**
     * Returns the 0-based index of the last occurrence of {@code subString} in the specified {@code text},
     * or the {@code defaultValue} if {@code subString} does not occur.
     * @param text          the text to check
     * @param subString     the text to search for
     * @param defaultValue  the value to return if {@code subString} is not found
     * @return              the 0-based index of the last occurrence of {@code subString} in {@code text}
     * @see #len(String)
     * @see #indexOf(String, String, Value)
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
