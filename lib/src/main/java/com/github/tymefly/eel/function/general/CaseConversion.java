package com.github.tymefly.eel.function.general;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;
import com.github.tymefly.eel.utils.StringUtils;

/**
 * Functions that convert text to a specified case.
 * @since 1.0
 */
@PackagedEelFunction
public class CaseConversion {

    /**
     * Converts all the characters in the supplied text to upper case.
     * @param text      the text to convert to upper case
     * @return          the {@code text} in upper case
     * @see #lower(String)
     * @see #title(String)
     * @since 1.0
     */
    @EelFunction("upper")
    @Nonnull
    public String upper(@Nonnull String text) {
        return text.toUpperCase();
    }

    /**
     * Converts all the characters in the supplied text to lower case.
     * @param text      the text to convert to lower case
     * @return          the {@code text} in lower case
     * @see #upper(String)
     * @see #title(String)
     * @since 1.0
     */
    @EelFunction("lower")
    @Nonnull
    public String lower(@Nonnull String text) {
        return text.toLowerCase();
    }


    /**
     * Converts all the characters in the supplied text to title case.
     * @param text      the text to convert to title case
     * @return          the {@code text} in title case
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
