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
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

@JinjavaDoc(
  value = "Reverse the object or return an iterator the iterates over it the other way round.",
  input = @JinjavaParam(
    value = "value",
    type = "object",
    desc = "The sequence or dict to reverse the iteration order",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% set nums = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] %}\n" +
      "{% for num in nums|reverse %}\n" +
      "    {{ num }}\n" +
      "{% endfor %}"
    ),
  }
)
public class ReverseFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object == null) {
      return null;
    }
    // collection
    if (object instanceof Collection) {
      return ReverseArrayIterator.create(((Collection<?>) object).toArray());
    }
    // array
    if (object.getClass().isArray()) {
      return ReverseArrayIterator.create((Object[]) object);
    }
    // string
    if (object instanceof String) {
      String origin = (String) object;
      int length = origin.length();
      char[] res = new char[length];
      length--;
      for (int i = 0; i <= length; i++) {
        res[i] = origin.charAt(length - i);
      }
      return String.valueOf(res);
    }

    return object;
  }

  @Override
  public String getName() {
    return "reverse";
  }

  static class ReverseArrayIterator<T> implements Iterator<T> {

    private final T[] array;
    private int index;

    static <T> ReverseArrayIterator<T> create(T[] array) {
      return new ReverseArrayIterator<>(array);
    }

    private ReverseArrayIterator(T[] array) {
      this.array = array;
      index = array.length - 1;
    }

    @Override
    public T next() {
      if (index < 0) {
        throw new NoSuchElementException();
      }
      return array[index--];
    }

    @Override
    public boolean hasNext() {
      return index >= 0;
    }
  }
}
