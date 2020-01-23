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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
    value = "Removes a string from the value from another string",
    input = @JinjavaParam(value = "value", desc = "The original string", required = true),
    params = {
        @JinjavaParam(value = "to_remove", desc = "String to remove from the original string", required = true)
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set my_string = \"Hello world.\" %}\n" +
                "{{ my_string|cut(' world') }}")
    })
public class CutFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {

    if (arg.length < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (string to remove from target)");
    }
    String cutee = arg[0];
    if (object instanceof String) {
      String origin = Objects.toString(object, "");
      return StringUtils.replace(origin, cutee, "");
    }
    return safeFilter(object, interpreter, arg);
  }

  @Override
  public String getName() {
    return "cut";
  }

}
