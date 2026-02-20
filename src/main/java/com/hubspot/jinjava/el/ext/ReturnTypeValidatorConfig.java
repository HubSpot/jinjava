package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public abstract class ReturnTypeValidatorConfig {

  public abstract ImmutableSet<String> allowedCanonicalClassPrefixes();

  public abstract ImmutableSet<String> allowedCanonicalClassNames();

  @Value.Default
  public Consumer<Class<?>> onRejectedClass() {
    return m -> {};
  }

  @Value.Default
  public boolean allowArrays() {
    return false;
  }

  @Value.Check
  void banClassesAndMethods() {
    List<String> list = BannedAllowlistOptions.findBannedPrefixes(
      Stream
        .of(
          allowedCanonicalClassPrefixes().stream(),
          allowedCanonicalClassNames().stream()
        )
        .flatMap(Function.identity())
    );
    if (!list.isEmpty()) {
      throw new IllegalStateException(
        "Banned classes or prefixes (Object.class, Class.class, java.lang.reflect, com.fasterxml.jackson.databind) are not allowed: " +
        list
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
