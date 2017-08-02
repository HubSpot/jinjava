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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return a random item from the sequence.",
    params = @JinjavaParam(value = "seq", type = "sequence", desc = "Sequence to return a random item from"),
    snippets = {
        @JinjavaSnippet(
            desc = "The example below is a standard blog loop that returns a single random post.",
            code = "{% for content in contents|random %}\n" +
                "    <div class=\"post-item\">Post item markup</div>" +
                "{% endfor %}")
    })
public class RandomFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object == null) {
      return null;
    }
    // collection
    if (object instanceof Collection) {
      Collection<?> clt = (Collection<?>) object;
      Iterator<?> it = clt.iterator();
      int size = clt.size();
      if (size == 0) {
        return null;
      }
      int index = interpreter.getRandom().nextInt(size);
      while (index-- > 0) {
        it.next();
      }
      return it.next();
    }
    // array
    if (object.getClass().isArray()) {
      int size = Array.getLength(object);
      if (size == 0) {
        return null;
      }
      int index = interpreter.getRandom().nextInt(size);
      return Array.get(object, index);
    }
    // map
    if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      Iterator<?> it = map.values().iterator();
      int size = map.size();
      if (size == 0) {
        return null;
      }
      int index = interpreter.getRandom().nextInt(size);
      while (index-- > 0) {
        it.next();
      }
      return it.next();
    }
    // number
    if (object instanceof Number) {
      return interpreter.getRandom().nextInt(((Number) object).intValue());
    }
    // string
    if (object instanceof String) {
      try {
        return interpreter.getRandom().nextInt(new BigDecimal((String) object).intValue());
      } catch (Exception e) {
        return 0;
      }
    }

    return object;
  }

  @Override
  public String getName() {
    return "random";
  }

}
