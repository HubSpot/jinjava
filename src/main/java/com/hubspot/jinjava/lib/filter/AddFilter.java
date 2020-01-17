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
import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
    value = "adds a number to the existing value",
    input = @JinjavaParam(value = "number", type = "number", desc = "Number or numeric variable to add to", required = true),
    params = {
        @JinjavaParam(value = "addend", type = "number", desc = "The number added to the base number", required = true)
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_num = 40 %} \n" +
                "{{ my_num|add(13) }}")
    })
public class AddFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, Object... args) {

    if (object == null) {
      return null;
    }

    if (args.length < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (number to add to base)");
    }

    BigDecimal base;
    try {
      base = new BigDecimal(object.toString());
    } catch (NumberFormatException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.NUMBER_FORMAT, object.toString());
    }

    if (args[0] == null) {
      return base;
    }

    BigDecimal addend;
    try {
      addend = new BigDecimal(Objects.toString(args[0]));
    } catch (NumberFormatException e) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NUMBER_FORMAT, 0, args[0]);
    }

    return base.add(addend);
  }

  @Override
  public String getName() {
    return "add";
  }

}
