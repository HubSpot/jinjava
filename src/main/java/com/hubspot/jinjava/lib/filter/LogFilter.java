package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import com.google.common.primitives.Doubles;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

import ch.obermuhlner.math.big.BigDecimalMath;

@JinjavaDoc(
    value = "Return the natural log of the input.",
    input = @JinjavaParam(value = "number", type = "number", desc = "The number to get the log of", required = true),
    params = @JinjavaParam(value = "base", type = "number", defaultValue = "e (natural logarithm)", desc = "The base to use for the log calculation"),
    snippets = {
        @JinjavaSnippet(
            code = "{{ 25|log(5) }}")
    })
public class LogFilter implements Filter {

  private static final MathContext PRECISION = new MathContext(50);

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    // default to e
    Double root = null;
    if (args.length > 0 && args[0] != null) {
      Double tryRoot = Doubles.tryParse(args[0]);
      if (tryRoot == null) {
        throw new InvalidArgumentException(interpreter, this, InvalidReason.NUMBER_FORMAT, 0, args[0]);
      }

      root = tryRoot;
    }

    if (object instanceof Integer) {
      return calculateLog(interpreter, (Integer) object, root);
    }
    if (object instanceof Float) {
      return calculateLog(interpreter, (Float) object, root);
    }
    if (object instanceof Long) {
      return calculateLog(interpreter, (Long) object, root);
    }
    if (object instanceof Short) {
      return calculateLog(interpreter, (Short) object, root);
    }
    if (object instanceof Double) {
      return calculateLog(interpreter, (Double) object, root);
    }
    if (object instanceof Byte) {
      return calculateLog(interpreter, (Byte) object, root);
    }
    if (object instanceof BigDecimal) {
      return calculateBigLog(interpreter, (BigDecimal) object, root);
    }
    if (object instanceof BigInteger) {
      return calculateBigLog(interpreter, new BigDecimal((BigInteger) object), root);
    }
    if (object instanceof String) {
      try {
        return calculateBigLog(interpreter, new BigDecimal((String) object), root);
      } catch (NumberFormatException e) {
        throw new InvalidInputException(interpreter, this, InvalidReason.NUMBER_FORMAT, object.toString());
      }
    }

    return object;
  }

  private double calculateLog(JinjavaInterpreter interpreter, double num, Double base) {

    checkArguments(interpreter, num, base);

    if (base == null) {
      return Math.log(num);
    }

    return BigDecimalMath.log(new BigDecimal(num), PRECISION)
        .divide(BigDecimalMath.log(new BigDecimal(base), PRECISION), RoundingMode.HALF_EVEN)
        .doubleValue();
  }

  private BigDecimal calculateBigLog(JinjavaInterpreter interpreter, BigDecimal num, Double base) {

    checkArguments(interpreter, num.doubleValue(), base);

    if (base == null) {
      return BigDecimalMath.log(num, PRECISION);
    }

    return BigDecimalMath.log(num, PRECISION)
        .divide(BigDecimalMath.log(new BigDecimal(base), PRECISION), RoundingMode.HALF_EVEN);
  }

  private void checkArguments(JinjavaInterpreter interpreter, double num, Double base) {

    if (num <= 0) {
      throw new InvalidInputException(interpreter, this, InvalidReason.POSITIVE_NUMBER, num);
    }

    if (base != null && base <= 0) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.POSITIVE_NUMBER, 0, base);
    }
  }

  @Override
  public String getName() {
    return "log";
  }
}
