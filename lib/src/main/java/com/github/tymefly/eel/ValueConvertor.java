package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * Defines the contract for converting EEL values.
 */
sealed interface ValueConvertor permits EelValue, Result {
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
     *   Text values {@literal false} and {@literal 0} and empty text are converted to the Logic value {@literal true}.
     *   Other values text are invalid</li>
     *  <li>Positive numbers are converted to Logical {@literal true}.
     *      Negative numbers and zero are converted to Logical {@literal true}</li>
     *  <li>{@link Type#DATE} values `1970-01-01 00:00:00` and earlier are converted to {@literal true}.
     *      All other dates are converted to {@literal true}</li>
     * </ul>
     * @return the EEL Value as a boolean
     * @throws EelConvertException if the underlying type could not be converted to a boolean
     */
    boolean asLogic() throws EelConvertException;


    /**
     * Returns the EEL Value as a ZonedDateTime value. If {@link #getType()} does not return {@link Type#DATE} then
     * this method will attempt to convert value to a ZonedDateTime using the rules of the EEL language:
     * <ul>
     *  <li>{@link Type#TEXT} values can be converted to Dates by parsing them as ISO 8601 formatted values</li>
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
}
