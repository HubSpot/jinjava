package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc(
    value="Round the number to a given precision. The first parameter specifies the precision (default is 0), the second the rounding method:\n\n" +
          "<ul>\n" +
          "<li>'common' rounds either up or down</li>\n" +
          "<li>'ceil' always rounds up</li>\n" +
          "<li>'floor' always rounds down</li>\n" +
          "<ul>\n\n",
    params={
        @JinjavaParam(value="value", type="number"),
        @JinjavaParam(value="precision", type="number", defaultValue="0"),
        @JinjavaParam(value="method", type="enum common|ceil|floor", defaultValue="common")
    },
    snippets={
        @JinjavaSnippet(code="{{ 42.55|round }}", output="43.0", desc="Note that even if rounded to 0 precision, a float is returned."),
        @JinjavaSnippet(code="{{ 42.55|round(1, 'floor') }}", output="42.5"),
        @JinjavaSnippet(code="{{ 42.55|round|int }}", output="43", desc="If you need a real integer, pipe it through int"),
    }
)
public class RoundFilter implements Filter {

  @Override
  public String getName() {
    return "round";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    BigDecimal result = BigDecimal.ZERO;
    try {
      result = new BigDecimal(Objects.toString(var));
    }
    catch(NumberFormatException e) {}

    int precision = 0;
    if(args.length > 0) {
      precision = NumberUtils.toInt(args[0]);
    }

    String method = "common";
    if(args.length > 1) {
      method = args[1];
    }

    RoundingMode roundingMode;

    switch(method) {
    case "ceil":
      roundingMode = RoundingMode.CEILING;
      break;
    case "floor":
      roundingMode = RoundingMode.FLOOR;
      break;
    case "common":
    default:
      roundingMode = RoundingMode.HALF_UP;
    }

    return result.setScale(precision, roundingMode);
  }

}
