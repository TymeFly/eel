package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.StringUtils;

/**
 * Case conversion function
 */
@PackagedEelFunction
public class CaseConversion {

    /**
     * Entry point for the {@code upper} function, which returns the {@code text} converted to upper case
     * <br>
     * The EEL syntax for this function is <code>upper( text )</code>
     * @param text  to convert to upper case
     * @return text in upper case
     * @see #lower(String)
     * @see #title(String)
     */
    @EelFunction("upper")
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
    @EelFunction("lower")
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
    @EelFunction("title")
    @Nonnull
    public String title(@Nonnull String text) {
        return StringUtils.toTitleCase(text);
    }


}
