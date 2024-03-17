package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.utils.BigDecimals;
import com.github.tymefly.eel.utils.Convert;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Holds values computed by the type Expression parser
 */
@Immutable
@SuppressFBWarnings(value="JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification="fields cache computed values")
class Value implements EelValue, Result {
    private static final ZonedDateTime ONE_DATE = EelContext.FALSE_DATE.plusSeconds(1);
    private static final ZonedDateTime TEN_DATE = EelContext.FALSE_DATE.plusSeconds(10);

    private static final Value BLANK = new Value(Type.TEXT, "", null, null, null);
    private static final Value ZERO = new Value(Type.NUMBER, "0", BigDecimal.ZERO, false, EelContext.FALSE_DATE);
    private static final Value ONE = new Value(Type.NUMBER, "1", BigDecimal.ONE, true, ONE_DATE);
    private static final Value TEN = new Value(Type.NUMBER, "10", BigDecimal.TEN, true, TEN_DATE);
    private static final Value TRUE = new Value(Type.LOGIC, "true", BigDecimal.ONE, true, ONE_DATE);
    private static final Value FALSE = new Value(Type.LOGIC, "false", BigDecimal.ZERO, false, EelContext.FALSE_DATE);
    private static final Value EPOCH_START_UTC =
        new Value(Type.DATE, null, BigDecimal.ZERO, false, EelContext.FALSE_DATE);


    private static final Map<String, Value> TEXT_POOL = new WeakHashMap<>();
    private static final Map<BigDecimal, Value> NUMBER_POOL = new WeakHashMap<>();
    private static final Map<ZonedDateTime, Value> DATE_POOL = new WeakHashMap<>();


    private final Type type;
    private String text;                    // Fields are not final so that type conversions can be cached
    private BigDecimal number;
    private Boolean logic;
    private ZonedDateTime date;


    static {
        // As we keep a reference to these objects as static final fields they can't be garbage collected
        // and therefore will not be removed from the WeakHashMap
        // These values are created with all the conversion values defined to save time later.
        TEXT_POOL.put(BLANK.asText(), BLANK);

        NUMBER_POOL.put(ZERO.asNumber(), ZERO);
        NUMBER_POOL.put(ONE.asNumber(), ONE);
        NUMBER_POOL.put(TEN.asNumber(), TEN);

        DATE_POOL.put(EPOCH_START_UTC.asDate(), EPOCH_START_UTC);
    }


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
        return TEXT_POOL.computeIfAbsent(value, v -> new Value(Type.TEXT, v, null, null, null));
    }

    @Nonnull
    static Value of(@Nonnull Number value) {
        BigDecimal cleaned = BigDecimals.toBigDecimal(value);
        Value result;

        // Need to be careful about the scale
        if (BigDecimals.eq(cleaned, BigDecimal.ZERO)) {
            result = ZERO;
        } else if (BigDecimals.eq(cleaned, BigDecimal.ONE)) {
            result = ONE;
        } else {
            result = NUMBER_POOL.computeIfAbsent(cleaned, v -> new Value(Type.NUMBER, null, v, null, null));
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

        return DATE_POOL.computeIfAbsent(value, v -> new Value(Type.DATE, null, null, null, v));
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
                default -> null;    // ValueImpl.TRUE and ValueImpl.FALSE were created with text values => no conversion
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
        if (logic == null) {
            logic = switch (type) {
                case TEXT -> Convert.to(text, Boolean.class);
                case NUMBER -> Convert.to(number, Boolean.class);
                case DATE -> Convert.to(date, Boolean.class);
                default -> null;    // ValueImpl.TRUE and ValueImpl.FALSE already have the correct logic value
            };
        }

        if (logic == null) {        // Should not happen - all types can be converted to Logic
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
                    default -> null;    // Value.TRUE and Value.FALSE were created with date values => no conversion
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
            } else {
                equal = switch (type) {
                    case TEXT -> this.text.equals(value.text);
                    case NUMBER -> BigDecimals.eq(this.number, value.number);
                    case LOGIC -> this.logic.equals(value.logic);
                    case DATE -> this.date.equals(value.date);
                };
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
