package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * int(value, default=0) Convert the value into an integer. If the conversion doesn't work it will return 0. You can override this default using the first parameter.
 */
@JinjavaDoc(
  value = "Convert the value into an integer.",
  input = @JinjavaParam(
    value = "value",
    desc = "The value to convert to an integer",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "default",
      type = "number",
      defaultValue = "0",
      desc = "Value to return if the conversion fails"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This example converts a text field string value to a integer",
      code = "{% text \"my_text\" value='25', export_to_template_context=True %}\n" +
      "{% widget_data.my_text.value|int + 28 %}"
    ),
  }
)
public class IntFilter implements Filter {

  @Override
  public String getName() {
    return "int";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Long defaultVal = 0L;
    if (args.length > 0) {
      defaultVal = NumberUtils.toLong(args[0], 0);
    }

    if (var == null) {
      return convertResult(defaultVal);
    }

    if (Number.class.isAssignableFrom(var.getClass())) {
      return convertResult(((Number) var).longValue());
    }

    String input = var.toString().trim();
    Locale locale = interpreter.getConfig().getLocale();
    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
    ParsePosition pp = new ParsePosition(0);
    Long result;
    try {
      result = numberFormat.parse(input, pp).longValue();
    } catch (Exception e) {
      result = defaultVal;
    }
    if (pp.getErrorIndex() != -1 || pp.getIndex() != input.length()) {
      result = defaultVal;
    }
    return convertResult(result);
  }

  private Object convertResult(Long result) {
    if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
      return result;
    }
    return result.intValue();
  }
}
