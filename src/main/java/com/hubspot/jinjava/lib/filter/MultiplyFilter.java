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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Multiplies the current object with the given multiplier",
    params = {
        @JinjavaParam(value = "value", type = "number", desc = "Base number to be multiplied"),
        @JinjavaParam(value = "multiplier", type = "number", desc = "The multiplier")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set n = 20 %}\n" +
                "{{ n|multiply(3) }}")
    })
public class MultiplyFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (arg.length != 1) {
      throw new InterpretException("filter multiply expects 1 arg >>> " + arg.length);
    }
    String toMul = arg[0];
    Number num = new BigDecimal(toMul);

    if (object instanceof Integer) {
      return num.intValue() * (Integer) object;
    }
    if (object instanceof Float) {
      return 0D + num.floatValue() * (Float) object;
    }
    if (object instanceof Long) {
      return num.longValue() * (Long) object;
    }
    if (object instanceof Short) {
      return 0 + num.shortValue() * (Short) object;
    }
    if (object instanceof Double) {
      return num.doubleValue() * (Double) object;
    }
    if (object instanceof BigDecimal) {
      return ((BigDecimal) object).multiply(BigDecimal.valueOf(num.doubleValue()));
    }
    if (object instanceof BigInteger) {
      return ((BigInteger) object).multiply(BigInteger.valueOf(num.longValue()));
    }
    if (object instanceof Byte) {
      return num.byteValue() * (Byte) object;
    }
    if (object instanceof String) {
      try {
        return num.doubleValue() * Double.parseDouble((String) object);
      } catch (Exception e) {
        throw new InterpretException(object + " can't be dealed with multiply filter", e);
      }
    }
    return object;
  }

  @Override
  public String getName() {
    return "multiply";
  }

}
