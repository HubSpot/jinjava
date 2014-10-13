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
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class ForLoopTest {

  private static final int NULL_VAL = Integer.MIN_VALUE;

  public static class AIterator implements Iterator<String> {
    int i = 0;

    @Override
    public boolean hasNext() {
      return i < 6;
    }

    @Override
    public String next() {
      i++;
      if (i > 6)
        return null;
      return "test";
    }

    @Override
    public void remove() {
      // nothing
    }

  }

  private ForLoop loop;

  @Before
  public void setUp() throws Exception {
    ArrayList<Object> al = new ArrayList<Object>();
    al.add("String");
    al.add("true1");
    al.add(Boolean.TRUE);
    al.add(1);
    al.add(1.23);
    al.add(new java.util.Date());
    loop = new ForLoop(al.iterator(), al.size());
  }

  @Test
  public void test1() {
    loop.next();
    assertEquals(0, loop.getIndex0());
    assertEquals(true, loop.isFirst());
    assertEquals(1, loop.getCounter());
    assertEquals(5, loop.getRevindex0());
    assertEquals(6, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test2() {
    loop.next();
    loop.next();
    loop.next();
    assertEquals(2, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(3, loop.getCounter());
    assertEquals(3, loop.getRevindex0());
    assertEquals(4, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test3() {
    while (loop.hasNext()) {
      loop.next();
    }
    assertEquals(5, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(6, loop.getCounter());
    assertEquals(0, loop.getRevindex0());
    assertEquals(1, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(true, loop.isLast());
  }

  @Test
  public void test4() {
    loop.next();
    loop.next();
    loop.next();
    loop.next();
    loop.next();
    assertEquals(4, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(5, loop.getCounter());
    assertEquals(1, loop.getRevindex0());
    assertEquals(2, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test5() {
    loop.next();
    loop.next();
    assertEquals(1, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(2, loop.getCounter());
    assertEquals(4, loop.getRevindex0());
    assertEquals(5, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test6() {
    loop = new ForLoop(new AIterator());
    loop.next();
    assertEquals(0, loop.getIndex0());
    assertEquals(true, loop.isFirst());
    assertEquals(1, loop.getCounter());
    assertEquals(NULL_VAL, loop.getRevindex0());
    assertEquals(NULL_VAL, loop.getRevcounter());
    assertEquals(NULL_VAL, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test7() {
    loop = new ForLoop(new AIterator());
    loop.next();
    loop.next();
    loop.next();
    assertEquals(2, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(3, loop.getCounter());
    assertEquals(NULL_VAL, loop.getRevindex0());
    assertEquals(NULL_VAL, loop.getRevcounter());
    assertEquals(NULL_VAL, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test8() {
    loop = new ForLoop(new AIterator());
    while (loop.hasNext()) {
      loop.next();
    }
    assertEquals(5, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(6, loop.getCounter());
    assertEquals(0, loop.getRevindex0());
    assertEquals(1, loop.getRevcounter());
    assertEquals(6, loop.getLength());
    assertEquals(true, loop.isLast());
  }

  @Test
  public void test9() {
    loop = new ForLoop(new AIterator());
    loop.next();
    loop.next();
    loop.next();
    loop.next();
    loop.next();
    assertEquals(4, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(5, loop.getCounter());
    assertEquals(NULL_VAL, loop.getRevindex0());
    assertEquals(NULL_VAL, loop.getRevcounter());
    assertEquals(NULL_VAL, loop.getLength());
    assertEquals(false, loop.isLast());
  }

  @Test
  public void test10() {
    loop = new ForLoop(new AIterator());
    loop.next();
    loop.next();
    assertEquals(1, loop.getIndex0());
    assertEquals(false, loop.isFirst());
    assertEquals(2, loop.getCounter());
    assertEquals(NULL_VAL, loop.getRevindex0());
    assertEquals(NULL_VAL, loop.getRevcounter());
    assertEquals(NULL_VAL, loop.getLength());
    assertEquals(false, loop.isLast());
  }

}
