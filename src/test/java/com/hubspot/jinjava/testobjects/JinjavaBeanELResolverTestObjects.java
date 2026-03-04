package com.hubspot.jinjava.testobjects;

public class JinjavaBeanELResolverTestObjects {

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
