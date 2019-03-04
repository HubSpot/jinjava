package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import com.google.common.primitives.Doubles;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

import ch.obermuhlner.math.big.BigDecimalMath;

@JinjavaDoc(
    value = "Return the absolute value of the argument.",
    params = {
        @JinjavaParam(value = "number", type = "number", desc = "The number that you want to get the absolute value of")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_number = -53 %}\n" +
                "{{ my_number|abs }}")
    })
public class RootFilter implements Filter {

  private static final MathContext PRECISION = new MathContext(100);

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    double root = 2;
    if (args.length > 0 && args[0] != null) {
      Double tryRoot = Doubles.tryParse(args[0]);
      if (tryRoot != null) {
        root = tryRoot;
      }
    }

    if (object instanceof Integer) {
      return calculateRoot((Integer) object, root);
    }
    if (object instanceof Float) {
      return calculateRoot((Float) object, root);
    }
    if (object instanceof Long) {
      return calculateRoot((Long) object, root);
    }
    if (object instanceof Short) {
      return calculateRoot((Short) object, root);
    }
    if (object instanceof Double) {
      return calculateRoot((Double) object, root);
    }
    if (object instanceof BigDecimal) {
      if (root == 2) {
        return BigDecimalMath.sqrt((BigDecimal) object, PRECISION);
      }
      return BigDecimalMath.root((BigDecimal) object, new BigDecimal(root), PRECISION);
    }
    if (object instanceof BigInteger) {
      if (root == 2) {
        return BigDecimalMath.sqrt(new BigDecimal((BigInteger) object), PRECISION);
      }
      return BigDecimalMath.root(new BigDecimal((BigInteger) object), new BigDecimal(root), PRECISION);
    }
    if (object instanceof Byte) {
      return calculateRoot((Byte) object, root);
    }
    if (object instanceof String) {
      try {
        if (root == 2) {
          return BigDecimalMath.sqrt(new BigDecimal((String) object), PRECISION);
        } else {
          return BigDecimalMath.root(new BigDecimal((String) object), new BigDecimal(root), PRECISION);
        }
      } catch (Exception e) {
        throw new InterpretException(object + " can't be handled by root filter", e);
      }
    }

    return object;
  }

  @Override
  public String getName() {
    return "root";
  }

  private double calculateRoot(double base, double n) {
    return Math.pow(Math.E, Math.log(base)/n);
  }
}
