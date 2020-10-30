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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/***
 * Filter base that uses Filter Jinjavadoc to construct named argument parameters.
 * Only filters that specify name, type and defaults correctly should use this as a base
 *
 * @see JinjavaDoc
 * @see JinjavaParam
 */
public abstract class AbstractFilter implements Filter {

  private Map<String, JinjavaParam> namedArguments;
  private boolean isNamedArgumentCheckDone;

  abstract Object filter(
          Object var,
          JinjavaInterpreter interpreter,
          Map<String, Object> parsedArgs);

  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    throw new NotImplementedException("Not implemented");
  }

  public Object filter(
          Object var,
          JinjavaInterpreter interpreter,
          Object[] args,
          Map<String, Object> kwargs) {
    Map<String, JinjavaParam> validNamedArgs = getNamedArguments();
    Map<String, Object> namedArgs = new HashMap<>();
    //Set defaults
    validNamedArgs.forEach((k, v) -> {
      if (StringUtils.isNotEmpty(v.defaultValue())) {
        namedArgs.put(k, v.defaultValue());
      }
    });
    //Process named params
    for (String passedNamedArg : kwargs.keySet()) {
      int argPosition = getNamedArgumentPosition(passedNamedArg);
      if (argPosition == -1) {
        throw new InvalidInputException(
                interpreter,
                "INVALID_ARG_NAME",
                String.format("Argument named '%s' is invalid for filter %s", passedNamedArg, getName())
        );
      }
      namedArgs.put(passedNamedArg, kwargs.get(passedNamedArg));
    }

    //Process indexed params, as declared
    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      String argName = getIndexedArgumentName(i);
      if (argName == null) {
        throw new InvalidInputException(
                interpreter,
                "INVALID_ARG_NAME",
                String.format("Argument at index '%s' is invalid for filter %s", i, getName())
        );
      }
      namedArgs.put(argName, arg);
    }

    //Parse args based on their declared types
    Map<String, Object> parsedArgs = new HashMap<>();
    namedArgs.forEach((k, v) -> parsedArgs.put(k, parseArg(interpreter, validNamedArgs.get(k), v)));

    validateArgs(interpreter, parsedArgs);

    return filter(var, interpreter, parsedArgs);
  }

  public Object parseArg(JinjavaInterpreter interpreter, JinjavaParam jinjavaParamMetadata, Object value) {
    if (jinjavaParamMetadata.type() == null ||
        Arrays.asList("object", "dict", "sequence").contains(jinjavaParamMetadata.type())) {
      return value;
    }
    String valueString = Objects.toString(value, null);
    switch (jinjavaParamMetadata.type().toLowerCase()) {
      case "boolean": {
        return BooleanUtils.toBoolean(valueString);
      }
      case "int": {
        return NumberUtils.toInt(valueString);
      }
      case "long": {
        return NumberUtils.toLong(valueString);
      }
      case "float": {
        return NumberUtils.toFloat(valueString);
      }
      case "double": {
        return NumberUtils.toDouble(valueString);
      }
      case "number": {
        return new BigDecimal(valueString);
      }
      case "string": {
        return valueString;
      }
    }
    throw new InvalidInputException(
            interpreter,
            "INVALID_ARG_NAME",
            String.format("Argument named '%s' with value '%s' cannot be parsed for filter %s", jinjavaParamMetadata.value(), getName())
    );
  }

  public void validateArgs(JinjavaInterpreter interpreter, Map<String, Object> parsedArgs) {
    for(JinjavaParam jinjavaParam: getNamedArguments().values()) {
      if(jinjavaParam.required() && !parsedArgs.containsKey(jinjavaParam.value())) {
        throw new InvalidInputException(
                interpreter,
                "MISSING_REQUIRED_ARG",
                String.format("Argument named '%s' is required but missing for filter %s", jinjavaParam.value(), getName())
        );
      }
    }
  }

  public Map<String, JinjavaParam> getNamedArguments() {
    if (isNamedArgumentCheckDone) {
      return namedArguments;
    }
    JinjavaDoc jinjavaDoc = this.getClass().getAnnotation(JinjavaDoc.class);
    if (jinjavaDoc != null) {
      namedArguments = new LinkedHashMap<>();
      for (JinjavaParam jinjavaParam: jinjavaDoc.params()) {
        namedArguments.put(jinjavaParam.value(), jinjavaParam);
      }
      namedArguments = Collections.unmodifiableMap(namedArguments);
    }
    isNamedArgumentCheckDone = true;
    return namedArguments;
  }

  public int getNamedArgumentPosition(String argName) {
    getNamedArguments();
    if (namedArguments != null) {
      List<String> argNames = new ArrayList<>(namedArguments.keySet());
      return argNames.contains(argName) ? argNames.indexOf(argName) : -1;
    }
    return -1;
  }

  public String getIndexedArgumentName(int position) {
    getNamedArguments();
    if (namedArguments != null) {
      List<String> argNames = new ArrayList<>(namedArguments.keySet());
      return argNames.size() > position ? argNames.get(position) : null;
    }
    return null;
  }

}
