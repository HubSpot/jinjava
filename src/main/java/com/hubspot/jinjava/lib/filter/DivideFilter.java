/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.el.TruthyTypeConverter;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import de.odysseus.el.misc.NumberOperations;
import java.math.BigDecimal;
import java.util.Map;

@JinjavaDoc(
  value = "Divides the current value by a divisor",
  input = @JinjavaParam(
    value = "value",
    type = "number",
    desc = "The numerator to be divided",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "divisor",
      type = "number",
      desc = "The divisor to divide the value",
      required = true
    )
  },
  snippets = {
    @JinjavaSnippet(code = "{% set numerator = 106 %}\n" + "{% numerator|divide(2) %}")
  }
)
public class DivideFilter implements AdvancedFilter {
  private static final TruthyTypeConverter TYPE_CONVERTER = new TruthyTypeConverter();

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 number (divisor) argument"
      );
    }
    Object toMul = args[0];
    Number num;
    if (toMul != null) {
      try {
        num = new BigDecimal(toMul.toString());
      } catch (NumberFormatException e) {
        throw new InvalidArgumentException(
          interpreter,
          this,
          InvalidReason.NUMBER_FORMAT,
          0,
          toMul
        );
      }
    } else {
      return var;
    }

    return NumberOperations.div(TYPE_CONVERTER, var, num);
  }

  @Override
  public String getName() {
    return "divide";
  }
}
