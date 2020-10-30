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
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.util.ObjectTruthValue;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;

@JinjavaDoc(
  value = "If the value is undefined it will return the passed default value, otherwise the value of the variable",
  input = @JinjavaParam(
    value = "value",
    desc = "The variable or value to test",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "default_value",
      type = "object",
      desc = "Value to print when variable is not defined",
      required = true
    ),
    @JinjavaParam(
      value = "truthy",
      type = "boolean",
      defaultValue = "False",
      desc = "Set to True to use with variables which evaluate to false"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This will output the value of my_variable if the variable was defined, otherwise 'my_variable is not defined'",
      code = "{{ my_variable|default('my_variable is not defined') }}"
    ),
    @JinjavaSnippet(
      desc = "If you want to use default with variables that evaluate to false you have to set the second parameter to true",
      code = "{{ ''|default('the string was empty', true) }}"
    )
  }
)
public class DefaultFilter extends AbstractFilter implements AdvancedFilter {

  @Override
  public Object filter(
    Object object,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    if (parsedArgs.size() < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires either 1 (default value to use) or 2 (default value to use, default with variables that evaluate to false) arguments"
      );
    }

    boolean truthy = (boolean) parsedArgs.get("truthy");
    Object defaultValue = parsedArgs.get("default_value");

    if (truthy) {
      if (ObjectTruthValue.evaluate(object)) {
        return object;
      }
    } else if (object != null) {
      return object;
    }

    return defaultValue instanceof PyWrapper ? defaultValue : Objects.toString(defaultValue);
  }

  @Override
  public String getName() {
    return "default";
  }
}
