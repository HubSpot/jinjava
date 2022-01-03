package com.hubspot.jinjava.el.misc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.el.ELException;

public class BooleanOperations {
    private static final Set<Class<? extends Number>> SIMPLE_INTEGER_TYPES = new HashSet<>();
    private static final Set<Class<? extends Number>> SIMPLE_FLOAT_TYPES = new HashSet<>();
    public static final String ERROR_MSG = "Cannot compare ''{0}'' and ''{1}''";

    static {
        SIMPLE_INTEGER_TYPES.add(Byte.class);
        SIMPLE_INTEGER_TYPES.add(Short.class);
        SIMPLE_INTEGER_TYPES.add(Integer.class);
        SIMPLE_INTEGER_TYPES.add(Long.class);
        SIMPLE_FLOAT_TYPES.add(Float.class);
        SIMPLE_FLOAT_TYPES.add(Double.class);
    }

    @SuppressWarnings("unchecked")
    private static boolean lt0(TypeConverter converter, Object o1, Object o2) {
        Class<?> t1 = o1.getClass();
        Class<?> t2 = o2.getClass();
        if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigDecimal.class).compareTo(converter.convert(o2, BigDecimal.class)) < 0;
        }
        if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
            return converter.convert(o1, Double.class) < converter.convert(o2, Double.class);
        }
        if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigInteger.class).compareTo(converter.convert(o2, BigInteger.class)) < 0;
        }
        if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
            return converter.convert(o1, Long.class) < converter.convert(o2, Long.class);
        }
        if (t1 == String.class || t2 == String.class) {
            return converter.convert(o1, String.class).compareTo(converter.convert(o2, String.class)) < 0;
        }
        if (o1 instanceof Comparable) {
            return ((Comparable)o1).compareTo(o2) < 0;
        }
        if (o2 instanceof Comparable) {
            return ((Comparable)o2).compareTo(o1) > 0;
        }
        throw new ELException(MessageFormat.format(ERROR_MSG, o1.getClass(), o2.getClass()));
    }

    @SuppressWarnings("unchecked")
    private static boolean gt0(TypeConverter converter, Object o1, Object o2) {
        Class<?> t1 = o1.getClass();
        Class<?> t2 = o2.getClass();
        if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigDecimal.class).compareTo(converter.convert(o2, BigDecimal.class)) > 0;
        }
        if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
            return converter.convert(o1, Double.class) > converter.convert(o2, Double.class);
        }
        if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigInteger.class).compareTo(converter.convert(o2, BigInteger.class)) > 0;
        }
        if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
            return converter.convert(o1, Long.class) > converter.convert(o2, Long.class);
        }
        if (t1 == String.class || t2 == String.class) {
            return converter.convert(o1, String.class).compareTo(converter.convert(o2, String.class)) > 0;
        }
        if (o1 instanceof Comparable) {
            return ((Comparable)o1).compareTo(o2) > 0;
        }
        if (o2 instanceof Comparable) {
            return ((Comparable)o2).compareTo(o1) < 0;
        }
        throw new ELException(MessageFormat.format(ERROR_MSG, o1.getClass(), o2.getClass()));
    }

    public static boolean lt(TypeConverter converter, Object o1, Object o2) {
        if (o1 == o2) {
            return false;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return lt0(converter, o1, o2);
    }

    public static boolean gt(TypeConverter converter, Object o1, Object o2) {
        if (o1 == o2) {
            return false;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return gt0(converter, o1, o2);
    }

    public static boolean ge(TypeConverter converter, Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return !lt0(converter, o1, o2);
    }

    public static boolean le(TypeConverter converter, Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return !gt0(converter, o1, o2);
    }

    public static boolean eq(TypeConverter converter, Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        Class<?> t1 = o1.getClass();
        Class<?> t2 = o2.getClass();
        if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigDecimal.class).equals(converter.convert(o2, BigDecimal.class));
        }
        if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
            return converter.convert(o1, Double.class).equals(converter.convert(o2, Double.class));
        }
        if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
            return converter.convert(o1, BigInteger.class).equals(converter.convert(o2, BigInteger.class));
        }
        if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
            return converter.convert(o1, Long.class).equals(converter.convert(o2, Long.class));
        }
        if (t1 == Boolean.class || t2 == Boolean.class) {
            return converter.convert(o1, Boolean.class).equals(converter.convert(o2, Boolean.class));
        }
        if (o1 instanceof Enum<?>) {
            return o1 == converter.convert(o2, o1.getClass());
        }
        if (o2 instanceof Enum<?>) {
            return converter.convert(o1, o2.getClass()) == o2;
        }
        if (t1 == String.class || t2 == String.class) {
            return converter.convert(o1, String.class).equals(converter.convert(o2, String.class));
        }
        return o1.equals(o2);
    }

    public static boolean ne(TypeConverter converter, Object o1, Object o2) {
        return !eq(converter, o1, o2);
    }

    public static boolean empty(TypeConverter converter, Object o) {
        if (o == null || "".equals(o)) {
            return true;
        }
        if (o instanceof Object[]) {
            return ((Object[])o).length == 0;
        }
        if (o instanceof Map<?,?>) {
            return ((Map<?,?>)o).isEmpty();
        }
        if (o instanceof Collection<?>) {
            return ((Collection<?>)o).isEmpty();
        }
        return false;
    }
}
