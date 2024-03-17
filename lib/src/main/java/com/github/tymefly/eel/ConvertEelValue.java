package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.udf.EelFunction;

/**
 * EEL Functions to perform type conversions.
 */
class ConvertEelValue {
    /** EEL Function classes required a public constructor */
    @SuppressWarnings("checkstyle:redundantmodifier")
    public ConvertEelValue() {
    }

    @Nonnull
    @EelFunction(name = "text")
    public EelValue toText(@Nonnull EelValue value) {
        String text = value.asText();

        return EelValue.of(text);
    }

    @Nonnull
    @EelFunction(name = "number")
    public EelValue toNumber(@Nonnull EelValue value) {
        BigDecimal number = value.asNumber();

        return EelValue.of(number);
    }

    @Nonnull
    @EelFunction(name = "logic")
    public EelValue toLogic(@Nonnull EelValue value) {
        boolean logic = value.asLogic();

        return EelValue.of(logic);
    }

    @Nonnull
    @EelFunction(name = "date")
    public EelValue toDate(@Nonnull EelValue value) {
        ZonedDateTime date = value.asDate();

        return EelValue.of(date);
    }
}
