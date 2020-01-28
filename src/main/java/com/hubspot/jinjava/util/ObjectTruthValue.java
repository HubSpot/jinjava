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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.hubspot.jinjava.objects.SafeString;

public final class ObjectTruthValue {

  private ObjectTruthValue() {
  }

  public static boolean evaluate(Object object) {

    if (object == null) {
      return false;
    }

    if (object instanceof Boolean) {
      Boolean b = (Boolean) object;
      return b.booleanValue();
    }

    if (object instanceof Number) {
      return ((Number) object).intValue() != 0;
    }

    if (object instanceof String) {
      return !"".equals(object) && !"false".equalsIgnoreCase((String) object);
    }

    if (object instanceof SafeString) {
      return !"".equals(object.toString()) && !"false".equalsIgnoreCase(object.toString());
    }

    if (object.getClass().isArray()) {
      return Array.getLength(object) != 0;
    }

    if (object instanceof Collection) {
      return ((Collection<?>) object).size() != 0;
    }

    if (object instanceof Map) {
      return ((Map<?, ?>) object).size() != 0;
    }

    return true;
  }

}
