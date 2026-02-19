package com.hubspot.jinjava.el.ext;

import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingSet;
import com.hubspot.jinjava.interpret.NullValue;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
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
import java.util.LinkedHashMap;
import java.util.Map;

public enum AllowlistGroup {
  JavaPrimitives {
    private static final String[] ARRAY = {
      String.class.getCanonicalName(),
      Long.class.getCanonicalName(),
      Integer.class.getCanonicalName(),
      Double.class.getCanonicalName(),
      Byte.class.getCanonicalName(),
      Character.class.getCanonicalName(),
      Float.class.getCanonicalName(),
      Boolean.class.getCanonicalName(),
      Short.class.getCanonicalName(),
      long.class.getCanonicalName(),
      int.class.getCanonicalName(),
      double.class.getCanonicalName(),
      byte.class.getCanonicalName(),
      char.class.getCanonicalName(),
      float.class.getCanonicalName(),
      boolean.class.getCanonicalName(),
      short.class.getCanonicalName(),
      BigDecimal.class.getCanonicalName(),
    };

    @Override
    String[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    String[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  JinjavaObjects {
    private static final String[] ARRAY = {
      PyList.class.getCanonicalName(),
      PyMap.class.getCanonicalName(),
      SizeLimitingPyMap.class.getCanonicalName(),
      SizeLimitingPyList.class.getCanonicalName(),
      SnakeCaseAccessibleMap.class.getCanonicalName(),
      FormattedDate.class.getCanonicalName(),
      PyishDate.class.getCanonicalName(),
      DummyObject.class.getCanonicalName(),
      Namespace.class.getCanonicalName(),
      SafeString.class.getCanonicalName(),
      NullValue.class.getCanonicalName(),
    };

    @Override
    String[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    String[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  Collections {
    private static final String[] ARRAY = {
      Map.Entry.class.getCanonicalName(),
      PyList.class.getCanonicalName(),
      PyMap.class.getCanonicalName(),
      PySet.class.getCanonicalName(),
      SizeLimitingPyMap.class.getCanonicalName(),
      SizeLimitingPyList.class.getCanonicalName(),
      SizeLimitingPySet.class.getCanonicalName(),
      ArrayList.class.getCanonicalName(),
      ForwardingList.class.getCanonicalName(),
      ForwardingMap.class.getCanonicalName(),
      ForwardingSet.class.getCanonicalName(),
      ForwardingCollection.class.getCanonicalName(),
      AbstractCollection.class.getCanonicalName(),
      LinkedHashMap.class.getCanonicalName(),
      "%s.Entry".formatted(LinkedHashMap.class.getCanonicalName()),
      "%s.LinkedValues".formatted(LinkedHashMap.class.getCanonicalName()),
    };

    @Override
    String[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    String[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }

    @Override
    boolean enableArrays() {
      return true;
    }
  },
  JinjavaTagConstructs {
    private static final String[] ARRAY = {
      ForLoop.class.getCanonicalName(),
      MacroFunction.class.getCanonicalName(),
      EagerMacroFunction.class.getCanonicalName(),
    };

    @Override
    String[] allowedReturnTypeClasses() {
      return ARRAY;
    }

    @Override
    String[] allowedDeclaredMethodsFromClasses() {
      return ARRAY;
    }
  },
  JinjavaFilters {
    private static final String[] ARRAY = { Filter.class.getPackageName() };

    @Override
    String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
      return ARRAY;
    }

    @Override
    String[] allowedReturnTypeCanonicalClassPrefixes() {
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

    @Override
    String[] allowedReturnTypeCanonicalClassPrefixes() {
      return ARRAY;
    }
  };

  Method[] allowMethods() {
    return new Method[0];
  }

  String[] allowedDeclaredMethodsFromCanonicalClassPrefixes() {
    return new String[0];
  }

  String[] allowedDeclaredMethodsFromClasses() {
    return new String[0];
  }

  String[] allowedReturnTypeCanonicalClassPrefixes() {
    return new String[0];
  }

  String[] allowedReturnTypeClasses() {
    return new String[0];
  }

  boolean enableArrays() {
    return false;
  }
}
