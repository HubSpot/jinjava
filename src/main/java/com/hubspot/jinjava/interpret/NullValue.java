package com.hubspot.jinjava.interpret;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hubspot.jinjava.interpret.NullValue.NullValueSerializer;
import java.io.IOException;

/**
 * Marker object of a `null` value. A null value in the map is usually considered
 * the key does not exist. For example map = {"a": null}, if map.get("a") == null,
 * we treat it as the there is not key "a" in the map.
 */
@JsonSerialize(using = NullValueSerializer.class)
public final class NullValue {

  public static final NullValue INSTANCE = new NullValue();

  public static class NullValueSerializer extends StdSerializer<NullValue> {

    public NullValueSerializer() {
      this(null);
    }

    protected NullValueSerializer(Class<NullValue> t) {
      super(t);
    }

    @Override
    public void serialize(
      NullValue value,
      JsonGenerator jgen,
      SerializerProvider provider
    ) throws IOException {
      jgen.writeNull();
    }
  }

  private NullValue() {}

  public static NullValue instance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "null";
  }
}
