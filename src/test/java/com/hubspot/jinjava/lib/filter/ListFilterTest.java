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
}
