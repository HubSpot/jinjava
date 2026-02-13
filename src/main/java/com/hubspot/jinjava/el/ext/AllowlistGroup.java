package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingSet;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.objects.DummyObject;
import com.hubspot.jinjava.objects.Namespace;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.collections.PySet;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyList;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import com.hubspot.jinjava.objects.collections.SizeLimitingPySet;
import com.hubspot.jinjava.objects.collections.SnakeCaseAccessibleMap;
import com.hubspot.jinjava.objects.date.FormattedDate;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.util.ForLoop;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Map;

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
      BigDecimal.class,
    };

    @Override
    Class<?>[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    Class<?>[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  JinjavaObjects {
    private static final Class<?>[] ARRAY = {
      PyList.class,
      PyMap.class,
      SizeLimitingPyMap.class,
      SizeLimitingPyList.class,
      SnakeCaseAccessibleMap.class,
      FormattedDate.class,
      PyishDate.class,
      DummyObject.class,
      Namespace.class,
      SafeString.class,
    };

    @Override
    Class<?>[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    Class<?>[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  Collections {
    private static final Class<?>[] ARRAY = {
      Map.Entry.class,
      PyList.class,
      PyMap.class,
      PySet.class,
      SizeLimitingPyMap.class,
      SizeLimitingPyList.class,
      SizeLimitingPySet.class,
      ArrayList.class,
      ForwardingList.class,
      ForwardingMap.class,
      ForwardingSet.class,
      ForwardingCollection.class,
      AbstractCollection.class,
    };

    @Override
    Class<?>[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    Class<?>[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  JinjavaTagConstructs {
    private static final Class<?>[] ARRAY = { ForLoop.class };

    @Override
    Class<?>[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    Class<?>[] allowedDeclaredMethodsFromClasses() {
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
  JinjavaFunctions,
  JinjavaExpTests {
    private static final String[] ARRAY = { ExpTest.class.getPackageName() };

    @Override
    String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
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

  String[] allowedReturnTypeCanonicalClassPrefixes() {
    return new String[0];
  }

  Class<?>[] allowedReturnTypeClasses() {
    return new Class[0];
  }
}
