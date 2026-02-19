package com.hubspot.jinjava.el.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public abstract class ReturnTypeValidatorConfig {

  // These aren't required, but they prevent someone from misconfiguring Jinjava to allow sandbox bypass unintentionally
  private static final String JAVA_LANG_REFLECT_PACKAGE =
    Method.class.getPackage().getName(); // java.lang.reflect
  private static final String JACKSON_DATABIND_PACKAGE =
    ObjectMapper.class.getPackage().getName(); // com.fasterxml.jackson.databind

  private static final String[] BANNED_PREFIXES = {
    Class.class.getCanonicalName(),
    Object.class.getCanonicalName(),
    JAVA_LANG_REFLECT_PACKAGE,
    JACKSON_DATABIND_PACKAGE,
  };

  public abstract ImmutableSet<String> allowedCanonicalClassPrefixes();

  public abstract ImmutableSet<String> allowedCanonicalClassNames();

  @Value.Default
  public boolean allowArrays() {
    return false;
  }

  @Value.Check
  void banClassesAndMethods() {
    if (
      Stream
        .of(
          allowedCanonicalClassPrefixes().stream(),
          allowedCanonicalClassNames().stream()
        )
        .flatMap(Function.identity())
        .anyMatch(prefixOrName ->
          Arrays
            .stream(BANNED_PREFIXES)
            .anyMatch(banned ->
              banned.startsWith(prefixOrName) || prefixOrName.startsWith(banned)
            )
        )
    ) {
      throw new IllegalStateException(
        "Banned classes or prefixes (Object.class, Class.class, java.lang.reflect, com.fasterxml.jackson.databind) are not allowed"
      );
    }
  }

  public static ReturnTypeValidatorConfig of() {
    return ImmutableReturnTypeValidatorConfig.of();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableReturnTypeValidatorConfig.Builder {

    Builder() {}

    public Builder addDefaultAllowlistGroups() {
      return addAllowlistGroups(AllowlistGroup.values());
    }

    public Builder addAllowlistGroups(AllowlistGroup... allowlistGroups) {
      for (AllowlistGroup allowlistGroup : allowlistGroups) {
        this.addAllowedCanonicalClassPrefixes(
            allowlistGroup.allowedReturnTypeCanonicalClassPrefixes()
          )
          .addAllowedCanonicalClassNames(allowlistGroup.allowedReturnTypeClasses());
        if (allowlistGroup.enableArrays()) {
          this.setAllowArrays(true);
        }
      }
      return this;
    }
  }
}
