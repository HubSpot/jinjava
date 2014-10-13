package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;

public class VariableChainTest {

  static class Foo {
    private String bar;

    public Foo(String bar) {
      this.bar = bar;
    }

    public String getBar() {
      return bar;
    }

    public String getBarFoo() {
      return bar;
    }
    
    public String getBarFoo1() {
      return bar;
    }
  }

  @Test
  public void singleWordProperty() {
    assertThat(new VariableChain(Lists.newArrayList("bar"), new Foo("a")).resolve()).isEqualTo("a");
  }

  @Test
  public void multiWordCamelCase() {
    assertThat(new VariableChain(Lists.newArrayList("barFoo"), new Foo("a")).resolve()).isEqualTo("a");
  }

  @Test
  public void multiWordSnakeCase() {
    assertThat(new VariableChain(Lists.newArrayList("bar_foo"), new Foo("a")).resolve()).isEqualTo("a");
  }

  @Test
  public void multiWordNumberSnakeCase() {
    assertThat(new VariableChain(Lists.newArrayList("bar_foo_1"), new Foo("a")).resolve()).isEqualTo("a");
  }
  
  @Test
  public void triesBeanMethodFirst() {
    assertThat(new VariableChain(Lists.newArrayList("year"), DateTime.parse("2013-09-19T12:12:12")).resolve().toString()).isEqualTo("2013");
  }

}
