package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;


public class ShuffleFilterTest {

  ShuffleFilter filter;
  
  @Before
  public void setup() {
    this.filter = new ShuffleFilter();
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void shuffleItems() {
    List<String> before = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    List<String> after = (List<String>) filter.filter(before, null);

    assertThat(before).isSorted();
    assertThat(after).containsAll(before);
    assertThat(after).isNot(new Condition<List<String>>() {
      @Override
      public boolean matches(List<String> value) {
        try {
          assertThat(value).isSorted();
          return true;
        }
        catch(Throwable e) {}
        return false;
      }
    });
  }
  
}
