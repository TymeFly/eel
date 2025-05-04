package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.utils.BigDecimals;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Holds values computed by the type Expression parser
 */
@Immutable
@SuppressFBWarnings(value="JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification="fields cache computed values")
final class Constant extends AbstractValue implements Result {
    private static final ZonedDateTime ONE_DATE = EelContext.ZERO_DATE.plusSeconds(1);
    private static final ZonedDateTime TEN_DATE = EelContext.ZERO_DATE.plusSeconds(10);

    // As we keep a reference to these objects as static final fields they can't be garbage collected
    // and therefore will not be removed from the WeakHashMaps
    // These values are created with all the conversion values defined to save time later.
    private static final Constant BLANK = new Constant(Type.TEXT, "", null, null, null);
    private static final Constant ZERO = new Constant(Type.NUMBER, "0", BigDecimal.ZERO, false, EelContext.FALSE_DATE);
    private static final Constant ONE = new Constant(Type.NUMBER, "1", BigDecimal.ONE, true, ONE_DATE);
    private static final Constant TEN = new Constant(Type.NUMBER, "10", BigDecimal.TEN, true, TEN_DATE);
    private static final Constant TRUE = new Constant(Type.LOGIC, "true", BigDecimal.ONE, true, ONE_DATE);
    private static final Constant FALSE =
        new Constant(Type.LOGIC, "false", BigDecimal.ZERO, false, EelContext.FALSE_DATE);
    private static final Constant EPOCH_START_UTC =
        new Constant(Type.DATE, null, BigDecimal.ZERO, false, EelContext.FALSE_DATE);

    // The same value can end in multiple pools if they have different types
    // We can also have the same number in the NUMBER_POOL if the keys have different scales
    private static final Map<String, Constant> TEXT_POOL = new WeakHashMap<>();
    private static final Map<BigDecimal, Constant> NUMBER_POOL = new WeakHashMap<>();
    private static final Map<ZonedDateTime, Constant> DATE_POOL = new WeakHashMap<>();


    private final Type type;
    private String text;                    // Fields are not final so that type conversions can be cached
    private BigDecimal number;
    private Boolean logic;
    private ZonedDateTime date;


    static {
        TEXT_POOL.put(BLANK.asText(), BLANK);

        NUMBER_POOL.put(BigDecimal.ZERO, ZERO);
        NUMBER_POOL.put(BigDecimal.ONE, ONE);
        NUMBER_POOL.put(BigDecimal.TEN, TEN);

        DATE_POOL.put(EPOCH_START_UTC.asDate(), EPOCH_START_UTC);
    }


    private Constant(@Nonnull Type type,
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
    static Constant of(@Nonnull String value) {
        return TEXT_POOL.computeIfAbsent(value, v -> new Constant(Type.TEXT, v, null, null, null));
    }

    @Nonnull
    static Constant of(@Nonnull Number value) {
        BigDecimal backing = BigDecimals.toBigDecimal(value);
        Constant result = NUMBER_POOL.computeIfAbsent(backing, v -> new Constant(Type.NUMBER, null, v, null, null));

        return result;
    }

    @Nonnull
    static Constant of(boolean value) {
        return (value ? TRUE : FALSE);
    }

    @Nonnull
    static Constant of(@Nonnull ZonedDateTime value) {
        return DATE_POOL.computeIfAbsent(value, v -> new Constant(Type.DATE, null, null, null, v));
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
                case NUMBER -> Convert.toText(number);
                case DATE -> Convert.toText(date);
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
                    case TEXT -> Convert.toNumber(text);
                    case DATE -> Convert.toNumber(date);
                    default -> null;    // Value.TRUE and Value.FALSE were created with numeric values => no conversion
                };
            } catch (RuntimeException e) {
                throw new EelConvertException("Can not convert %s to NUMERIC", this, e);
            }
        }

        if (number == null) {
            throw new EelConvertException("Can not convert %s to NUMERIC", this);
        }

        return number;
    }


    @Override
    public boolean asLogic() {
        if (logic == null) {
            logic = switch (type) {
                case TEXT -> Convert.toLogic(text);
                case NUMBER -> Convert.toLogic(number);
                case DATE -> Convert.toLogic(date);
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
                    case TEXT -> Convert.toDate(text);
                    case NUMBER -> Convert.toDate(number);
                    default -> null;    // Value.TRUE and Value.FALSE were created with date values => no conversion
                };
            } catch (RuntimeException e) {
                throw new EelConvertException("Can not convert %s to DATE", this, e);
            }
        }

        if (date == null) {
            throw new EelConvertException("Can not convert %s to DATE", this);
        }

        return date;
    }

    @Nonnull
    @Override
    public File asFile() throws IOException {
        return FileFactory.from(asText());
    }


    @Nonnull
    @Override
    public Constant evaluate(@Nonnull SymbolsTable symbols) {
        return this;
    }

    @Override
    public boolean isConstant() {
        return true;
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
            Constant constant = (Constant) other;

            if (this.type != constant.type) {
                equal = false;
            } else {
                equal = switch (type) {
                    case TEXT -> this.text.equals(constant.text);
                    case NUMBER -> BigDecimals.eq(this.number, constant.number);
                    case LOGIC -> this.logic.equals(constant.logic);
                    case DATE -> this.date.equals(constant.date);
                };
            }
        }

        return equal;
    }

    @Override
    public int hashCode() {
        Object value;

        if (type == Type.NUMBER) {
            value = number;
        } else if (type == Type.DATE) {
            value = date;
        } else {
            value = asText();
        }

//        Object value = switch (type) {
//            case NUMBER -> number;
//            case DATE -> date;
//            default -> asText();
//        };

        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Constant{type=" + type + ", value=" + asText() + '}';
    }
}
