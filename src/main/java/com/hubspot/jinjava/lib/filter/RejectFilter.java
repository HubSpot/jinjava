package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@JinjavaDoc(
  value = "Filters a sequence of objects by applying a test to the object and rejecting the ones with the test succeeding.",
  input = @JinjavaParam(value = "seq", type = "Sequence to test", required = true),
  params = {
    @JinjavaParam(
      value = "exp_test",
      type = "name of expression test",
      desc = "Specify which expression test to run for making the selection",
      required = true
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% set some_numbers = [10, 12, 13, 3, 5, 17, 22] %}\n" +
      "{% some_numbers|reject('even') %}"
    )
  }
)
public class RejectFilter extends SelectFilter {

  @Override
  public String getName() {
    return "reject";
  }

  @Override
  boolean evaluate(
    JinjavaInterpreter interpreter,
    Object[] expArgs,
    ExpTest expTest,
    Object val
  ) {
    return !super.evaluate(interpreter, expArgs, expTest, val);
  }
}
