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

import java.util.Iterator;

public class ForLoop implements Iterator<Object> {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private int index = -1;
  private int counter = 0;
  private int revindex = NULL_VALUE;
  private int revcounter = NULL_VALUE;
  private int length = NULL_VALUE;
  private boolean first = true;
  private boolean last;

  private int depth;

  private Iterator<?> it;

  public ForLoop(Iterator<?> ite, int len) {
    length = len;
    if (len < 2) {
      revindex = 1;
      revcounter = 2;
      last = true;
    } else {
      revindex = len;
      revcounter = len + 1;
      last = false;
    }
    it = ite;
  }

  public ForLoop(Iterator<?> ite) {
    it = ite;
    if (it.hasNext()) {
      last = false;
    } else {
      length = 0;
      revindex = 1;
      revcounter = 2;
      last = true;
    }
  }

  @Override
  public Object next() {
    Object res;
    if (it.hasNext()) {
      index++;
      counter++;
      if (length != NULL_VALUE) {
        revindex--;
        revcounter--;
      }
      res = it.next();
      if (!it.hasNext()) {
        last = true;
        length = counter;
        revindex = 0;
        revcounter = 1;
      }
      if (index > 0) {
        first = false;
      }
    } else {
      res = null;
    }
    return res;
  }

  public int getIndex() {
    return index + 1;
  }

  public int getIndex0() {
    return index;
  }

  public int getDepth() {
    return depth + 1;
  }

  public int getDepth0() {
    return depth;
  }

  public int getCounter() {
    return counter;
  }

  public int getRevindex() {
    return getRevindex0() + 1;
  }

  public int getRevindex0() {
    if (revindex == NULL_VALUE) {
      ENGINE_LOG.warn("can't compute items' length while looping.");
    }
    return revindex;
  }

  public int getRevcounter() {
    if (revcounter == NULL_VALUE) {
      ENGINE_LOG.warn("can't compute items' length while looping.");
    }
    return revcounter;
  }

  public int getLength() {
    if (revcounter == NULL_VALUE) {
      ENGINE_LOG.warn("can't compute items' length while looping.");
    }
    return length;
  }

  public boolean isFirst() {
    return first;
  }

  public boolean isLast() {
    return last;
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  public Object cycle(Object... items) {
    int i = getIndex0() % items.length;
    return items[i];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
