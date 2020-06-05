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
import java.util.concurrent.ThreadLocalRandom;

@JinjavaDoc(
  value = "Filters a sequence of objects by applying a test to an attribute of an object and only selecting the ones with the test succeeding.",
  input = @JinjavaParam(
    value = "sequence",
    type = "sequence",
    desc = "Sequence to test",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "attr",
      desc = "Attribute to test for and select items that contain it",
      required = true
    ),
    @JinjavaParam(
      value = "exp_test",
      type = "name of expression test",
      defaultValue = "truthy",
      desc = "Specify which expression test to run for making the selection"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This loop would select any post containing content.post_list_summary_featured_image",
      code = "{% for content in contents|selectattr('post_list_summary_featured_image') %}\n" +
      "    <div class=\"post-item\">Post in listing markup</div>\n" +
      "{% endfor %}"
    )
  }
)
public class SelectAttrFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "selectattr";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    return applyFilter(var, interpreter, args, kwargs, true);
  }

  protected Object applyFilter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs,
    boolean acceptObjects
  ) {
    List<Object> result = new ArrayList<>();

    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires at least 1 argument (attr to filter on)"
      );
    }

    if (args[0] == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 0);
    }

    String attr = args[0].toString();

    Object[] expArgs = new String[] {};

    ExpTest expTest = interpreter.getContext().getExpTest("truthy");
    if (args.length > 1) {
      if (args[1] == null) {
        throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 1);
      }

      expTest = interpreter.getContext().getExpTest(args[1].toString());
      if (expTest == null) {
        throw new InvalidArgumentException(
          interpreter,
          this,
          InvalidReason.EXPRESSION_TEST,
          1,
          args[1].toString()
        );
      }

      if (args.length > 2) {
        expArgs = Arrays.copyOfRange(args, 2, args.length);
      }
    }

    String tempValue = generateTempVariable();
    String expression = generateTempVariable(tempValue, attr);
    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();
      interpreter.getContext().put(tempValue, val);

      Object attrVal = interpreter.resolveELExpression(
        expression,
        interpreter.getLineNumber()
      );

      if (acceptObjects == expTest.evaluate(attrVal, interpreter, expArgs)) {
        result.add(val);
      }
    }

    return result;
  }

  private String generateTempVariable() {
    return "jj_temp_" + Math.abs(ThreadLocalRandom.current().nextInt());
  }

  private String generateTempVariable(String tempValue, String expression) {
    return String.format("%s.%s", tempValue, expression).trim();
  }
}
