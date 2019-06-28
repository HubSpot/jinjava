package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Returns true if a list contains a value",
    input = @JinjavaParam(value = "list", type = "list", required = true),
    params = @JinjavaParam(value = "value", type = "object", desc = "The value to check is in the list", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3] is containing 2 }}")
    })
public class IsContainingExpTest extends CollectionExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (args == null || args.length == 0) {
      return false;
    }

    return (Boolean) COLLECTION_MEMBERSHIP_OPERATOR.apply(TYPE_CONVERTER, args[0], var);
  }

  @Override
  public String getName() {
    return "containing";
  }
}
