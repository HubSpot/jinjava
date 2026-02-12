package com.hubspot.jinjava;

import com.hubspot.immutable.collection.encoding.ImmutableListEncodingEnabled;
import com.hubspot.immutable.collection.encoding.ImmutableMapEncodingEnabled;
import com.hubspot.immutable.collection.encoding.ImmutableSetEncodingEnabled;
import org.immutables.value.Value;

@Value.Style(
  init = "with*",
  get = { "is*", "get*" } // Detect 'get' and 'is' prefixes in accessor methods
)
@ImmutableSetEncodingEnabled
@ImmutableListEncodingEnabled
@ImmutableMapEncodingEnabled
public @interface JinjavaImmutableStyle {
}
