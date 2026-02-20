package com.hubspot.jinjava;

import org.immutables.value.Value;

@Value.Style(
  init = "set*",
  get = { "is*", "get*" } // Detect 'get' and 'is' prefixes in accessor methods
)
public @interface JinjavaImmutableStyle {
  @Value.Style(
    init = "with*",
    get = { "is*", "get*" } // Detect 'get' and 'is' prefixes in accessor methods
  )
  @interface WithStyle {
  }
}
