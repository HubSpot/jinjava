package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
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
  public StringBuilder appendPyishString(StringBuilder sb) {
    return sb
      .append(name)
      .append('=')
      .append(PyishSerializable.writeValueAsString(value));
  }
}
