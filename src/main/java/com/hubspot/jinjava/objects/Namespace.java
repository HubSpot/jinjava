package com.hubspot.jinjava.objects;

import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Namespace extends SizeLimitingPyMap implements PyishSerializable {

  public Namespace() {
    this(new HashMap<>());
  }

  public Namespace(Map<String, Object> map) {
    this(map, Integer.MAX_VALUE);
  }

  public Namespace(Map<String, Object> map, int maxSize) {
    super(map, maxSize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) PyishSerializable.super
      .appendPyishString((T) appendable.append("namespace("))
      .append(')');
  }
}
