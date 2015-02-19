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
package com.hubspot.jinjava.util;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class VariableChain {

  private List<String> chain;
  private Object value;

  public VariableChain(List<String> chain, Object value) {
    this.chain = chain;
    this.value = value;
  }

  public Object resolve() {
    for (String name : chain) {
      if (value == null) {
        return null;
      } else {
        value = resolveInternal(name);
      }
    }
    return value;
  }

  private static final ConcurrentMap<String, Method> METHOD_CACHE = Maps.newConcurrentMap();
  
  private Object resolveInternal(String name) {
    Class<?> clazz = value.getClass();
    Method getter = findGetterMethodCached(clazz, name);
    
    if(getter != null && getter != NULL_METHOD) {
      try {
        return getter.invoke(value);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        ENGINE_LOG.error("resolve variable trigger error.", e);
      }
    }
    
    // map
    if (value instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) value;
      if (map.containsKey(name)) {
        return map.get(name);
      }
    }

    try {
      int index = Integer.parseInt(name);
      // array
      if (value.getClass().isArray()) {
        return Array.get(value, index);
      }
      // list
      if (value instanceof List) {
        return ((List<?>) value).get(index);
      }
      // collection
      if (value instanceof Collection) {
        return ((Collection<?>) value).toArray()[index];
      }
    } catch (Exception e) { /* no-op */ }

    throw new JinjavaPropertyNotResolvedException(value, name);
  }

  private Method findGetterMethodCached(Class<?> clazz, String name) {
    Method m = METHOD_CACHE.get(clazz.getName() + ":" + name);
    
    if(m == null) {
      m = findGetterMethod(clazz, name);
      
      if(m != null) {
        METHOD_CACHE.put(clazz.getName() + ":" + name, m);
      }
    }

    return m;
  }

  private static final String[] METHOD_PREFIXES = { "get", "is", "" };
  private static final Method NULL_METHOD;
  static {
    try {
      NULL_METHOD = VariableChain.class.getDeclaredMethod("resolve");
    }
    catch(NoSuchMethodException e){
      throw Throwables.propagate(e);
    }
  }
  
  private Method findGetterMethod(Class<?> clazz, String name) {
    String transformedName = transformName(name);

    for (String prefix : METHOD_PREFIXES) {
      try {
        Method m = clazz.getMethod(prefix + transformedName);
        m.setAccessible(true);
        return m;
      } catch (NoSuchMethodException | SecurityException e) { 
        /* no-op */
      }
    }

    return NULL_METHOD;
  }

  private static final Pattern SNAKE_CASE = Pattern.compile("_([^_]?)");

  private String transformName(String name) {
    Matcher m = SNAKE_CASE.matcher(name);

    StringBuffer result = new StringBuffer();
    while (m.find()) {
      String replacement = m.group(1).toUpperCase();
      m.appendReplacement(result, replacement);
    }
    m.appendTail(result);

    return upperFirst(result.toString());
  }

  private String upperFirst(String name) {
    char c = name.charAt(0);
    if (Character.isLowerCase(c)) {
      return String.valueOf(c).toUpperCase().concat(name.substring(1));
    } else {
      return name;
    }
  }

}
