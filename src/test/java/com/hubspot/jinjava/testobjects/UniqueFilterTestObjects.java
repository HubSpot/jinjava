package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;

public class UniqueFilterTestObjects {

  public static class MyClass implements PyishSerializable {

    private final String name;

    public MyClass(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "[Name:" + name + "]";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append(toString());
    }
  }
}
