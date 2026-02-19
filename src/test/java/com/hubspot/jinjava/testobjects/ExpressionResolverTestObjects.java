package com.hubspot.jinjava.testobjects;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExpressionResolverTestObjects {

  public static class MyCustomList<T> extends ForwardingList<T> implements PyWrapper {

    private final List<T> list;

    public MyCustomList(List<T> list) {
      this.list = list;
    }

    @Override
    protected List<T> delegate() {
      return list;
    }

    public int getTotalCount() {
      return list.size();
    }
  }

  public static final class MyCustomMap implements Map<String, String> {

    Map<String, String> data = ImmutableMap.of("foo", "bar", "two", "2", "size", "777");

    @Override
    public int size() {
      return data.size();
    }

    @Override
    public boolean isEmpty() {
      return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return data.containsValue(value);
    }

    @Override
    public String get(Object key) {
      return data.get(key);
    }

    @Override
    public String put(String key, String value) {
      return null;
    }

    @Override
    public String remove(Object key) {
      return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {}

    @Override
    public void clear() {}

    @Override
    public Set<String> keySet() {
      return data.keySet();
    }

    @Override
    public Collection<String> values() {
      return data.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      return data.entrySet();
    }
  }

  public static class TestClass {

    private boolean touched = false;
    private String name = "Amazing test class";

    public boolean isTouched() {
      return touched;
    }

    public void touch() {
      this.touched = true;
    }

    public String getName() {
      return name;
    }
  }

  public static final class MyClass {

    private Date date;

    public MyClass(Date date) {
      this.date = date;
    }

    public Class getClazz() {
      return this.getClass();
    }

    public Date getDate() {
      return date;
    }
  }

  public static final class OptionalProperty {

    private MyClass nested;
    private String val;

    public OptionalProperty(MyClass nested, String val) {
      this.nested = nested;
      this.val = val;
    }

    public Optional<MyClass> getNested() {
      return Optional.ofNullable(nested);
    }

    public Optional<String> getVal() {
      return Optional.ofNullable(val);
    }
  }

  public static final class NestedOptionalProperty {

    private OptionalProperty nested;

    public NestedOptionalProperty(OptionalProperty nested) {
      this.nested = nested;
    }

    public Optional<OptionalProperty> getNested() {
      return Optional.ofNullable(nested);
    }
  }
}
