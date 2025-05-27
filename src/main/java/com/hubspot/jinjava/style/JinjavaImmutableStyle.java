package com.hubspot.jinjava.style;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value.Style;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
@JsonSerialize
@Style(
  get = { "is*", "get*" },
  init = "set*",
  typeAbstract = { "Abstract*", "*IF" },
  typeImmutable = "*",
  optionalAcceptNullable = true,
  forceJacksonPropertyNames = false,
  visibility = ImplementationVisibility.SAME,
  redactedMask = "**REDACTED**"
)
public @interface JinjavaImmutableStyle {
}
