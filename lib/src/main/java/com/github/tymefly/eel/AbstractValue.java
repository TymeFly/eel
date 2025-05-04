package com.github.tymefly.eel;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * Base class for all Value implementations.
 */
abstract sealed class AbstractValue implements Value, Term permits Constant, ValueArgument {
    @Override
    @Nonnull
    public BigInteger asBigInteger() {
        return asNumber().toBigInteger();
    }

    @Override
    public double asDouble() {
        return asNumber().doubleValue();
    }

    @Override
    public long asLong() {
        return asNumber().longValue();
    }

    @Override
    public int asInt() {
        return asNumber().intValue();
    }


    @Override
    public char asChar() {
        String text = asText();

        if (text.isEmpty()) {
            throw new EelConvertException("Empty text can not be converted to a char");
        }

        return text.charAt(0);
    }
}
