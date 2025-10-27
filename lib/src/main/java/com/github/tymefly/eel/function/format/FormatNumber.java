package com.github.tymefly.eel.function.format;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that convert numbers to text in different bases
 */
@PackagedEelFunction
public class FormatNumber {
    private static final int BINARY_RADIX = 2;
    private static final int OCTAL_RADIX = 8;
    private static final int HEX_RADIX = 16;

    /**
     * Returns the binary representation of the given {@code value} as text.
     * The result does not include a leading {@code 0b}.
     * This is equivalent to {@code $format.number(value, 2)}.
     * @param value     the value to convert.
     * @return          the number in binary text.
     * @see #formatNumber(BigInteger, int)
     * @since 2.1.0
     */
    @Nonnull
    @EelFunction("format.binary")
    public String formatBinary(@Nonnull BigInteger value) {
        return formatNumber(value, BINARY_RADIX);
    }

    /**
     * Returns the octal representation of the given {@code value} as text.
     * The result does not include a leading {@code 0c}.
     * This is equivalent to {@code $format.number(value, 8)}.
     * @param value     the value to convert.
     * @return          the number in octal text.
     * @see #formatNumber(BigInteger, int)
     */
    @Nonnull
    @EelFunction("format.octal")
    public String formatOctal(@Nonnull BigInteger value) {
        return formatNumber(value, OCTAL_RADIX);
    }

    /**
     * Returns the hexadecimal representation of the given {@code value} as text. The result does not include a
     * leading {@code 0x}.
     * This is equivalent to {@code $format.number(value, 16)}.
     * @param value     the value to convert.
     * @return          the number in hexadecimal text.
     * @see #formatNumber(BigInteger, int)
     */
    @Nonnull
    @EelFunction("format.hex")
    public String formatHex(@Nonnull BigInteger value) {
        return formatNumber(value, HEX_RADIX);
    }


    /**
     * Returns the representation of the given {@code value} as text in the specified {@code radix}.
     * @param value     the value to convert.
     * @param radix     the radix of the text representation. This must be in the range
     *                  {@value Character#MIN_RADIX} to {@value Character#MAX_RADIX}, inclusive.
     * @return          the number as text in the specified radix.
     * @throws IllegalArgumentException if the radix is out of range.
     */
    @Nonnull
    @EelFunction("format.number")
    public String formatNumber(@Nonnull BigInteger value,
                               int radix) throws IllegalArgumentException {
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            throw new IllegalArgumentException("Radix " + radix + " is out of range");
        }

        return value.toString(radix);
    }
}