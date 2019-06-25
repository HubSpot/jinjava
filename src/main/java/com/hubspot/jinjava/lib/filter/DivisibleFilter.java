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
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
    value = "Evaluates to true if the value is divisible by the given number",
    input = @JinjavaParam(value = "value", type = "number", desc = "The value to be divided", required = true),
    params = {
        @JinjavaParam(value = "divisor", type = "number", desc = "The divisor to check if the value is divisible by", required = true)
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example is an alternative to using the is divisibleby expression test",
            code = "{% set num = 10 %}\n" +
                "{% if num|divisible(2) %}\n" +
                "    The number is divisble by 2\n" +
                "{% endif %}")
    })
public class DivisibleFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object == null) {
      return false;
    }
    if (object instanceof Number) {
      if (arg.length < 1) {
        throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (number to divide by)");
      }
      long factor = Long.parseLong(arg[0]);
      long value = ((Number) object).longValue();
      if (value % factor == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return null;
  }

  @Override
  public String getName() {
    return "divisible";
  }

}
