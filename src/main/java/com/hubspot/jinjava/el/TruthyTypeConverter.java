package com.hubspot.jinjava.el;

import com.hubspot.jinjava.objects.DummyObject;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.ObjectTruthValue;
import de.odysseus.el.misc.TypeConverterImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import javax.el.ELException;

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
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof Enum<?>) {
      return ((Enum<?>) value).name();
    }

    if (value instanceof Collection) {
      return coerceCollection((Collection) value);
    }

    return value.toString();
  }

  private String coerceCollection(Collection value) {
    Iterator<?> it = value.iterator();
    if (!it.hasNext()) return "[]";

    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(1_000_000L);
    sb.append('[');
    for (;;) {
      Object e = it.next();
      sb.append(e == this ? "(this Collection)" : e);
      if (!it.hasNext()) return sb.append(']').toString();
      sb.append(',');
      sb.append(' ');
    }
  }

  @Override
  protected <T extends Enum<T>> T coerceToEnum(Object value, Class<T> type) {
    if (value instanceof DummyObject) {
      EnumSet<T> enumSet = EnumSet.allOf(type);
      if (!enumSet.isEmpty()) {
        return enumSet.iterator().next();
      }
    }

    try {
      return super.coerceToEnum(value, type);
    } catch (ELException e) {
      if (value instanceof String) {
        for (T enumVal : type.getEnumConstants()) {
          String enumStr = enumVal.toString();
          if (enumStr != null && enumStr.equalsIgnoreCase((String) value)) {
            return enumVal;
          }
        }
      }
      throw e;
    }
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
