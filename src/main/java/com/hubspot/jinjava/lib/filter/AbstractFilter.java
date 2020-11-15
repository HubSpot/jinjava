/**********************************************************************
 Copyright (c) 2020 HubSpot Inc.

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

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/***
 * Filter base that uses Filter Jinjavadoc to construct named argument parameters.
 * Only filters that specify name, type and defaults correctly should use this as a base
 *
 * @see JinjavaDoc
 * @see JinjavaParam
 */
public abstract class AbstractFilter implements Filter, AdvancedFilter {
  private static final Map<Class, Map<String, JinjavaParam>> NAMED_ARGUMENTS_CACHE = new ConcurrentHashMap<>();
  private static final Map<Class, Map<String, Object>> DEFAULT_VALUES_CACHE = new ConcurrentHashMap<>();

  private final Map<String, JinjavaParam> namedArguments;
  private final Map<String, Object> defaultValues;

  public AbstractFilter() {
    namedArguments =
      NAMED_ARGUMENTS_CACHE.computeIfAbsent(getClass(), cls -> initNamedArguments());
    defaultValues =
      DEFAULT_VALUES_CACHE.computeIfAbsent(getClass(), cls -> initDefaultValues());
  }

  abstract Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  );

  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    return filter(var, interpreter, args, Collections.emptyMap());
  }

  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    Map<String, Object> namedArgs = new HashMap<>();
    //Set defaults
    namedArgs.putAll(defaultValues);

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
      (k, v) -> parsedArgs.put(k, parseArg(interpreter, namedArguments.get(k), v))
    );

    validateArgs(interpreter, parsedArgs);

    return filter(var, interpreter, parsedArgs);
  }

  protected Object parseArg(
    @Nullable JinjavaInterpreter interpreter,
    JinjavaParam jinjavaParamMetadata,
    Object value
  ) {
    if (jinjavaParamMetadata.type() == null || value == null) {
      return value;
    }
    switch (jinjavaParamMetadata.type().toLowerCase()) {
      case "boolean":
        return value instanceof Boolean
          ? (Boolean) value
          : BooleanUtils.toBooleanObject(value.toString());
      case "int":
        return parseNumber(value, int.class);
      case "long":
        return parseNumber(value, long.class);
      case "float":
        return parseNumber(value, float.class);
      case "double":
        return parseNumber(value, double.class);
      case "number":
        return parseNumber(value, Number.class);
      case "string":
        return value.toString();
      case "object":
      case "dict":
      case "sequence":
        return value;
      default:
        String errorMessage = String.format(
          "Argument named '%s' with value '%s' cannot be parsed for filter '%s'",
          jinjavaParamMetadata.value(),
          value,
          getName()
        );
        if (interpreter != null) { //Filter runtime vs init
          throw new InvalidInputException(interpreter, "INVALID_ARG_NAME", errorMessage);
        } else {
          throw new IllegalArgumentException(errorMessage);
        }
    }
  }

  public <N extends Number> Number parseNumber(Object value, Class<N> numberClass) {
    //check if needs to be parsed to number first, and then convert to required type
    if (!(value instanceof Number)) {
      String str = Objects.toString(value, null);
      if (NumberUtils.isCreatable(str)) {
        value = NumberUtils.createNumber(str);
      } else {
        throw new IllegalArgumentException(
          String.format(
            "Input '%s' is not parsable type of '%s' for filter '%s'",
            value,
            numberClass,
            getName()
          )
        );
      }
    }
    Number n = (Number) value;
    if (numberClass == int.class || numberClass == Integer.class) {
      return n.intValue();
    } else if (numberClass == long.class || numberClass == Long.class) {
      return n.longValue();
    } else if (numberClass == float.class || numberClass == Float.class) {
      return n.floatValue();
    } else if (numberClass == double.class || numberClass == Double.class) {
      return n.doubleValue();
    } else { //if nclass == Number.class
      return n;
    }
  }

  public void validateArgs(
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    for (JinjavaParam jinjavaParam : namedArguments.values()) {
      if (
        jinjavaParam.required() &&
        (
          !parsedArgs.containsKey(jinjavaParam.value()) ||
          parsedArgs.get(jinjavaParam.value()) == null
        )
      ) {
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
      .ofNullable(namedArguments)
      .map(Map::keySet)
      .map(ArrayList::new)
      .flatMap(argNames -> Optional.of(argNames.indexOf(argName)))
      .orElse(-1);
  }

  public String getIndexedArgumentName(int position) {
    return Optional
      .ofNullable(namedArguments)
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
      ImmutableMap.Builder<String, JinjavaParam> namedArgsBuilder = ImmutableMap.builder();

      Arrays
        .stream(jinjavaDoc.params())
        .forEachOrdered(
          jinjavaParam -> namedArgsBuilder.put(jinjavaParam.value(), jinjavaParam)
        );

      return namedArgsBuilder.build();
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

  public Map<String, Object> initDefaultValues() {
    return namedArguments
      .entrySet()
      .stream()
      .filter(e -> StringUtils.isNotEmpty(e.getValue().defaultValue()))
      .collect(
        //              ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().defaultValue())
        ImmutableMap.toImmutableMap(
          Map.Entry::getKey,
          e -> parseArg(null, e.getValue(), e.getValue().defaultValue())
        )
      );
  }
}
