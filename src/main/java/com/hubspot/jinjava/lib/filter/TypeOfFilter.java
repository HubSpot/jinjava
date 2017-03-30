package com.hubspot.jinjava.lib.filter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.date.PyishDate;


@JinjavaDoc(
    value = "Get a string that describes the type of the object")
public class TypeOfFilter implements Filter {

  private static final Set<Class<?>> SIMPLE_NAME_TYPES = ImmutableSet.of(String.class);

  @Override
  public String getName() {
    return "typeof";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return "null";
    }

    if (var.getClass() == AstDict.class || var.getClass() == PyMap.class) {
      return "dict";
    }

    if (var.getClass() == AstList.class || var.getClass() == ArrayList.class || var.getClass() == PyList.class) {
      return "list";
    }

    if (var.getClass() == Boolean.class) {
      return "boolean";
    }

    if (var.getClass() == AstTuple.class) {
      return "tuple";
    }

    if (var.getClass() == PyishDate.class || var.getClass() == ZonedDateTime.class) {
      return "datetime";
    }

    if (Number.class.isAssignableFrom(var.getClass())) {
      return "number";
    }

    if (SIMPLE_NAME_TYPES.contains(var.getClass())) {
      return var.getClass().getSimpleName().toLowerCase();
    }

    return "unknown";
  }

}
