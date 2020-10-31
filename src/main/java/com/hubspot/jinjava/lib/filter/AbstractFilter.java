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
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  private final Map<String, JinjavaParam> NAMED_ARGUMENTS;

  public AbstractFilter() {
    NAMED_ARGUMENTS = initNamedArguments();
  }

  abstract Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  );

  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    throw new NotImplementedException("Not implemented");
  }

  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    Map<String, Object> namedArgs = new HashMap<>();
    //Set defaults
    NAMED_ARGUMENTS.forEach(
      (k, v) -> {
        if (StringUtils.isNotEmpty(v.defaultValue())) {
          namedArgs.put(k, v.defaultValue());
        }
      }
    );
    //Process named params
    for (Map.Entry<String, Object> passedNamedArgEntry : kwargs.entrySet()) {
      String argName = passedNamedArgEntry.getKey();
      Object argValue = passedNamedArgEntry.getValue();
      int argPosition = getNamedArgumentPosition(argName);
      if (argPosition == -1) {
        throw new InvalidInputException(
          interpreter,
          "INVALID_ARG_NAME",
          String.format(
            "Argument named '%s' is invalid for filter %s",
            argName,
            getName()
          )
        );
      }
      namedArgs.put(argName, argValue);
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
    namedArgs.forEach(
      (k, v) -> parsedArgs.put(k, parseArg(interpreter, NAMED_ARGUMENTS.get(k), v))
    );

    validateArgs(interpreter, parsedArgs);

    return filter(var, interpreter, parsedArgs);
  }

  protected Object parseArg(
    JinjavaInterpreter interpreter,
    JinjavaParam jinjavaParamMetadata,
    Object value
  ) {
    if (
      jinjavaParamMetadata.type() == null ||
      value == null ||
      Arrays.asList("object", "dict", "sequence").contains(jinjavaParamMetadata.type())
    ) {
      return value;
    }
    String valueString = Objects.toString(value, null);
    switch (jinjavaParamMetadata.type().toLowerCase()) {
      case "boolean":
        return value instanceof Boolean
          ? (Boolean) value
          : BooleanUtils.toBooleanObject(valueString);
      case "int":
        return value instanceof Integer
          ? (Integer) value
          : NumberUtils.toInt(valueString);
      case "long":
        return value instanceof Long ? (Long) value : NumberUtils.toLong(valueString);
      case "float":
        return value instanceof Float ? (Float) value : NumberUtils.toFloat(valueString);
      case "double":
        return value instanceof Double
          ? (Double) value
          : NumberUtils.toDouble(valueString);
      case "number":
        return value instanceof Number ? (Number) value : new BigDecimal(valueString);
      case "string":
        return valueString;
      default:
        throw new InvalidInputException(
          interpreter,
          "INVALID_ARG_NAME",
          String.format(
            "Argument named '%s' with value '%s' cannot be parsed for filter %s",
            jinjavaParamMetadata.value(),
            value,
            getName()
          )
        );
    }
  }

  public void validateArgs(
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    for (JinjavaParam jinjavaParam : NAMED_ARGUMENTS.values()) {
      if (jinjavaParam.required() && !parsedArgs.containsKey(jinjavaParam.value())) {
        throw new InvalidInputException(
          interpreter,
          "MISSING_REQUIRED_ARG",
          String.format(
            "Argument named '%s' is required but missing for filter %s",
            jinjavaParam.value(),
            getName()
          )
        );
      }
    }
  }

  public int getNamedArgumentPosition(String argName) {
    return Optional
      .ofNullable(NAMED_ARGUMENTS)
      .map(Map::keySet)
      .map(ArrayList::new)
      .flatMap(argNames -> Optional.of(argNames.indexOf(argName)))
      .orElse(-1);
  }

  public String getIndexedArgumentName(int position) {
    return Optional
      .ofNullable(NAMED_ARGUMENTS)
      .map(Map::keySet)
      .map(ArrayList::new)
      .flatMap(
        argNames ->
          Optional.ofNullable(argNames.size() > position ? argNames.get(position) : null)
      )
      .orElse(null);
  }

  public Map<String, JinjavaParam> initNamedArguments() {
    JinjavaDoc jinjavaDoc = this.getClass().getAnnotation(JinjavaDoc.class);
    if (jinjavaDoc != null) {
      Map<String, JinjavaParam> namedArgs = new LinkedHashMap<>();
      for (JinjavaParam jinjavaParam : jinjavaDoc.params()) {
        namedArgs.put(jinjavaParam.value(), jinjavaParam);
      }
      return Collections.unmodifiableMap(namedArgs);
    } else {
      throw new UnsupportedOperationException(
        String.format(
          "%s: @JinjavaDoc must be configured for filter %s to function",
          getClass(),
          getName()
        )
      );
    }
  }
}
