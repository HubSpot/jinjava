package com.hubspot.jinjava.lib.filter;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Convert the value into a floating point number.",
    params = {
        @JinjavaParam(value = "value", desc = "Value to convert to a float"),
        @JinjavaParam(value = "default", type = "float", defaultValue = "0.0", desc = "Value to return if conversion fails")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example converts a text field string value to a float",
            code = "{% text \"my_text\" value='25', export_to_template_context=True %}\n" +
                "{% widget_data.my_text.value|float + 28 %}")
    })
public class FloatFilter implements Filter {

  @Override
  public String getName() {
    return "float";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    float defaultVal = 0;
    if (args.length > 0) {
      defaultVal = NumberUtils.toFloat(args[0], 0.0f);
    }

    if (var == null) {
      return defaultVal;
    }

    if (Float.class.isAssignableFrom(var.getClass())) {
      return var;
    }
    if (Number.class.isAssignableFrom(var.getClass())) {
      return ((Number) var).floatValue();
    }

    String input = var.toString();
    Locale locale = interpreter.getConfig().getLocale();
    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
    ParsePosition pp = new ParsePosition(0);
    float result;
    try {
      result = numberFormat.parse(input, pp).floatValue();
    } catch (Exception e) {
      result = defaultVal;
    }
    if (pp.getErrorIndex() != -1 || pp.getIndex() != input.length()) {
      result = defaultVal;
    }
    return result;
  }

}
