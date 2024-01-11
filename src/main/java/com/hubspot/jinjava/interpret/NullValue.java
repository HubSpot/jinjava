package com.hubspot.jinjava.interpret;

/**
 * Marker object of a `null` value. A null value in the map is usually considered
 * the key does not exist. For example map = {"a": null}, if map.get("a") == null,
 * we treat it as the there is not key "a" in the map.
 */
public final class NullValue {

  public static final NullValue INSTANCE = new NullValue();

  private NullValue() {}

  public static NullValue instance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "null";
  }
}
