package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.Objects;

public class NamedParameter implements PyishSerializable {
  private final String name;
  private final Object value;

  public NamedParameter(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Objects.toString(value, "");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) appendable
      .append(name)
      .append('=')
      .append(PyishSerializable.writeValueAsString(value));
  }
}
