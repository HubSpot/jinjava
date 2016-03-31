package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ListFilterTest {

  ListFilter filter;

  @Before
  public void setup() {
    filter = new ListFilter();
  }

  @Test
  public void itConvertsStringToListOfChars() {
    List o = (List)filter.filter("hello", null);
    assertThat(o).isEqualTo(Lists.newArrayList('h', 'e', 'l', 'l', 'o'));
  }

  @Test
  public void itConvertsSetsToLists() {
    Set<Integer> ints = Sets.newHashSet(1,2,3);
    List o = (List)filter.filter(ints, null);
    assertThat(o).isEqualTo(Lists.newArrayList(1,2,3));
  }

  @Test
  public void itWrapsNonCollectionNonStringsInLists() {
    List o = (List)filter.filter(BigDecimal.ONE, null);
    assertThat(o).isEqualTo(Lists.newArrayList(BigDecimal.ONE));
  }
}
