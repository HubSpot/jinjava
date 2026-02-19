package com.hubspot.jinjava.testobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JinjavaInterpreterTestObjects {

  public static class Foo {

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

    @JsonIgnore
    public String getBarHidden() {
      return bar;
    }
  }
}
