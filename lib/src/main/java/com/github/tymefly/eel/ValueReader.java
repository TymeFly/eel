package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * This interface defines a consistent interface for reading EEL {@link Value}s and {@link Result}s
 * @since 3.0.0
 */
public sealed interface ValueReader permits Value, Result {
    /**
     * Returns the type of the EEL value
     * @return the type of the EEL value
     */
    @Nonnull
    Type getType();

    /**
     * Returns the EEL Value as a string. If {@link #getType()} does not return {@link Type#TEXT}
     * then this method will convert the value to a String using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#NUMBER} values are converted to Text as their plain (non-scientific) decimal representation</li>
     *  <li>{@link Type#LOGIC} values {@literal true} and {@literal false} are converted to Text values
     *      {@literal "true"} and {@literal "false"} respectively</li>
     *  <li>{@link Type#DATE} values are converted to Text in the format `yyyy-MM-dd'T'HH:mm:ssX`</li>
     * </ul>
     * @return the EEL Value as a string
     */
    @Nonnull
    String asText();

    /**
     * Returns the EEL Value as a number. If {@link #getType()} does not return {@link Type#NUMBER}
     * then this method will attempt to convert value to a BigDecimal using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#TEXT} values are converted to Numbers if they are in one of the following formats
     *      <ul>
     *          <li>Decimal integers (e.g. `1234`)</li>
     *          <li>Binary integers (e.g. `0b1010`)</li>
     *          <li>Octal integers (e.g. `0c1234567`)</li>
     *          <li>Hexadecimal integers (e.g. `0x89ab`)</li>
     *          <li>Decimals with fractional parts (e.g. `123.456789`)</li>
     *          <li>Scientific format (e.g. `2.99792e8`). Exponents may be negative</li>
     *      </ul>
     *      The underscore character (`_`) may appear between digits in a numerical literal for grouping purposes.
     *      This is to make numeric literals more readable, but otherwise they have no effect.
     *  </li>
     *  <li>{@link Type#LOGIC} values {@literal true} and {@literal false} are converted to numeric values
     *      {@literal "1"} and {@literal "0"} respectively</li>
     *  <li>{@link Type#DATE} values are converted to Numbers by taking the number of elapsed seconds since
     *      `1970-01-01 00:00:00` in the UTC zone</li>
     * </ul>
     * @return the EEL Value as a number
     * @throws EelConvertException if the underlying type could not be converted to a Number
     */
    @Nonnull
    BigDecimal asNumber() throws EelConvertException;


    /**
     * Returns the EEL Value as a boolean value. If {@link #getType()} does not return {@link Type#LOGIC} then this
     * method will attempt to convert value to a Boolean using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#TEXT} values {@literal true} and {@literal 1} are converted to the Logic value {@literal true}.
     *   Text values {@literal false} and {@literal 0} and empty text are converted to the Logic value {@literal false}.
     *   Other values text are invalid</li>
     *  <li>Positive numbers are converted to Logical {@literal true}.
     *      Negative numbers and zero are converted to Logical {@literal false}</li>
     *  <li>{@link Type#DATE} values `1970-01-01 00:00:00` and earlier are converted to {@literal true}.
     *      All other dates are converted to {@literal false}</li>
     * </ul>
     * @return the EEL Value as a boolean
     * @throws EelConvertException if the underlying type could not be converted to a boolean
     */
    boolean asLogic() throws EelConvertException;


    /**
     * Returns the EEL Value as a ZonedDateTime value. If {@link #getType()} does not return {@link Type#DATE} then
     * this method will attempt to convert value to a ZonedDateTime using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#TEXT} values can be converted to Dates by parsing them as ISO 8601 formatted values
     *      ({@literal `yyyy-MM-dd'T'HH:mm:ssX`}). The precision of these strings is flexible; they can contain as
     *      little as a 4-digit year or be specified to the second, but each period but be fully defined.
     *      If a time zone is not given then UTC is assumed.
     *      The `T`, `-` and `:` separator characters can be replaced with a single space or omitted entirely.
     *  </li>
     *  <li>{@link Type#NUMBER} values are converted to Dates as the number of elapsed seconds since
     *      1970-01-01 00:00:00 in the UTC zone.</li>
     *  <li>{@link Type#LOGIC} {@literal true} is converted to date values {@literal 1970-01-01 00:00:01Z}.
     *      {@literal false} is converted to {@literal 1970-01-01 00:00:00Z}
     * </ul>
     * @return the EEL Value as a ZonedDateTime
     * @throws EelConvertException if the underlying type could not be converted to a date
     */
    @Nonnull
    ZonedDateTime asDate();


    /**
     * A helper method that will return the EEL Value as a {@code char}. This is done by converting the value
     * to text and returning the first character.
     * @return the EEL Value as a character
     * @throws EelConvertException if the text is empty
     * @see #asText()
     * @since 3.0.0
     */
    char asChar() throws EelConvertException;

    /**
     * Convenience method to return a numeric value as a {@link BigInteger}.
     * <b>This may cause the value to be rounded</b>
     * @return {@link #asNumber()} but as a BigInteger
     * @see #asNumber()
     * @since 3.0.0
     */
    @Nonnull
    BigInteger asBigInteger();

    /**
     * Convenience method to return a numeric value as a {@code double}.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} but as a double
     * @see #asNumber()
     * @since 3.0.0
     */
    double asDouble();

    /**
     * Convenience method to return a numeric value as a {@code long}.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} but as a long
     * @see #asNumber()
     * @since 3.0.0
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
