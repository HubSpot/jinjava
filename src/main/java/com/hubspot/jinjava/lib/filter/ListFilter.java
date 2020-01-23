package com.hubspot.jinjava.lib.filter;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

@JinjavaDoc(
    value = "Convert the value into a list. If it was a string the returned list will be a list of characters.",
    input = @JinjavaParam(value = "value", desc = "Value to add to a sequence", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{% set one = 1 %}\n" +
                "{% set two = 2 %}\n" +
                "{% set three = 3 %}\n" +
                "{% set list_num = one|list + two|list + three|list %}\n" +
                "{{ list_num|list }}")
    })
public class ListFilter implements Filter {

  @Override
  public String getName() {
    return "list";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<?> result;

    if (var instanceof String || var instanceof SafeString) {
      result = Chars.asList(var.toString().toCharArray());
    } else if (Collection.class.isAssignableFrom(var.getClass())) {
      result = Lists.newArrayList((Collection<?>) var);
    } else {
      result = Lists.newArrayList(var);
    }

    return result;
  }

}
