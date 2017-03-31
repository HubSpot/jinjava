package com.hubspot.jinjava.lib.fn;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.objects.date.PyishDate;


@JinjavaDoc(
    value = "Get a string that describes the type of the object, similar to Python's type()")
public class TypeFunction {

  public static String type(Object var) {
    if (var == null) {
      return "null";
    }

    if (var.getClass() == AstDict.class || Map.class.isAssignableFrom(var.getClass())) {
      return "dict";
    }

    if (var.getClass() == AstList.class || List.class.isAssignableFrom(var.getClass())) {
      return "list";
    }

    if (var.getClass() == Boolean.class) {
      return "bool";
    }

    if (var.getClass() == AstTuple.class) {
      return "tuple";
    }

    if (var.getClass() == PyishDate.class || var.getClass() == ZonedDateTime.class) {
      return "datetime";
    }

    if (Integer.class.isAssignableFrom(var.getClass())) {
      return "int";
    }

    if (Long.class.isAssignableFrom(var.getClass())) {
      return "long";
    }

    if (Float.class.isAssignableFrom(var.getClass()) || Double.class.isAssignableFrom(var.getClass())) {
      return "float";
    }

    if (String.class.isAssignableFrom(var.getClass())) {
      return "str";
    }

    return "unknown";
  }

}
