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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterators;

public final class ObjectIterator {

  private ObjectIterator() {}

  @SuppressWarnings("unchecked")
  public static ForLoop getLoop(Object obj) {
    if (obj == null) {
      return new ForLoop(Collections.emptyIterator(), 0);
    }
    // collection
    if (obj instanceof Collection) {
      Collection<Object> clt = (Collection<Object>) obj;
      return new ForLoop(clt.iterator(), clt.size());
    }
    // array
    if (obj.getClass().isArray()) {
      Object[] arr = (Object[]) obj;
      return new ForLoop(Iterators.forArray(arr), arr.length);
    }
    // map
    if (obj instanceof Map) {
      Collection<Object> clt = ((Map<Object, Object>) obj).values();
      return new ForLoop(clt.iterator(), clt.size());
    }
    // iterable,iterator
    if (obj instanceof Iterable) {
      return new ForLoop(((Iterable<Object>) obj).iterator());
    }
    if (obj instanceof Iterator) {
      return new ForLoop((Iterator<Object>) obj);
    }
    // others
    return new ForLoop(Iterators.singletonIterator(obj), 1);
  }

}
