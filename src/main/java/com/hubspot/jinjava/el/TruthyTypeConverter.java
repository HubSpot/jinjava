package com.hubspot.jinjava.el;

import com.hubspot.jinjava.objects.DummyObject;
import com.hubspot.jinjava.util.ObjectTruthValue;
import de.odysseus.el.misc.TypeConverterImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;

public class TruthyTypeConverter extends TypeConverterImpl {
  private static final long serialVersionUID = 1L;

  @Override
  protected Boolean coerceToBoolean(Object value) {
    return Boolean.valueOf(ObjectTruthValue.evaluate(value));
  }

  @Override
  protected Character coerceToCharacter(Object value) {
    if (value instanceof DummyObject) {
      return '0';
    }
    return super.coerceToCharacter(value);
  }

  @Override
  protected BigDecimal coerceToBigDecimal(Object value) {
    if (value instanceof DummyObject) {
      return BigDecimal.ZERO;
    }
    return super.coerceToBigDecimal(value);
  }

  @Override
  protected BigInteger coerceToBigInteger(Object value) {
    if (value instanceof DummyObject) {
      return BigInteger.ZERO;
    }
    return super.coerceToBigInteger(value);
  }

  @Override
  protected Double coerceToDouble(Object value) {
    if (value instanceof DummyObject) {
      return 0d;
    }
    return super.coerceToDouble(value);
  }

  @Override
  protected Float coerceToFloat(Object value) {
    if (value instanceof DummyObject) {
      return 0f;
    }
    return super.coerceToFloat(value);
  }

  @Override
  protected Long coerceToLong(Object value) {
    if (value instanceof DummyObject) {
      return 0L;
    }
    return super.coerceToLong(value);
  }

  @Override
  protected Integer coerceToInteger(Object value) {
    if (value instanceof DummyObject) {
      return 0;
    }
    return super.coerceToInteger(value);
  }

  @Override
  protected Short coerceToShort(Object value) {
    if (value instanceof DummyObject) {
      return 0;
    }
    return super.coerceToShort(value);
  }

  @Override
  protected Byte coerceToByte(Object value) {
    if (value instanceof DummyObject) {
      return 0;
    }
    return super.coerceToByte(value);
  }

  @Override
  protected String coerceToString(Object value) {
    if (value instanceof DummyObject) {
      return "";
    }
    return super.coerceToString(value);
  }

  @Override
  protected <T extends Enum<T>> T coerceToEnum(Object value, Class<T> type) {
    if (value instanceof DummyObject) {
      EnumSet<T> enumSet = EnumSet.allOf(type);
      if (!enumSet.isEmpty()) {
        return enumSet.iterator().next();
      }
    }
    return super.coerceToEnum(value, type);
  }

  @Override
  protected Object coerceStringToType(String value, Class<?> type) {
    return super.coerceStringToType(value, type);
  }

  @Override
  protected Object coerceToType(Object value, Class<?> type) {
    return super.coerceToType(value, type);
  }
}
