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
    value = "Divides the current value by a divisor",
    params = {
        @JinjavaParam(value = "value", type = "number", desc = "The numerator to be divided"),
        @JinjavaParam(value = "divisor", type = "number", desc = "The divisor to divide the value")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set numerator = 106 %}\n" +
                "{% numerator|divide(2) %}")
    })
public class DivideFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (arg.length != 1) {
      throw new InterpretException("filter multiply expects 1 arg >>> " + arg.length);
    }
    String toMul = arg[0];
    Number num;
    if (toMul != null) {
      num = new BigDecimal(toMul);
    } else {
      return object;
    }
    if (object instanceof Integer) {
      return (Integer) object / num.intValue();
    }
    if (object instanceof Float) {
      return (Float) object / num.floatValue();
    }
    if (object instanceof Long) {
      return (Long) object / num.longValue();
    }
    if (object instanceof Short) {
      return (Short) object / num.shortValue();
    }
    if (object instanceof Double) {
      return (Double) object / num.doubleValue();
    }
    if (object instanceof BigDecimal) {
      return ((BigDecimal) object).divide(BigDecimal.valueOf(num.doubleValue()));
    }
    if (object instanceof BigInteger) {
      return ((BigInteger) object).divide(BigInteger.valueOf(num.longValue()));
    }
    if (object instanceof Byte) {
      return (Byte) object / num.byteValue();
    }
    if (object instanceof String) {
      try {
        return Double.valueOf((String) object) / num.doubleValue();
      } catch (Exception e) {
        throw new InterpretException(object + " can't be dealed with multiply filter", e);
      }
    }
    return object;
  }

  @Override
  public String getName() {
    return "divide";
  }

}
