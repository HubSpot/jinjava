package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;

public class EagerExpressionResolverTestObjects {

  public static class Foo {

    private final String bar;

    public Foo(String bar) {
      this.bar = bar;
    }

    public String bar() {
      return bar;
    }

    public String echo(String toEcho) {
      return toEcho;
    }
  }

  public static class SomethingExceptionallyPyish implements PyishSerializable {

    private String name;

    public SomethingExceptionallyPyish(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      throw new DeferredValueException("Can't serialize");
    }
  }

  public static class SomethingPyish implements PyishSerializable {

    private String name;

    public SomethingPyish(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
