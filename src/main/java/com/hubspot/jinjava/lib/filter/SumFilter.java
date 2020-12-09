package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@JinjavaDoc(
  value = "Returns the sum of a sequence of numbers plus the value of parameter ‘start’ (which defaults to 0). When the sequence is empty it returns start.",
  input = @JinjavaParam(
    value = "value",
    type = "iterable",
    desc = "Selects the sequence or dict to sum values from",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = SumFilter.START_PARAM,
      type = "number",
      defaultValue = "0",
      desc = "Sets a value to return, if there is nothing in the variable to sum"
    ),
    @JinjavaParam(
      value = SumFilter.ATTRIBUTE_PARAM,
      desc = "Specify an optional attribute of dict to sum"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% set sum_this = [1, 2, 3, 4, 5] %}\n" + "{{ sum_this|sum }}\n"
    ),
    @JinjavaSnippet(
      desc = "Sum up only certain attributes",
      code = "Total: {{ items|sum(attribute='price') }}"
    )
  }
)
public class SumFilter extends AbstractFilter implements AdvancedFilter {
  public static final String START_PARAM = "start";
  public static final String ATTRIBUTE_PARAM = "attribute";

  @Override
  public String getName() {
    return "sum";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    ForLoop loop = ObjectIterator.getLoop(var);

    Number start = (Number) parsedArgs.get(START_PARAM);
    String attr = (String) parsedArgs.get(ATTRIBUTE_PARAM);

    BigDecimal sum = start instanceof BigDecimal
      ? (BigDecimal) start
      : new BigDecimal(Objects.toString(start.toString(), "0"));

    while (loop.hasNext()) {
      Object val = loop.next();
      if (val == null) {
        continue;
      }

      BigDecimal addend = BigDecimal.ZERO;

      if (attr != null) {
        val = interpreter.resolveProperty(val, attr);
      }

      try {
        if (Number.class.isAssignableFrom(val.getClass())) {
          addend = new BigDecimal(((Number) val).doubleValue());
        } else {
          addend = new BigDecimal(Objects.toString(val, "0"));
        }
      } catch (NumberFormatException e) {}

      sum = sum.add(addend);
    }

    return sum;
  }
}
