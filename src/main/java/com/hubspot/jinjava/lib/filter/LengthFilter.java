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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return the number of items of a sequence or mapping",
    params = @JinjavaParam(value = "object", desc = "The sequence to count"),
    snippets = {
        @JinjavaSnippet(
            code = "{% set services = ['Web design', 'SEO', 'Inbound Marketing', 'PPC'] %}\n" +
                "{{ services|length }}")
    })
public class LengthFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (null == object) {
      return 0;
    }

    if (object instanceof Collection) {
      return ((Collection<?>) object).size();
    }

    if (object.getClass().isArray()) {
      return Array.getLength(object);
    }

    if (object instanceof Map) {
      return ((Map<?, ?>) object).size();
    }

    if (object instanceof Iterable) {
      Iterator<?> it = ((Iterable<?>) object).iterator();
      int size = 0;
      while (it.hasNext()) {
        it.next();
        size++;
      }
      return size;
    }

    if (object instanceof Iterator) {
      Iterator<?> it = (Iterator<?>) object;
      int size = 0;
      while (it.hasNext()) {
        it.next();
        size++;
      }
      return size;
    }

    if (object instanceof String) {
      return ((String) object).length();
    }
    return 0;
  }

  @Override
  public String getName() {
    return "length";
  }

}
