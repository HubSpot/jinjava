package com.hubspot.jinjava.testobjects;

import java.util.concurrent.atomic.AtomicReference;

public class JinjavaBeanELResolverTestObjects {

  public record Person(String name, int age) {}

  public record CustomAccessor(String name) {
    @Override
    public String name() {
      return "accessor: " + name;
    }
  }

  public record BeanGetter(String name) {
    public Integer getName() {
      return name.length();
    }
  }

  public record WriteOnlyProperty(String name, AtomicReference<String> writtenValue) {
    public void setName(String value) {
      writtenValue.set(value);
    }
  }

  private record PrivatePerson(String name) {}

  public static Object privatePerson(String name) {
    return new PrivatePerson(name);
  }

  public static class TempItInvokesBestMethodWithSingleParam {

    public String getResult(int a) {
      return "int";
    }

    public String getResult(String a) {
      return "String";
    }

    public String getResult(Object a) {
      return "Object";
    }

    public String getResult(CharSequence a) {
      return "CharSequence";
    }
  }

  public static class TempItPrefersPrimitives {

    public String getResult(int a, Integer b) {
      return "int Integer";
    }

    public String getResult(int a, Object b) {
      return "int Object";
    }

    public String getResult(Number a, int b) {
      return "Number int";
    }
  }
}
