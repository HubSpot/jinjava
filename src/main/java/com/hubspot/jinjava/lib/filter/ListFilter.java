package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

@JinjavaDoc(
  value = "Convert the value into a list. If it was a string the returned list will be a list of characters.",
  input = @JinjavaParam(
    value = "value",
    desc = "Value to add to a sequence",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% set one = 1 %}\n" +
      "{% set two = 2 %}\n" +
      "{% set three = 3 %}\n" +
      "{% set list_num = one|list + two|list + three|list %}\n" +
      "{{ list_num|list }}"
    )
  }
)
public class ListFilter implements Filter {

  @Override
  public String getName() {
    return "list";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<?> result;

    if (var == null) {
      return null;
    }

    if (var instanceof String) {
      result = Chars.asList(((String) var).toCharArray());
    } else if (Collection.class.isAssignableFrom(var.getClass())) {
      result = Lists.newArrayList((Collection<?>) var);
    } else if (var.getClass().isArray()) {
      if (var instanceof boolean[]) {
        Boolean[] outputBoxed = ArrayUtils.toObject((boolean[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof byte[]) {
        Byte[] outputBoxed = ArrayUtils.toObject((byte[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof char[]) {
        Character[] outputBoxed = ArrayUtils.toObject((char[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof short[]) {
        Short[] outputBoxed = ArrayUtils.toObject((short[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof int[]) {
        Integer[] outputBoxed = ArrayUtils.toObject((int[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof long[]) {
        Long[] outputBoxed = ArrayUtils.toObject((long[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof float[]) {
        Float[] outputBoxed = ArrayUtils.toObject((float[]) var);
        result = Arrays.asList(outputBoxed);
      } else if (var instanceof double[]) {
        Double[] outputBoxed = ArrayUtils.toObject((double[]) var);
        result = Arrays.asList(outputBoxed);
      } else {
        result = Arrays.asList((Object[]) var);
      }
    } else {
      result = Lists.newArrayList(var);
    }

    return result;
  }
}
