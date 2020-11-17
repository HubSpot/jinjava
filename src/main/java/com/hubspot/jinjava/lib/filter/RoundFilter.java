package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

@JinjavaDoc(
  value = "Round the number to a given precision.",
  input = @JinjavaParam(
    value = "value",
    type = "number",
    desc = "The number to round",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = RoundFilter.PRECISION_KEY,
      type = "int",
      defaultValue = "0",
      desc = "Specifies the precision of rounding"
    ),
    @JinjavaParam(
      value = RoundFilter.METHOD_KEY,
      type = "enum common|ceil|floor",
      defaultValue = "common",
      desc = "Method of rounding: 'common' rounds either up or down, 'ceil' always rounds up, and 'floor' always rounds down."
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{{ 42.55|round }}",
      output = "43.0",
      desc = "Note that even if rounded to 0 precision, a float is returned."
    ),
    @JinjavaSnippet(code = "{{ 42.55|round(1, 'floor') }}", output = "42.5"),
    @JinjavaSnippet(
      code = "{{ 42.55|round|int }}",
      output = "43",
      desc = "If you need a real integer, pipe it through int"
    )
  }
)
public class RoundFilter extends AbstractFilter {
  public static final String PRECISION_KEY = "precision";
  public static final String METHOD_KEY = "method";

  @Override
  public String getName() {
    return "round";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    if (var == null) {
      return null;
    }

    BigDecimal result;
    try {
      result = new BigDecimal(var.toString());
    } catch (NumberFormatException e) {
      throw new InvalidInputException(
        interpreter,
        this,
        InvalidReason.NUMBER_FORMAT,
        var.toString()
      );
    }

    int precision = (int) parsedArgs.get(PRECISION_KEY);

    RoundingMode roundingMode = (RoundingMode) parsedArgs.get(METHOD_KEY);

    return result.setScale(precision, roundingMode);
  }

  @Override
  protected Object parseArg(
    JinjavaInterpreter interpreter,
    JinjavaParam jinjavaParamMetadata,
    Object value
  ) {
    if (jinjavaParamMetadata.value().equals(METHOD_KEY)) {
      switch (Objects.toString(value, null)) {
        case "ceil":
          return RoundingMode.CEILING;
        case "floor":
          return RoundingMode.FLOOR;
        case "common":
        default:
          return RoundingMode.HALF_UP;
      }
    }
    return super.parseArg(interpreter, jinjavaParamMetadata, value);
  }
}
