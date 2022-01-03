package com.hubspot.jinjava.el.misc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;

import jakarta.el.ELException;

/**
 * Arithmetic Operations as specified in chapter 1.7.
 *
 * @author Christoph Beck
 */
public class NumberOperations {
    private static final Long LONG_ZERO = 0L;

    private static boolean isDotEe(String value) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            switch (value.charAt(i)) {
                case '.':
                case 'E':
                case 'e': return true;
                default: throw new IllegalArgumentException("Value " + value + " is not supported.");
            }
        }
        return false;
    }

    private static boolean isDotEe(Object value) {
        return value instanceof String && isDotEe((String)value);
    }

    private static boolean isFloatOrDouble(Object value) {
        return value instanceof Float || value instanceof Double;
    }

    private static boolean isFloatOrDoubleOrDotEe(Object value) {
        return isFloatOrDouble(value) || isDotEe(value);
    }

    private static boolean isBigDecimalOrBigInteger(Object value) {
        return value instanceof BigDecimal || value instanceof BigInteger;
    }

    private static boolean isBigDecimalOrFloatOrDoubleOrDotEe(Object value) {
        return value instanceof BigDecimal || isFloatOrDoubleOrDotEe(value);
    }

    public static Number add(TypeConverter converter, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return LONG_ZERO;
        }
        if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
            return converter.convert(o1, BigDecimal.class).add(converter.convert(o2, BigDecimal.class));
        }
        if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
            if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
                return converter.convert(o1, BigDecimal.class).add(converter.convert(o2, BigDecimal.class));
            }
            return converter.convert(o1, Double.class) + converter.convert(o2, Double.class);
        }
        if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
            return converter.convert(o1, BigInteger.class).add(converter.convert(o2, BigInteger.class));
        }
        return converter.convert(o1, Long.class) + converter.convert(o2, Long.class);
    }

    public static Number sub(TypeConverter converter, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return LONG_ZERO;
        }
        if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
            return converter.convert(o1, BigDecimal.class).subtract(converter.convert(o2, BigDecimal.class));
        }
        if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
            if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
                return converter.convert(o1, BigDecimal.class).subtract(converter.convert(o2, BigDecimal.class));
            }
            return converter.convert(o1, Double.class) - converter.convert(o2, Double.class);
        }
        if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
            return converter.convert(o1, BigInteger.class).subtract(converter.convert(o2, BigInteger.class));
        }
        return converter.convert(o1, Long.class) - converter.convert(o2, Long.class);
    }

    public static Number mul(TypeConverter converter, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return LONG_ZERO;
        }
        if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
            return converter.convert(o1, BigDecimal.class).multiply(converter.convert(o2, BigDecimal.class));
        }
        if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
            if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
                return converter.convert(o1, BigDecimal.class).multiply(converter.convert(o2, BigDecimal.class));
            }
            return converter.convert(o1, Double.class) * converter.convert(o2, Double.class);
        }
        if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
            return converter.convert(o1, BigInteger.class).multiply(converter.convert(o2, BigInteger.class));
        }
        return converter.convert(o1, Long.class) * converter.convert(o2, Long.class);
    }

    public static Number div(TypeConverter converter, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return LONG_ZERO;
        }
        if (isBigDecimalOrBigInteger(o1) || isBigDecimalOrBigInteger(o2)) {
            return converter.convert(o1, BigDecimal.class).divide(converter.convert(o2, BigDecimal.class), BigDecimal.ROUND_HALF_UP);
        }
        return converter.convert(o1, Double.class) / converter.convert(o2, Double.class);
    }

    public static Number mod(TypeConverter converter, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return LONG_ZERO;
        }
        if (isBigDecimalOrFloatOrDoubleOrDotEe(o1) || isBigDecimalOrFloatOrDoubleOrDotEe(o2)) {
            return converter.convert(o1, Double.class) % converter.convert(o2, Double.class);
        }
        if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
            return converter.convert(o1, BigInteger.class).remainder(converter.convert(o2, BigInteger.class));
        }
        return converter.convert(o1, Long.class) % converter.convert(o2, Long.class);
    }

    public static Number neg(TypeConverter converter, Object value) {
        if (value == null) {
            return LONG_ZERO;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal)value).negate();
        }
        if (value instanceof BigInteger) {
            return ((BigInteger)value).negate();
        }
        if (value instanceof Double) {
            return -(Double) value;
        }
        if (value instanceof Float) {
            return -(Float) value;
        }
        if (value instanceof String) {
            if (isDotEe((String)value)) {
                return -converter.convert(value, Double.class);
            }
            return -converter.convert(value, Long.class);
        }
        if (value instanceof Long) {
            return -(Long) value;
        }
        if (value instanceof Integer) {
            return -(Integer) value;
        }
        if (value instanceof Short) {
            return (short) -(Short) value;
        }
        if (value instanceof Byte) {
            return (byte) -(Byte) value;
        }
        throw new ELException(MessageFormat.format("Cannot negate ''{0}''", value.getClass()));
    }
}
