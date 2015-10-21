/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    value = "Return the absolute value of the argument.",
    params = {
        @JinjavaParam(value = "number", type = "number", desc = "The number that you want to get the absolute value of")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_number = -53 %}\n" +
                "{{ my_number|abs }}")
    })
public class AbsFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object instanceof Integer) {
      return Math.abs((Integer) object);
    }
    if (object instanceof Float) {
      return Math.abs((Float) object);
    }
    if (object instanceof Long) {
      return Math.abs((Long) object);
    }
    if (object instanceof Short) {
      return Math.abs((Short) object);
    }
    if (object instanceof Double) {
      return Math.abs((Double) object);
    }
    if (object instanceof BigDecimal) {
      return ((BigDecimal) object).abs();
    }
    if (object instanceof BigInteger) {
      return ((BigInteger) object).abs();
    }
    if (object instanceof Byte) {
      return Math.abs((Byte) object);
    }
    if (object instanceof String) {
      try {
        return new BigDecimal((String) object).abs();
      } catch (Exception e) {
        throw new InterpretException(object + " can't be handled by abs filter", e);
      }
    }
    return object;
  }

  @Override
  public String getName() {
    return "abs";
  }

}
