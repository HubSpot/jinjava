package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import com.google.common.primitives.Doubles;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

import ch.obermuhlner.math.big.BigDecimalMath;

public class LogFilter implements Filter {

  private static final MathContext PRECISION = new MathContext(100);

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    // default to e
    Double base = null;
    if (args.length > 0 && args[0] != null) {
      base = Doubles.tryParse(args[0]);
    }

    if (object instanceof Integer) {
      if (base == null) {
        return Math.log((Integer) object);
      }
      return calculateLog((Integer) object, base);
    }
    if (object instanceof Float) {
      if (base == null) {
        return Math.log((Float) object);
      }
      return calculateLog((Float) object, base);
    }
    if (object instanceof Long) {
      if (base == null) {
        return Math.log((Long) object);
      }
      return calculateLog((Long) object, base);
    }
    if (object instanceof Short) {
      if (base == null) {
        return Math.log((Short) object);
      }
      return calculateLog((Short) object, base);
    }
    if (object instanceof Double) {
      if (base == null) {
        return Math.log((Double) object);
      }
      return calculateLog((Double) object, base);
    }
    if (object instanceof BigDecimal) {
      if (base == null) {
        return BigDecimalMath.log((BigDecimal) object, PRECISION);
      }
      return calculateLog((BigDecimal) object, new BigDecimal(base));
    }
    if (object instanceof BigInteger) {
      if (base == null) {
        return BigDecimalMath.log(new BigDecimal((BigInteger) object), PRECISION);
      }
      return calculateLog(new BigDecimal((BigInteger) object), new BigDecimal(base));
    }
    if (object instanceof Byte) {
      return calculateLog((Byte) object, base);
    }
    if (object instanceof String) {
      try {
        if (base == null) {
          return BigDecimalMath.log(new BigDecimal((String) object), PRECISION);
        } else {
          return calculateLog(new BigDecimal((String) object), new BigDecimal(base));
        }
      } catch (Exception e) {
        throw new InterpretException(object + " can't be handled by log filter", e);
      }
    }

    return object;
  }

  private double calculateLog(double num, double base) {
    return Math.log(num) / Math.log(base);
  }

  private BigDecimal calculateLog(BigDecimal num, BigDecimal base) {
    return BigDecimalMath.log(num, PRECISION).divide(BigDecimalMath.log(base, PRECISION));
  }

  @Override
  public String getName() {
    return "log";
  }
}
