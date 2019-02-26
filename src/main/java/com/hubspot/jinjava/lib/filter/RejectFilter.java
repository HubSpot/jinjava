package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;

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

@JinjavaDoc(
    value = "Filters a sequence of objects by applying a test to the object and rejecting the ones with the test succeeding.",
    input = @JinjavaParam(value = "seq", type = "Sequence to test"),
    params = {
        @JinjavaParam(value = "exp_test", type = "name of expression test", desc = "Specify which expression test to run for making the selection")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set some_numbers = [10, 12, 13, 3, 5, 17, 22] %}\n" +
                "{% some_numbers|reject('even') %}")
    })
public class RejectFilter implements Filter {

  @Override
  public String getName() {
    return "reject";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<Object> result = new ArrayList<>();

    if (args.length == 0) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (name of expression test to filter by)");
    }

    if (args[0] == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 0);
    }

    ExpTest expTest = interpreter.getContext().getExpTest(args[0]);
    if (expTest == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.EXPRESSION_TEST, 0, args[0]);
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();

      if (!expTest.evaluate(val, interpreter)) {
        result.add(val);
      }
    }

    return result;
  }

}
