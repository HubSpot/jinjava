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

public interface Filter extends Importable {

  /**
   * Filter the specified template variable within the context of a render process. {{ myvar|myfiltername(arg1,arg2) }}
   *
   * @param var
   *          the variable which this filter should operate on
   * @param interpreter
   *          current interpreter context
   * @param args
   *          any arguments passed to this filter invocation
   * @return the filtered form of the given variable
   */
  Object filter(Object var, JinjavaInterpreter interpreter, String... args);

}
