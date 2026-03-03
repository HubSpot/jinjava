package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.TemplateError;
import java.util.Map;

public class AstDictTestObjects {

  public static class TestClass {

    private Map<TemplateError.ErrorType, String> myMap;

    public TestClass(Map<TemplateError.ErrorType, String> myMap) {
      this.myMap = myMap;
    }

    public Map<TemplateError.ErrorType, String> getMyMap() {
      return myMap;
    }
  }

  public static enum TestEnum {
    FOO("fooName"),
    BAR("barName");

    private String name;

    TestEnum(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
