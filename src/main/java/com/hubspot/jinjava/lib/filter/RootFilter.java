package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import com.google.common.primitives.Doubles;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

import ch.obermuhlner.math.big.BigDecimalMath;

@JinjavaDoc(
    value = "Return the square root of the input.",
    input = @JinjavaParam(value = "number", type = "number", desc = "The number to get the root of", required = true),
    params = @JinjavaParam(value = "root", type = "number", defaultValue = "2", desc = "The nth root to use for the calculation"),
    snippets = {
        @JinjavaSnippet(
            code = "{{ 125|root(3) }}")
    })
public class RootFilter implements Filter {

  private static final MathContext PRECISION = new MathContext(50);

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    double root = 2;
    if (args.length > 0 && args[0] != null) {
      Double tryRoot = Doubles.tryParse(args[0]);
      if (tryRoot == null) {
        throw new InvalidArgumentException(interpreter, this, InvalidReason.NUMBER_FORMAT, 0, args[0]);
      }

      root = tryRoot;
    }

    if (object instanceof Integer) {
      return calculateRoot(interpreter, (Integer) object, root);
    }
    if (object instanceof Float) {
      return calculateRoot(interpreter, (Float) object, root);
    }
    if (object instanceof Long) {
      return calculateRoot(interpreter, (Long) object, root);
    }
    if (object instanceof Short) {
      return calculateRoot(interpreter, (Short) object, root);
    }
    if (object instanceof Double) {
      return calculateRoot(interpreter, (Double) object, root);
    }
    if (object instanceof Byte) {
      return calculateRoot(interpreter, (Byte) object, root);
    }
    if (object instanceof BigDecimal) {
      return calculateBigRoot(interpreter, (BigDecimal) object, root);
    }
    if (object instanceof BigInteger) {
      return calculateBigRoot(interpreter, new BigDecimal((BigInteger) object), root);
    }
    if (object instanceof String || object  instanceof SafeString) {
      try {
        return calculateBigRoot(interpreter, new BigDecimal(object.toString()), root);
      } catch (NumberFormatException e) {
        throw new InvalidInputException(interpreter, this, InvalidReason.NUMBER_FORMAT, object.toString());
      }
    }

    return object;
  }

  @Override
  public String getName() {
    return "root";
  }

  private double calculateRoot(JinjavaInterpreter interpreter, double num, double root) {

    checkArguments(interpreter, num, root);

    if (root == 2) {
      return Math.sqrt(num);
    } else if (root == 3) {
      return Math.cbrt(num);
    }

    return BigDecimalMath.root(new BigDecimal(num), new BigDecimal(root), PRECISION).doubleValue();
  }

  private BigDecimal calculateBigRoot(JinjavaInterpreter interpreter, BigDecimal num, double root) {

    checkArguments(interpreter, num.doubleValue(), root);

    if (root == 2) {
      return BigDecimalMath.sqrt(num, PRECISION);
    }

    return BigDecimalMath.root(num, new BigDecimal(root), PRECISION);
  }

  private void checkArguments(JinjavaInterpreter interpreter, double num, double root) {
    if (num <= 0) {
      throw new InvalidInputException(interpreter, this, InvalidReason.POSITIVE_NUMBER, num);
    }

    if (root <= 0) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.POSITIVE_NUMBER, 0, root);
    }
  }
}
