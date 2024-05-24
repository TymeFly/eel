package com.github.tymefly.eel.function.format;

import java.util.MissingFormatArgumentException;
import java.util.Set;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.EelValue;
import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.CharSetBuilder;

/**
 * The PrintF function
 */
@PackagedEelFunction
public class PrintF {
    /** Disposable class that holds state for the stateless {@link #printf(String, EelValue...)}  function */
    private static class ArgumentParser {
        private static final String FORMAT_SPECIFIER
            = "%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%]).*";
        private static final int ARG_INDEX_GROUP = 1;
        private static final int TIME_GROUP = 5;
        private static final int CONVERSION_GROUP = 6;
        private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMAT_SPECIFIER);
        private static final Set<Character> LITERALS = new CharSetBuilder()
            .with('n')
            .with('%')
            .immutable();

        private final String format;
        private final EelValue[] arguments;
        private final Object[] parsed;
        private int next;

        private ArgumentParser(@Nonnull String format, @Nonnull EelValue[] arguments) {
            this.format = format;
            this.arguments = arguments;
            this.parsed = new Object[arguments.length];
            this.next = 0;
        }


        @Nonnull
        Object[] parse() {
            int conversionIndex = format.indexOf("%");
            int formatLength = format.length();

            while (conversionIndex != -1) {
                CharSequence remaining = format.subSequence(conversionIndex, formatLength);
                Matcher matcher = FORMAT_PATTERN.matcher(remaining);

                if (matcher.matches()) {
                    convert(matcher);

                    conversionIndex += matcher.end(CONVERSION_GROUP);
                } else {
                    throw new UnknownFormatConversionException(format);
                }

                conversionIndex = format.indexOf("%", conversionIndex);
            }

            return parsed;
        }

        private void convert(@Nonnull Matcher matcher) {
            char format = getFormatType(matcher);

            if (!LITERALS.contains(format)) {
                int argumentIndex = getIndex(matcher, format);

                convert(format, argumentIndex);
            }
        }

        private char getFormatType(@Nonnull Matcher matcher) {
            // If the time group is defined then the conversion group gives a date period. So in this case
            // 'd' would be "Day of Month"  rather than the more usual "decimal integer"
            //
            // We need to know the data type to convert to, so return 't' for all Date types.

            String timeGroup = matcher.group(TIME_GROUP);
            char format = (timeGroup != null ? 't' : matcher.group(CONVERSION_GROUP).charAt(0));

            return format;
        }

        private int getIndex(@Nonnull Matcher matcher, char format) {
            String argIndexGroup = matcher.group(ARG_INDEX_GROUP);
            int index;

            if (argIndexGroup != null) {
                String argumentIndex = argIndexGroup.substring(0, argIndexGroup.length() - 1);
                index = Integer.parseInt(argumentIndex) - 1;
            } else {
                index = next;
                next++;
            }

            if (index >= arguments.length) {
                throw new MissingFormatArgumentException("Format specifier '%" + format + "'");
            }

            return index;
        }

        private void convert(char format, int index) {
            if (parsed[index] == null) {
                switch(format) {
                    case 's', 'S' ->
                        parsed[index] = arguments[index].asText();
                    case 'c', 'C' ->
                        parsed[index] = arguments[index].asChar();
                    case 'b', 'B' ->
                        parsed[index] = arguments[index].asLogic();
                    case 'd', 'o' , 'x', 'X' ->
                        parsed[index] = arguments[index].asNumber().toBigInteger();
                    case 'e', 'E' , 'f', 'g', 'G' ->
                        parsed[index] = arguments[index].asNumber();
                    case 't', 'T' ->
                        parsed[index] = arguments[index].asDate();
                    default ->
                        throw new UnknownFormatConversionException(String.valueOf(format));
                }
            }
        }
    }


    /**
     * Entry point for the {@code printf} function. The following conversion characters are supported:
     * <ul>
     *  <li><b>Text:</b> {@code s}, {@code S}</li>
     *  <li><b>Character:</b> {@code c}, {@code C} - this is the first character of text passed to the function</li>
     *  <li><b>Logic:</b> {@code b}, {@code B}</li>
     *  <li><b>Integral Number:</b> {@code d}, {@code o}, {@code x}, {@code X}</li>
     *  <li><b>Real Number:</b> {@code e}, {@code E}, {@code f}, {@code g}, {@code G}</li>
     *  <li><b>Dates:</b> {@code t}, {@code T}</li>
     *  <li><b>Character literals:</b> {@code %}, {@code n}</li>
     * </ul>
     * All Java formatting indexes, flags, widths and precisions are supported.
     * <br>
     * The EEL syntax for this function is <code>printf( format, arguments... )</code>
     * @param format        the format
     * @param arguments     Arguments referenced by the format specifiers in the {@code format}.
     *                      If there are more arguments than format specifiers, the extra arguments are ignored
     * @return              formatted text
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html">
     *         Oracle Java 17 JavaDocs
     *      </a>
     * @since 1.1
     */
    @EelFunction(name = "printf")
    @Nonnull
    public String printf(@Nonnull String format, @Nonnull EelValue... arguments) {
        Object[] parameters = new ArgumentParser(format, arguments).parse();

        return String.format(format, parameters);
    }
}
