package com.hubspot.jinjava.el.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public abstract class MethodValidatorConfig {

  // These aren't required, but they prevent someone from misconfiguring Jinjava to allow sandbox bypass unintentionally
  private static final String JAVA_LANG_REFLECT_PACKAGE =
    Method.class.getPackage().getName(); // java.lang.reflect
  private static final String JACKSON_DATABIND_PACKAGE =
    ObjectMapper.class.getPackage().getName(); // com.fasterxml.jackson.databind

  public abstract ImmutableSet<Method> allowedMethods();

  public abstract ImmutableSet<Class<?>> allowedDeclaredMethodsFromClasses();

  public abstract ImmutableSet<String> allowedDeclaredMethodsFromPackages();

  public abstract ImmutableSet<Class<?>> allowedResultClasses();

  public abstract ImmutableSet<String> allowedResultPackages();

  @Value.Check
  void banClassesAndMethods() {
    if (
      allowedMethods()
        .stream()
        .anyMatch(method ->
          Class.class.equals(method.getDeclaringClass()) ||
          Object.class.equals(method.getDeclaringClass()) ||
          method.getDeclaringClass().getName().startsWith(JAVA_LANG_REFLECT_PACKAGE) ||
          method.getDeclaringClass().getName().startsWith(JACKSON_DATABIND_PACKAGE)
        )
    ) {
      throw new IllegalStateException(
        "Methods from banned classes (Object.class, Class.class) are not allowed"
      );
    }
    if (
      Stream
        .concat(
          allowedDeclaredMethodsFromClasses().stream(),
          allowedResultClasses().stream()
        )
        .anyMatch(clazz ->
          Class.class.equals(clazz) ||
          Object.class.equals(clazz) ||
          clazz.getName().startsWith(JAVA_LANG_REFLECT_PACKAGE) ||
          clazz.getName().startsWith(JACKSON_DATABIND_PACKAGE)
        )
    ) {
      throw new IllegalStateException(
        "Banned classes (Object.class, Class.class) are not allowed"
      );
    }
    if (
      Stream
        .concat(
          allowedDeclaredMethodsFromPackages().stream(),
          allowedResultPackages().stream()
        )
        .anyMatch(p ->
          JAVA_LANG_REFLECT_PACKAGE.startsWith(p) ||
          JACKSON_DATABIND_PACKAGE.startsWith(p) ||
          p.startsWith(JAVA_LANG_REFLECT_PACKAGE) ||
          p.startsWith(JACKSON_DATABIND_PACKAGE)
        )
    ) {
      throw new IllegalStateException("Banned packages are not allowed");
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
  }
}
