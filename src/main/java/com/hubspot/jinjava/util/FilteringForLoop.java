/**********************************************************************
Copyright (c) 2018 HubSpot Inc.

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

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringForLoop extends ForLoop {
  private Predicate<Object> predicate;
  private boolean hasPeeked;
  private Object peekedElement;

  public FilteringForLoop(Iterator<?> ite, int len, Predicate<Object> predicate) {
    super(ite, len);
    this.predicate = predicate;
  }

  public FilteringForLoop(Iterator<?> ite, Predicate<Object> predicate) {
    super(ite);
    this.predicate = predicate;
  }

  @Override
  public Object next() {
    if (hasPeeked) {
      hasPeeked = false;
      return peekedElement;
    }
    if (hasNext()) {
      return peekedElement;
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    if (hasPeeked) {
      return true;
    }

    while (super.hasNext()) {
      peekedElement = super.next();
      if (predicate.test(peekedElement)) {
        hasPeeked = true;
        return true;
      }
    }
    hasPeeked = false;
    return false;
  }
}
