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

import org.apache.commons.lang3.BooleanUtils;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ObjectTruthValue;

public class DefaultFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    boolean truthy = false;
    
    if(arg.length == 0) {
      throw new InterpretException("default filter requires 1 or 2 args");
    }
    
    if(arg.length == 2) {
      truthy = BooleanUtils.toBoolean(arg[1]);
    }
    
    if(truthy) {
      if(ObjectTruthValue.evaluate(object)) {
        return object;
      }
    }
    else if(object != null) {
      return object;
    }

    return arg[0];
  }

  @Override
  public String getName() {
    return "default";
  }

}
