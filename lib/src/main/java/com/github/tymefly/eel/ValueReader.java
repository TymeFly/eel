package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * This interface defines a consistent interface for reading EEL {@link Value}s and {@link Result}s.
 * @since 3.0
 */
public sealed interface ValueReader permits Value, Result {
    /**
     * Returns the type of the EEL value.
     * @return the type of the EEL value
     * @since 3.0
     */
    @Nonnull
    Type getType();

    /**
     * Returns the EEL Value as a string. If {@link #getType()} does not return {@link Type#TEXT}
     * then this method will convert the value to a String using the rules of the EEL language:
     * <ul>
     *  <li>
     *      {@link Type#NUMBER} values are converted to Text as their plain (non-scientific) decimal representation
     *  </li>
     *  <li>
     *      {@link Type#LOGIC} values {@literal true} and {@literal false} are converted to Text values
     *      {@literal "true"} and {@literal "false"} respectively</li>
     *  <li>
     *      {@link Type#DATE} values are converted to Text in the format `yyyy-MM-dd'T'HH:mm:ssX`
     *  </li>
     * </ul>
     * @return the EEL Value as a string
     * @since 3.0
     */
    @Nonnull
    String asText();

    /**
     * Returns the EEL Value as a number. If {@link #getType()} does not return {@link Type#NUMBER}
     * then this method will attempt to convert value to a {@link BigDecimal} using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#TEXT} values are converted to Numbers if they are in one of the following formats:
     *      <ul>
     *          <li>Decimal integers (e.g. `1234`)</li>
     *          <li>Binary integers (e.g. `0b1010`)</li>
     *          <li>Octal integers (e.g. `0c1234567`)</li>
     *          <li>Hexadecimal integers (e.g. `0x89ab`)</li>
     *          <li>Decimals with fractional parts (e.g. `123.456789`)</li>
     *          <li>Scientific format (e.g. `2.99792e8`). Exponents may be negative</li>
     *      </ul>
     *      The underscore character (`_`) may appear between digits in a numerical literal for readability but has
     *      no effect.
     *  </li>
     *  <li>
     *      {@link Type#LOGIC} values {@literal true} and {@literal false} are converted to numeric values
     *      {@literal 1} and {@literal 0} respectively
     *  </li>
     *  <li>
     *      {@link Type#DATE} values are converted to Numbers by taking the number of elapsed seconds since
     *      `1970-01-01 00:00:00` in the UTC zone
     *  </li>
     * </ul>
     * @return the EEL Value as a number
     * @throws EelConvertException if the underlying type could not be converted to a Number
     * @since 3.0
     */
    @Nonnull
    BigDecimal asNumber() throws EelConvertException;

    /**
     * Returns the EEL Value as a logic (boolean) value. If {@link #getType()} does not return {@link Type#LOGIC},
     * this method will attempt to convert value to a logic value using the rules of the EEL language:
     * <ul>
     *   <li>
     *       {@link Type#TEXT} values {@literal true} and {@literal 1} are converted to {@literal true};
     *       {@literal false}, {@literal 0}, or empty text are converted to {@literal false};
     *       other text values are invalid
     *   </li>
     *   <li>
     *       {@link Type#NUMBER} positive numbers are converted to {@literal true};
     *       negative numbers and zero are converted to {@literal false}
     *   </li>
     *   <li>
     *       {@link Type#DATE} values earlier than or equal to {@literal 1970-01-01 00:00:00} UTC are converted to
     *      {@literal true};
     *       all other dates are converted to {@literal false}
     *   </li>
     * </ul>
     * @return the EEL Value as a boolean
     * @throws EelConvertException if the underlying type could not be converted to a boolean
     * @since 3.0
     */
    boolean asLogic() throws EelConvertException;

    /**
     * Returns the EEL Value as a {@link ZonedDateTime}. If {@link #getType()} does not return {@link Type#DATE},
     * this method will attempt to convert value to a ZonedDateTime using the rules of the EEL language:
     * <ul>
     *   <li>
     *       {@link Type#TEXT} values can be converted to Dates by parsing them as ISO 8601 formatted values
     *       ({@literal `yyyy-MM-dd'T'HH:mm:ssX`}). The precision of these strings is flexible; they can contain as
     *       little as a 4-digit year or be specified to the second, but each period must be fully defined if present.
     *       If a time zone is not given then UTC is assumed. The {@literal T}, {@literal -} and {@literal :}
     *       separator characters can be replaced with a single space or omitted entirely.
     *   </li>
     *   <li>
 *          {@link Type#NUMBER} values are interpreted as seconds elapsed since {@literal 1970-01-01 00:00:00} UTC.
     *   </li>
     *   <li>
     *       {@link Type#LOGIC} {@literal true} is converted to {@literal 1970-01-01T00:00:01Z};
     *       {@literal false} is converted to {@literal 1970-01-01T00:00:00Z}.
 *       </li>
     * </ul>
     * @return the EEL Value as a ZonedDateTime
     * @throws EelConvertException if the underlying type could not be converted to a date
     * @since 3.0
     */
    @Nonnull
    ZonedDateTime asDate();

    /**
     * A helper method that will return the EEL Value as a {@code char}.
     * This is done by converting the value to text and returning the first character.
     * @return the EEL Value as a character
     * @throws EelConvertException if the text is empty
     * @see #asText()
     * @since 3.0
     */
    char asChar() throws EelConvertException;

    /**
     * Convenience method to return a numeric value as a {@link BigInteger}.
     * <b>This may cause the value to be rounded</b>
     * @return {@link #asNumber()} but as a BigInteger
     * @see #asNumber()
     * @since 3.0
     */
    @Nonnull
    BigInteger asBigInteger();

    /**
     * Convenience method to return a numeric value as a {@code double}.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} but as a double
     * @see #asNumber()
     * @since 3.0
     */
    double asDouble();

    /**
     * Convenience method to return a numeric value as a {@code long}.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} as a long value
     * @see #asNumber()
     * @since 3.0
     */
    long asLong();

    /**
     * Convenience method to return a numeric value as an {@code int}.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} but as an int
     * @see #asNumber()
     * @since 3.0.0
     */
    int asInt();
}