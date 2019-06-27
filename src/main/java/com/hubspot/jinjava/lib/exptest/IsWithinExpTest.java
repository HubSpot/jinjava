package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Returns true if a value is within a list",
    input = @JinjavaParam(value = "value", type = "object", required = true),
    params = @JinjavaParam(value = "list", type = "list", desc = "A list to check if the value is in.", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{{ 2 is within [1, 2, 3] }}")
    })
public class IsWithinExpTest extends CollectionExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (args == null || args.length == 0) {
      return false;
    }

    return (Boolean) COLLECTION_MEMBERSHIP_OPERATOR.apply(TYPE_CONVERTER, var, args[0]);
  }

  @Override
  public String getName() {
    return "within";
  }
}
