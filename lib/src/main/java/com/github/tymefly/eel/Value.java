package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.exception.EelInternalException;
import com.github.tymefly.eel.utils.BigDecimals;
import com.github.tymefly.eel.utils.Convert;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Holds values computed by the type Expression parser
 */
@Immutable
@SuppressFBWarnings(value="JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification="fields cache computed values")
class Value implements Result {
    public static final Value BLANK = new Value(Type.TEXT, "", null, null, null);
    public static final Value ZERO = new Value(Type.NUMBER, "0", BigDecimal.ZERO, false, null);
    public static final Value ONE = new Value(Type.NUMBER, "1", BigDecimal.ONE, true, null);
    public static final Value TRUE = new Value(Type.LOGIC, "true", BigDecimal.ONE, true, null);
    public static final Value FALSE = new Value(Type.LOGIC, "false", BigDecimal.ZERO, false, null);


    private final Type type;
    private String text;                    // Fields are not final so that type conversions can be cached
    private BigDecimal number;
    private Boolean logic;
    private ZonedDateTime date;

    private Value(@Nonnull Type type,
                  @Nullable String text,
                  @Nullable BigDecimal number,
                  @Nullable Boolean logic,
                  @Nullable ZonedDateTime date) {
        this.type = type;
        this.text = text;
        this.number = number;
        this.logic = logic;
        this.date = date;
    }


    @Nonnull
    static Value of(@Nonnull String value) {
        Value result;

        if (value.isEmpty()) {
            result = BLANK;
        } else {
            result = new Value(Type.TEXT, value, null, null, null);
        }

        return result;
    }

    @Nonnull
    static Value of(@Nonnull Number value) {
        BigDecimal cleaned = BigDecimals.toBigDecimal(value);
        Value result;

        if (BigDecimals.eq(cleaned, BigDecimal.ZERO)) {
            result = ZERO;
        } else if (BigDecimals.eq(cleaned, BigDecimal.ONE)) {
            result = ONE;
        } else {
            result = new Value(Type.NUMBER, null, cleaned, null, null);
        }

        return result;
    }

    @Nonnull
    static Value of(boolean value) {
        return (value ? TRUE : FALSE);
    }

    @Nonnull
    static Value of(@Nonnull ZonedDateTime value) {
        if (value.getNano() != 0) {                         // EEL Dates do not support fractions of a second
            value = value.withNano(0);                      //   This is because conventions to dates are based on secs
        }

        return new Value(Type.DATE, null, null, null, value);
    }

    @Override
    @Nonnull
    public Type getType() {
        return type;
    }

    @Override
    @Nonnull
    public String asText() {
        if (text == null) {
            text = switch (type) {
                case NUMBER -> Convert.to(number, String.class);
                case DATE -> Convert.to(date, String.class);
                default -> null;    // Value.TRUE and Value.FALSE were created with text values => no conversion
            };
        }

        if (text == null) {         // Should not happen - everything can be converted to TEXT
            throw new EelConvertException("Can not convert %s to TEXT", this);
        }

        return text;
    }

    @Override
    @Nonnull
    public BigDecimal asNumber() {
        if (number == null) {
            try {
                number = switch (type) {
                    case TEXT -> Convert.to(text, BigDecimal.class);
                    case DATE -> Convert.to(date, BigDecimal.class);
                    default -> null;    // Value.TRUE and Value.FALSE were created with numeric values => no conversion
                };
            } catch (RuntimeException e) {
                // Following code will generate a better exception.
            }
        }

        if (number == null) {
            throw new EelConvertException("Can not convert %s to NUMERIC", this);
        }

        return number;
    }

    /**
     * Convenience method to return a numeric value as a {@link BigInteger}.
     * <b>This may cause the value to be rounded</b>
     * @return {@link #asNumber()} but returning a BigInteger
     */
    @Nonnull
    BigInteger asBigInteger() {
        return asNumber().toBigInteger();
    }

    /**
     * Convenience method to return a numeric value as an integer.
     * <b>This may cause the value to be rounded and/or truncated</b>
     * @return {@link #asNumber()} but returning an integer
     */
    int asInteger() {
        return asNumber().intValue();
    }


    @Override
    public boolean asLogic() {
        if ((logic == null) && (type == Type.TEXT)) {
            logic = Convert.to(text, Boolean.class);
        }
        // Value.ZERO and Value.ONE were created with their logic values defined, so no conversion is required

        if (logic == null) {
            throw new EelConvertException("Can not convert %s to LOGIC", this);
        }

        return logic;
    }


    @Override
    @Nonnull
    public ZonedDateTime asDate() {
        if (date == null) {
            try {
                date = switch (type) {
                    case TEXT -> Convert.to(text, ZonedDateTime.class);
                    case NUMBER -> Convert.to(number, ZonedDateTime.class);
                    default -> null;    // Value.TRUE and Value.FALSE can't be converted => no conversion
                };
            } catch (RuntimeException e) {
                // Following code will generate a better exception.
            }
        }

        if (date == null) {
            throw new EelConvertException("Can not convert %s to DATE", this);
        }

        return date;
    }

    @Override
    public boolean equals(Object other) {
        boolean equal;

        if (this == other) {
            equal = true;
        } else if (other == null) {
            equal = false;
        } else if (getClass() != other.getClass()) {
            equal = false;
        } else {
            Value value = (Value) other;

            if (this.type != value.type) {
                equal = false;
            } else if (type == Type.TEXT) {
                equal = this.text.equals(value.text);
            } else if (type == Type.NUMBER) {
                equal = (this.number.compareTo(value.number) == 0);
            } else if (type == Type.LOGIC) {
                equal = (this.logic.equals(value.logic));
            } else if (type == Type.DATE) {
                equal = (this.date.equals(value.date));
            } else {            // Should not happen
                throw new EelInternalException("Unexpected type %s", type.toString());
            }
        }

        return equal;
    }

    @Override
    public int hashCode() {
        Object value;

        if (type == Type.NUMBER) {
            value = asBigInteger().toString();          // remove fractions (and any potential trailing zeros)
        } else if (type == Type.DATE) {
            value = date;
        } else {
            value = asText();
        }

        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Value{type=" + type + ", value=" + asText() + '}';
    }
}
