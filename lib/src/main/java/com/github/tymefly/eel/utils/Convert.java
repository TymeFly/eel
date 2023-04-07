package com.github.tymefly.eel.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.validate.Preconditions;

import static java.util.Map.entry;

/**
 * Value conversion functions
 */
public class Convert {
    private static final Set<String> FALSE_VALUES = Set.of("false", "0");
    private static final Set<String> TRUE_VALUES = Set.of("true", "1");

    private static final int HEX_RADIX = 16;
    private static final int DEC_RADIX = 10;

    private static final Set<Class<?>> SUPPORTED = Set.of(
        String.class,
        Boolean.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        BigInteger.class,
        BigDecimal.class,
        ZonedDateTime.class
    );

    private static final Map<Class<?>, Class<?>> TO_BOXED = Map.ofEntries(
        entry(boolean.class, Boolean.class),
        entry(byte.class, Byte.class),
        entry(short.class, Short.class),
        entry(int.class, Integer.class),
        entry(long.class, Long.class),
        entry(float.class, Float.class),
        entry(double.class, Double.class)
    );

    private static final int YEAR_WIDTH = 4;
    private static final int MONTH_WIDTH = 2;
    private static final int DAY_WIDTH = 2;
    private static final int HOUR_WIDTH = 2;
    private static final int MINUTE_WIDTH = 2;
    private static final int SECOND_WIDTH = 2;

    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final DateTimeFormatter DATE_TO_STRING = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    private static final DateTimeFormatter STRING_TO_DATE = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, YEAR_WIDTH)
        .optionalStart()
            .optionalStart().appendLiteral('-').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendValue(ChronoField.MONTH_OF_YEAR, MONTH_WIDTH)
            .optionalStart()
                .optionalStart().appendLiteral('-').optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .appendValue(ChronoField.DAY_OF_MONTH, DAY_WIDTH)
                .optionalStart()
                    .optionalStart().appendLiteral('T').optionalEnd()
                    .optionalStart().appendLiteral(' ').optionalEnd()
                    .appendValue(ChronoField.HOUR_OF_DAY, HOUR_WIDTH)
                    .optionalStart()
                        .optionalStart().appendLiteral(':').optionalEnd()
                        .optionalStart().appendLiteral(' ').optionalEnd()
                        .appendValue(ChronoField.MINUTE_OF_HOUR, MINUTE_WIDTH)
                        .optionalStart()
                            .optionalStart().appendLiteral(':').optionalEnd()
                            .optionalStart().appendLiteral(' ').optionalEnd()
                            .appendValue(ChronoField.SECOND_OF_MINUTE, SECOND_WIDTH)
                        .optionalEnd()
                    .optionalEnd()
                .optionalEnd()
            .optionalEnd()
        .optionalEnd()
        .optionalStart().appendLiteral(' ').optionalEnd()
        .optionalStart().appendPattern("X").optionalEnd()
        .toFormatter();

    private Convert() {
    }


    /**
     * Convert a single value to another type in a flexible way. The conversion rules are:
     * <ul>
     *     <li>All values can be converted to Strings by calling {@link Object#toString()}</li>
     *     <li>Strings can be converted to Numeric types using their associated parse function.</li>
     *     <li>Numbers can be converted to Booleans using 'C'-like rules; {@literal 0} represents {@literal false} and
     *          {@literal 1} represents {@literal true}</li>
     *     <li>Strings can be converted to Booleans us by strictly comparing their (case insensitive) values</li>
     * </ul>
     * @param value     Value to convert
     * @param type      Type of the value to convert. This is of {@link String}, {@link Boolean}, {@link Byte},
     *                  {@link Short}, {@link Integer}, {@link Long}, {@link Float}, {@link Double},
     *                  {@link BigInteger} or {@link BigDecimal}
     * @param <T>       Type of the value to convert.
     * @return          converted value
     * @throws EelConvertException the value could not be converted
     * @throws IllegalArgumentException the type is not supported
     */
    @Nonnull
    public static <T> T to(@Nonnull Object value, @Nonnull Class<T> type) throws IllegalArgumentException {
        Object result;
        type = (Class<T>) TO_BOXED.getOrDefault(type, type);

        Preconditions.checkNotNull(value, "Internal ERROR: Can not convert Null");
        Preconditions.checkArgument(SUPPORTED.contains(type), "Unsupported type %s", type.getName());

        try {
            if (type == String.class) {
                result = toString(value);
            } else if (type == Boolean.class) {
                result = toBoolean(value);
            } else if (Number.class.isAssignableFrom(type)) {
                result = toNumber(value, (Class<? extends Number>) type);
            } else if (type == ZonedDateTime.class) {
                result = toDate(value);
            } else {                // Should not happen
                result = null;
            }
        } catch (RuntimeException e) {
            result = null;
        }

        if (result == null) {
            throw new EelConvertException("Can not convert '%s' from %s to %s",
                value, value.getClass().getSimpleName(), type.getSimpleName());
        }

        return type.cast(result);
    }


    @Nonnull
    private static String toString(@Nonnull Object value) {
        String result;

        if (value instanceof BigDecimal num) {
            result = num.toPlainString();
        } else if (value instanceof ZonedDateTime date) {
            result = DATE_TO_STRING.format(date);
        } else {
            result = value.toString();
        }

        return result;
    }


    @Nullable
    private static Boolean toBoolean(@Nonnull Object value) {
        Boolean result;

        value = value.toString().trim().toLowerCase();

        if (FALSE_VALUES.contains(value)) {
            result = Boolean.FALSE;
        } else if (TRUE_VALUES.contains(value)) {
            result = Boolean.TRUE;
        } else {
            result = null;
        }

        return result;
    }


    @Nullable
    private static <N extends Number> N toNumber(@Nonnull Object value, @Nonnull Class<N> type) {
        N result;

        if (value instanceof String str) {
            result = toNumber(str, type);
        } else if (value instanceof Number num) {
            result = toNumber(num, type);
        } else if (value instanceof Boolean bool) {
            result = toNumber((bool ? 1 : 0), type);
        } else if (value instanceof ZonedDateTime time) {
            result = toNumber(time.toEpochSecond(), type);
        } else {                // Should not happen
            result = null;
        }

        return result;
    }


    @Nullable
    private static <N extends Number> N toNumber(@Nonnull String value, @Nonnull Class<N> type)
            throws ClassCastException, NumberFormatException {
        Object result;
        int radix;

        value = value.trim();

        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
            radix = HEX_RADIX;
        } else {
            radix = DEC_RADIX;
        }

        if (type == Byte.class) {
            result = Byte.parseByte(value, radix);
        } else if (type == Short.class) {
            result = Short.parseShort(value, radix);
        } else if (type == Integer.class) {
            result = Integer.parseInt(value, radix);
        } else if (type == Long.class) {
            result = Long.parseLong(value, radix);
        } else if (type == Float.class) {
            result = (radix == HEX_RADIX ? new BigInteger(value, radix).floatValue() : Float.parseFloat(value));
        } else if (type == Double.class) {
            result = (radix == HEX_RADIX ? new BigInteger(value, radix).doubleValue() :  Double.parseDouble(value));
        } else if (type == BigDecimal.class) {
            result = (radix == HEX_RADIX ? new BigDecimal(new BigInteger(value, radix)) : new BigDecimal(value));
        } else if (type == BigInteger.class) {
            result = new BigInteger(value, radix);
        } else {                // Should not happen
            result = null;
        }

        return type.cast(result);
    }


    @Nullable
    private static <N extends Number> N toNumber(@Nonnull Number value, @Nonnull Class<N> type)
            throws ClassCastException, NumberFormatException {
        Object result;

        if (type == value.getClass()) {
            result = value;
        } else if (type == Byte.class) {
            result = value.byteValue();
        } else if (type == Short.class) {
            result = value.shortValue();
        } else if (type == Integer.class) {
            result = value.intValue();
        } else if (type == Long.class) {
            result = value.longValue();
        } else if (type == Float.class) {
            result = value.floatValue();
        } else if (type == Double.class) {
            result = value.doubleValue();
        } else if (type == BigDecimal.class) {
            result = new BigDecimal(value.toString());
        } else if (type == BigInteger.class) {
            result = toNumber(value, BigDecimal.class).toBigInteger();
        } else {                // Should not happen
            result = null;
        }

        return type.cast(result);
    }


    @Nullable
    private static ZonedDateTime toDate(@Nonnull Object value) {
        ZonedDateTime result;

        if (value instanceof String text) {
            result = toDate(text);
        } else if (value instanceof Number number) {
            result = ZonedDateTime.ofInstant(Instant.ofEpochSecond(number.longValue()), UTC);
        } else {
            result = null;
        }

        return result;
    }

    @Nonnull
    private static ZonedDateTime toDate(@Nonnull String text) {
        TemporalAccessor accessor = STRING_TO_DATE.parse(text.trim());
        int year = accessor.get(ChronoField.YEAR);
        int month = extractChrono(accessor, ChronoField.MONTH_OF_YEAR, 1);
        int day = extractChrono(accessor, ChronoField.DAY_OF_MONTH, 1);
        int hour = extractChrono(accessor, ChronoField.HOUR_OF_DAY, 0);
        int minute = extractChrono(accessor, ChronoField.MINUTE_OF_HOUR, 0);
        int second = extractChrono(accessor, ChronoField.SECOND_OF_MINUTE, 0);
        ZoneId zone = accessor.query(TemporalQueries.zone());
        ZonedDateTime result = ZonedDateTime.of(year,
            month,
            day,
            hour,
            minute,
            second,
            0,
            (zone == null ? UTC : zone));

        return result;
    }

    private static int extractChrono(@Nonnull TemporalAccessor accessor,
                                     @Nonnull ChronoField field,
                                     int defaultValue) {
        return accessor.isSupported(field) ? accessor.get(field) : defaultValue;
    }
}
