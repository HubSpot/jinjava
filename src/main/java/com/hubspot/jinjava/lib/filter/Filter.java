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

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.objects.SafeString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public interface Filter extends Importable {
  /**
   * Filter the specified template variable within the context of a render process. {{ myvar|myfiltername(arg1,arg2) }}
   *
   * @param var         the variable which this filter should operate on
   * @param interpreter current interpreter context
   * @param args        any arguments passed to this filter invocation
   * @return the filtered form of the given variable
   */
  Object filter(Object var, JinjavaInterpreter interpreter, String... args);

  /*
   * The JinJava parser calls filters giving to them two lists of parameters:
   *   - Positional arguments as Object[]
   *   - Named arguments as Map<String, Object>
   *
   * This default method transforms that call to a simple filter that only receives String positional arguments to
   * maintain backward-compatibility with old filters that don't support named arguments.
   */
  default Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    // We append the named arguments at the end of the positional ones
    Object[] allArgs = ArrayUtils.addAll(args, kwargs.values().toArray());

    List<String> stringArgs = new ArrayList<>();

    for (Object arg : allArgs) {
      stringArgs.add(arg == null ? null : Objects.toString(arg));
    }

    String[] filterArgs = new String[stringArgs.size()];
    for (int i = 0; i < stringArgs.size(); i++) {
      filterArgs[i] = stringArgs.get(i);
    }

    if (var instanceof SafeString) {
      return filter((SafeString) var, interpreter, filterArgs);
    }

    return filter(var, interpreter, filterArgs);
  }

  default boolean preserveSafeString() {
    return true;
  }

  default Object filter(SafeString var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return filter((Object) null, interpreter, args);
    }
    Object filteredValue = filter(var.toString(), interpreter, args);
    if (preserveSafeString() && filteredValue instanceof String) {
      return new SafeString(filteredValue.toString());
    }
    return filteredValue;
  }
}
