package com.hubspot.jinjava.doc;

import java.util.Map;
import java.util.TreeMap;

public class JinjavaDoc {

  private final Map<String, JinjavaDocExpTest> expTests = new TreeMap<>();
  private final Map<String, JinjavaDocFilter> filters = new TreeMap<>();
  private final Map<String, JinjavaDocFunction> functions = new TreeMap<>();
  private final Map<String, JinjavaDocTag> tags = new TreeMap<>();

  public Map<String, JinjavaDocExpTest> getExpTests() {
    return expTests;
  }

  public void addExpTest(JinjavaDocExpTest expTest) {
    expTests.put(expTest.getName(), expTest);
  }

  public Map<String, JinjavaDocFilter> getFilters() {
    return filters;
  }

  public void addFilter(JinjavaDocFilter filter) {
    filters.put(filter.getName(), filter);
  }

  public Map<String, JinjavaDocFunction> getFunctions() {
    return functions;
  }

  public void addFunction(JinjavaDocFunction function) {
    functions.put(function.getName(), function);
  }

  public Map<String, JinjavaDocTag> getTags() {
    return tags;
  }

  public void addTag(JinjavaDocTag tag) {
    tags.put(tag.getName(), tag);
  }

}
