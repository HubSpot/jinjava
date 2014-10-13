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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ObjectIteratorTest {

  private Object items = null;
  private ForLoop loop = null;

  @Test
  public void test1() {
    loop = ObjectIterator.getLoop(items);
    assertEquals(false, loop.hasNext());
    assertEquals(0, loop.getLength());
  }

  @Test
  public void test2() {
    items = "hello";
    loop = ObjectIterator.getLoop(items);
    assertEquals(1, loop.getLength());
  }

  @Test
  public void test3() {
    items = 2;
    loop = ObjectIterator.getLoop(items);
    assertEquals(1, loop.getLength());
  }

  @Test
  public void test4() {
    items = new Integer[] { 7, 8, 9 };
    loop = ObjectIterator.getLoop(items);
    assertEquals(3, loop.getLength());
    loop.next();
    assertEquals(8, loop.next());
  }

  @Test
  public void test5() {
    items = new String[] { "jan", "god" };
    loop = ObjectIterator.getLoop(items);
    assertEquals(2, loop.getLength());
    assertEquals("jan", loop.next());
    assertEquals("god", loop.next());
  }

  @Test
  public void test6() {
    List<String> items = new ArrayList<String>();
    items.add("hello");
    items.add("world");
    items.add("jinjava");
    items.add("asfun");
    loop = ObjectIterator.getLoop(items);
    assertEquals(4, loop.getLength());
    assertEquals("hello", loop.next());
  }

  @Test
  public void test7() {
    Map<Object, Object> items = new HashMap<Object, Object>();
    items.put("ok", 1);
    items.put(1, "ok");
    items.put(2, 2);
    items.put("ko", "ko");
    items.put("test", new ObjectIteratorTest());
    loop = ObjectIterator.getLoop(items);
    assertEquals(5, loop.getLength());
  }
}
