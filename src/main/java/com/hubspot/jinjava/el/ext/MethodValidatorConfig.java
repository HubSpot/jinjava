package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public abstract class MethodValidatorConfig {

  public abstract ImmutableSet<Method> allowedMethods();

  public abstract ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassPrefixes();

  public abstract ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassNames();

  @Value.Default
  public Consumer<Method> onRejectedMethod() {
    return m -> {};
  }

  @Value.Check
  void banClassesAndMethods() {
    List<String> list = BannedAllowlistOptions.findBannedPrefixes(
      Stream
        .of(
          allowedMethods()
            .stream()
            .map(method -> method.getDeclaringClass().getCanonicalName()),
          allowedDeclaredMethodsFromCanonicalClassPrefixes().stream(),
          allowedDeclaredMethodsFromCanonicalClassNames().stream()
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

  public static MethodValidatorConfig of() {
    return ImmutableMethodValidatorConfig.of();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableMethodValidatorConfig.Builder {

    Builder() {}

    public Builder addDefaultAllowlistGroups() {
      return addAllowlistGroups(AllowlistGroup.values());
    }

    public Builder addAllowlistGroups(AllowlistGroup... allowlistGroups) {
      for (AllowlistGroup allowlistGroup : allowlistGroups) {
        this.addAllowedMethods(allowlistGroup.allowMethods())
          .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
            allowlistGroup.allowedDeclaredMethodsFromCanonicalClassPrefixes()
          )
          .addAllowedDeclaredMethodsFromCanonicalClassNames(
            allowlistGroup.allowedDeclaredMethodsFromClasses()
          );
      }
      return this;
    }
  }
}
