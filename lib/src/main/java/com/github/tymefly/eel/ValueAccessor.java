package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * Defines the contract for accessing values managed by EEL
 */
sealed interface ValueAccessor permits EelValue, Result {
        /**
     * Returns the type of the data in the result
     * @return the type of the data in the result
     */
    @Nonnull
    Type getType();

    /**
     * Returns the result of the Expression as a string. If {@link #getType()} does not return {@link Type#TEXT}
     * then this Result will convert the value to a String as follows:
     * <ul>
     *  <li>A {@link Type#NUMBER} value can be converted a {@link Type#TEXT} type using {@link Object#toString()}
     *      associated parse functions</li>
     *  <li>A {@link Type#LOGIC} value can be converted a {@link Type#TEXT} type as either the literal strings
     *      {@literal true} or {@literal false}</li>
     *  <li>A {@link Type#DATE} value can be converted to a {@link Type#TEXT} as an ISO 8601 formatted string</li>
     * </ul>
     * @return the result of the Expression as a stringN
     */
    @Nonnull
    String asText();

    /**
     * Returns the result of the Expression as a number. If {@link #getType()} does not return {@link Type#NUMBER}
     * then this Result will attempt to convert value to a BigDecimal as follows:
     * <ul>
     *  <li>A {@link Type#TEXT} value can be converted a {@link Type#NUMBER} type using one of the
     *      associated parse functions</li>
     *  <li>A {@link Type#LOGIC} value can be converted a {@link Type#NUMBER} type using 'C'-like rules;
     *      {@literal 0} represents {@literal false} and {@literal 1} represents {@literal true}</li>
     *  <li>A {@link Type#DATE} value can be converted to a {@link Type#NUMBER} by taking the number of
     *      seconds that have elapsed since midnight on the first of January 1970</li>
     * </ul>
     * @return the result of the Expression as a number
     * @throws EelConvertException if the underlying type could not be converted to a Number
     */
    @Nonnull
    BigDecimal asNumber() throws EelConvertException;


    /**
     * Returns the result of the Expression as a boolean value. If {@link #getType()} does not return
     * {@link Type#LOGIC} then this Result will attempt to convert value to a Boolean as follows:
     * <ul>
     *  <li>A {@link Type#TEXT} value can be converted a {@link Boolean} by case-insensitive match against the literals
     *      {@literal true} and {@literal false}. Other values are invalid</li>
     *  <li>A {@link Type#NUMBER} value can be converted a {@link Type#LOGIC} type using 'C'-like rules;
     *      {@literal 0} represents {@literal false} and {@literal 1} represents {@literal true}.
     *      Other values are invalid</li>
     *  <li>A {@link Type#DATE} value can not be converted to a logic value
     * </ul>
     * @return the result of the Expression as a boolean
     * @throws EelConvertException if the underlying type could not be converted to a boolean
     */
    boolean asLogic() throws EelConvertException;


    /**
     * Returns the result of the Expression as a ZonedDateTime value. If {@link #getType()} does not return
     * {@link Type#DATE} then this Result will attempt to convert value to a ZonedDateTime as follows:
     * <ul>
     *  <li>A {@link Type#TEXT} value can be converted a {@link ZonedDateTime} by parsing ISO8601 formatted strings</li>
     *  <li>A {@link Type#NUMBER} value can be converted a {@link ZonedDateTime} by using a second offset from
     *          midnight on the first of January 1970</li>
     *  <li>A {@link Type#LOGIC} value can not be converted to a {@link ZonedDateTime}</li>
     * </ul>
     * @return the result of the Expression as a ZonedDateTime
     * @throws EelConvertException if the underlying type could not be converted to a date
     */
    @Nonnull
    ZonedDateTime asDate();
}
