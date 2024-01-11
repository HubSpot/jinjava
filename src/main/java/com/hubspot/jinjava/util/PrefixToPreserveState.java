package com.hubspot.jinjava.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Beta
public class PrefixToPreserveState extends ForwardingMap<String, String> {

  private Map<String, String> reconstructedValues;

  public PrefixToPreserveState() {
    reconstructedValues = new LinkedHashMap<>();
  }

  public PrefixToPreserveState(Map<String, String> reconstructedValues) {
    this.reconstructedValues = reconstructedValues;
  }

  @Override
  protected Map<String, String> delegate() {
    return reconstructedValues;
  }

  @Override
  public String toString() {
    return String.join("", reconstructedValues.values());
  }

  public PrefixToPreserveState withAllInFront(Map<String, String> toInsert) {
    Map<String, String> newMap = new LinkedHashMap<>(toInsert);
    reconstructedValues.forEach(newMap::putIfAbsent);
    reconstructedValues = newMap;
    return this;
  }

  public PrefixToPreserveState withAll(Map<String, String> toPut) {
    putAll(toPut);
    return this;
  }
}
