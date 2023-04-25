/**********************************************************************
Copyright (c) 2016 HubSpot Inc.

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;

public class ListFilterTest {
  ListFilter filter;

  @Before
  public void setup() {
    filter = new ListFilter();
  }

  @Test
  public void itConvertsStringToListOfChars() {
    List<?> o = (List<?>) filter.filter("hello", null);
    assertThat(o).isEqualTo(Lists.newArrayList('h', 'e', 'l', 'l', 'o'));
    assertThat(o.get(0)).isEqualTo('h');
  }

  @Test
  public void itConvertsSetsToLists() {
    Set<Integer> ints = Sets.newHashSet(1, 2, 3);
    ints = new TreeSet<Integer>(ints); // Converting to TreeSet to avoid non-deterministic permutations.
    List<?> o = (List<?>) filter.filter(ints, null);
    assertThat(o).isEqualTo(Lists.newArrayList(1, 2, 3));
  }

  @Test
  public void itWrapsNonCollectionNonStringsInLists() {
    List<?> o = (List<?>) filter.filter(BigDecimal.ONE, null);
    assertThat(o).isEqualTo(Lists.newArrayList(BigDecimal.ONE));
  }

  @Test
  public void itHandlesNullListParams() {
    List<?> o = (List<?>) filter.filter(null, null);
    assertThat(o).isNull();
  }

  @Test
  public void itHandlesBoolean() {
    boolean[] array = { true, false, true };

    Object result = filter.filter(array, null);

    doAssertions(result, Boolean.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesByte() {
    byte[] array = { 1, 2, 3 };

    Object result = filter.filter(array, null);

    doAssertions(result, Byte.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesChar() {
    char[] array = { 'a', 'b', 'c' };

    Object result = filter.filter(array, null);

    doAssertions(result, Character.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesShort() {
    short[] array = { 1, 2, 3 };

    Object result = filter.filter(array, null);

    doAssertions(result, Short.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesInt() {
    int[] array = { 1, 2, 3 };

    Object result = filter.filter(array, null);

    doAssertions(result, Integer.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesLong() {
    long[] array = { 1L, 2L, 3L };

    Object result = filter.filter(array, null);

    doAssertions(result, Long.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesFloat() {
    float[] array = { 1, 2, 3 };

    Object result = filter.filter(array, null);

    doAssertions(result, Float.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesDouble() {
    double[] array = { 1.0, 2.0, 3.0 };

    Object result = filter.filter(array, null);

    doAssertions(result, Double.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesString() {
    String[] array = { "word", "word2", "word3" };

    Object result = filter.filter(array, null);

    doAssertions(result, String.class, array[0], array[1], array[2]);
  }

  @Test
  public void itHandlesInputNull() {
    Object result = filter.filter(null, null);

    assertNull(result);
  }

  @Test
  public void itHandlesGetName() {
    String name = filter.getName();

    assertEquals("list", name);
  }

  private void doAssertions(Object result, Class classOfElements, Object... elements) {
    assertNotNull(result);
    assertTrue(result instanceof List);
    List resultList = (List) result;
    assertEquals(elements.length, resultList.size());
    for (int i = 0; i < elements.length; i++) {
      assertEquals(elements[i], resultList.get(i));
      assertEquals(classOfElements, resultList.get(i).getClass());
    }
  }
}
