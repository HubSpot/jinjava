package com.hubspot.jinjava.el.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyList;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import com.hubspot.jinjava.util.ForLoop;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
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

  private static final String[] BANNED_PREFIXES = {
    Class.class.getCanonicalName(),
    Object.class.getCanonicalName(),
    JAVA_LANG_REFLECT_PACKAGE,
    JACKSON_DATABIND_PACKAGE,
  };

  public abstract ImmutableSet<Method> allowedMethods();

  public abstract ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassPrefixes();

  public abstract ImmutableSet<String> allowedDeclaredMethodsFromCanonicalClassNames();

  public abstract ImmutableSet<String> allowedResultCanonicalClassPrefixes();

  public abstract ImmutableSet<String> allowedResultCanonicalClassNames();

  @Value.Check
  void banClassesAndMethods() {
    if (
      allowedMethods()
        .stream()
        .map(method -> method.getDeclaringClass().getCanonicalName())
        .anyMatch(canonicalName ->
          Arrays
            .stream(BANNED_PREFIXES)
            .anyMatch(banned ->
              Arrays.stream(BANNED_PREFIXES).anyMatch(canonicalName::startsWith)
            )
        )
    ) {
      throw new IllegalStateException(
        "Methods from banned classes or packages (Object.class, Class.class, java.lang.reflect, com.fasterxml.jackson.databind) are not allowed"
      );
    }
    if (
      Stream
        .of(
          allowedDeclaredMethodsFromCanonicalClassPrefixes().stream(),
          allowedDeclaredMethodsFromCanonicalClassNames().stream(),
          allowedResultCanonicalClassPrefixes().stream(),
          allowedResultCanonicalClassNames().stream()
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

  public static MethodValidatorConfig of() {
    return ImmutableMethodValidatorConfig.of();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends ImmutableMethodValidatorConfig.Builder {

    Builder() {}/**/

    public Builder addDefaultAllowlistGroups() {
      return addAllowlistGroups(AllowlistGroup.values());
    }

    public Builder addAllowlistGroups(AllowlistGroup... allowlistGroups) {
      for (AllowlistGroup allowlistGroup : allowlistGroups) {
        this.addAllowedMethods(allowlistGroup.allowMethods())
          .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
            allowlistGroup.allowedDeclaredMethodsFromCanonicalClassPrefixes()
          )
          .addAllAllowedDeclaredMethodsFromCanonicalClassNames(
            Arrays
              .stream(allowlistGroup.allowedDeclaredMethodsFromClasses())
              .map(Class::getCanonicalName)
              .toList()
          )
          .addAllowedResultCanonicalClassPrefixes(
            allowlistGroup.allowedResultCanonicalClassPrefixes()
          )
          .addAllAllowedResultCanonicalClassNames(
            Arrays
              .stream(allowlistGroup.allowedResultClasses())
              .map(Class::getCanonicalName)
              .toList()
          );
      }
      return this;
    }

    public enum AllowlistGroup {
      JavaPrimitives {
        private static final Class<?>[] ARRAY = {
          String.class,
          Long.class,
          Integer.class,
          Double.class,
          Byte.class,
          Character.class,
          Float.class,
          Boolean.class,
          Short.class,
          long.class,
          int.class,
          double.class,
          byte.class,
          char.class,
          float.class,
          boolean.class,
          short.class,
        };

        @Override
        Class<?>[] allowedResultClasses() {
          return ARRAY;
        }
      },
      JinjavaFilters {
        private static final String[] ARRAY = { Filter.class.getPackageName() };

        @Override
        String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
          return ARRAY;
        }
      },
      Collections {
        private static final Class<?>[] ARRAY = {
          PyList.class,
          PyMap.class,
          SizeLimitingPyMap.class,
          SizeLimitingPyList.class,
        };

        @Override
        Class<?>[] allowedDeclaredMethodsFromClasses() {
          return ARRAY;
        }
      },
      JinjavaFunctions,
      JinjavaExpTests {
        private static final String[] ARRAY = { ExpTest.class.getPackageName() };

        @Override
        String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
          return ARRAY;
        }
      },
      JinjavaTagConstructs {
        private static final Class<?>[] ARRAY = { ForLoop.class };

        @Override
        Class<?>[] allowedDeclaredMethodsFromClasses() {
          return ARRAY;
        }
      };

      Method[] allowMethods() {
        return new Method[0];
      }

      String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
        return new String[0];
      }

      Class<?>[] allowedDeclaredMethodsFromClasses() {
        return new Class[0];
      }

      String[] allowedResultCanonicalClassPrefixes() {
        return new String[0];
      }

      Class<?>[] allowedResultClasses() {
        return new Class[0];
      }
    }
  }
}
