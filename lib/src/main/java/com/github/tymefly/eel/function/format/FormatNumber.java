package com.github.tymefly.eel.function.format;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;
import com.github.tymefly.eel.udf.PackagedEelFunction;

/**
 * EEL functions that converts NUMBER to TEXT with a different base
 */
@PackagedEelFunction
public class FormatNumber {
    private static final int OCTAL_RADIX = 8;
    private static final int HEX_RADIX = 16;

    /**
     * Entry point for the {@code format.octal} function that converts a number to octal Text.
     * Logically this is the same as the more verbose {@code format.number( <value>, 8) }
     * <br>
     * The EEL syntax for this function is <code>format.octal( value )</code>
     * @param value     Value to convert
     * @return the number in octal.
     * @see #formatNumber(BigInteger, int)
     */
    @Nonnull
    @EelFunction(name = "format.octal")
    public String formatOctal(@Nonnull BigInteger value) {
        return formatNumber(value, OCTAL_RADIX);
    }

    /**
     * Entry point for the {@code format.hex} function that converts a number to Hex Text. This will not have a
     * leading {@literal 0x}
     * Logically this is the same as the more verbose {@code format.number( <value>, 16) }
     * <br>
     * The EEL syntax for this function is <code>format.hex( value )</code>
     * @param value     Value to convert
     * @return the number in hex.
     * @see #formatNumber(BigInteger, int)
     */
    @Nonnull
    @EelFunction(name = "format.hex")
    public String formatHex(@Nonnull BigInteger value) {
        return formatNumber(value, HEX_RADIX);
    }


    /**
     * Entry point for the {@code format.hex} function that converts a number to Text in the given radix.
     * <br>
     * The EEL syntax for this function is <code>format.number( value { , radix } )</code>
     * @param value     Value to convert
     * @param radix     radix of the String representation. This is in the range {@link Character#MIN_RADIX} to
     *                  {@link Character#MAX_RADIX} inclusive.
     * @return the number in hex.
     * @throws IllegalArgumentException if the radix is out of range
     */
    @Nonnull
    @EelFunction(name = "format.number")
    public String formatNumber(@Nonnull BigInteger value,
                               int radix) throws IllegalArgumentException {
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            throw new IllegalArgumentException("Radix " + radix + " is out of range");
        }

        return value.toString(radix);
    }
}