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
  value = "Filters a sequence of objects by applying a test to the object and only selecting the ones with the test succeeding.",
  input = @JinjavaParam(
    value = "sequence",
    type = "sequence",
    desc = "Sequence to test",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "exp_test",
      type = "name of expression test",
      defaultValue = "truthy",
      desc = "Specify which expression test to run for making the selection"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% set some_numbers = [10, 12, 13, 3, 5, 17, 22] %}\n" +
      "{% some_numbers|select('even') %}"
    )
  }
)
public class SelectFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "select";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    List<Object> result = new ArrayList<>();

    if (args.length == 0) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (name of expression test to filter by)"
      );
    }

    if (args[0] == null) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NULL,
        "exp_test"
      );
    }

    Object[] expArgs = new Object[] {};

    if (args.length > 1) {
      expArgs = Arrays.copyOfRange(args, 1, args.length);
    }

    ExpTest expTest = interpreter.getContext().getExpTest(args[0].toString());
    if (expTest == null) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.EXPRESSION_TEST,
        0,
        args[0].toString()
      );
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();

      if (expTest.evaluate(val, interpreter, expArgs)) {
        result.add(val);
      }
    }

    return result;
  }
}
