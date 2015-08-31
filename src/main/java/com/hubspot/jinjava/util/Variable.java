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
package com.hubspot.jinjava.util;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class Variable {
  private static final Splitter DOT_SPLITTER = Splitter.on('.');

  private final JinjavaInterpreter interpreter;

  private final String name;
  private final List<String> chainList;

  public Variable(JinjavaInterpreter interpreter, String variable) {
    this.interpreter = interpreter;

    if (variable.indexOf('.') == -1) {
      name = variable;
      chainList = Collections.emptyList();
    } else {
      List<String> parts = Lists.newArrayList(DOT_SPLITTER.split(variable));
      name = parts.get(0);
      chainList = parts.subList(1, parts.size());
    }
  }

  public String getName() {
    return name;
  }

  public Object resolve(Object value) {
    return interpreter.resolveProperty(value, chainList);
  }

  @Override
  public String toString() {
    return "<Variable: " + name + ">";
  }
}
