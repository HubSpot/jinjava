package com.hubspot.jinjava.objects;

import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
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
  public String toPyishString() {
    return String.format("namespace(%s)", PyishSerializable.super.toPyishString());
  }
}
