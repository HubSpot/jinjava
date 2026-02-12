package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaImmutableStyle;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.filter.FilterLibrary;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;
import org.immutables.value.Value;

public class MethodAllowlists {

  @Value.Immutable
  @JinjavaImmutableStyle
  interface MethodAllowlist {
    ImmutableSet<Method> allowCustomMethods();
    ImmutableSet<Class<?>> allowCustomDeclaredMethodsFromClasses();
    ImmutableSet<AllowlistGroup> allowlistGroups();
  }

  enum AllowlistGroup {
    JavaString {
      private static final Class<?>[] ARRAY = { String.class };

      @Override
      Class<?>[] allowDeclaredMethodsFromClasses() {
        return ARRAY;
      }
    },
    JinjavaFilters {
      private static final Class<?>[] ARRAY = Stream
        .concat(Stream.of(Filter.class), Arrays.stream(FilterLibrary.DEFAULT_FILTERS))
        .toArray(Class<?>[]::new);

      @Override
      Class<?>[] allowDeclaredMethodsFromClasses() {
        return ARRAY;
      }
    },
    ReadOnlyCollectionsg,
    WritableCollections,
    JinjavaFunctions;

    Method[] allowMethods() {
      return new Method[0];
    }

    Class<?>[] allowDeclaredMethodsFromClasses() {
      return new Class[0];
    }
  }
}
