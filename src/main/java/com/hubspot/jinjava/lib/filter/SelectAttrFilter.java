package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.Variable;

@JinjavaDoc(
    value = "Filters a sequence of objects by applying a test to an attribute of an object and only selecting the ones with the test succeeding.",
    params = {
        @JinjavaParam(value = "sequence", type = "sequence", desc = "Sequence to test"),
        @JinjavaParam(value = "attr", desc = "Attribute to test for and select items that contain it"),
        @JinjavaParam(value = "exp_test", type = "name of expression test", defaultValue = "truthy", desc = "Specify which expression test to run for making the selection")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This loop would select any post containing content.post_list_summary_featured_image",
            code = "{% for content in contents|selectattr('post_list_summary_featured_image') %}\n" +
                "    <div class=\"post-item\">Post in listing markup</div>\n" +
                "{% endfor %}")
    })
public class SelectAttrFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "selectattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    return applyFilter(var, interpreter, args, kwargs, true);
  }

  protected Object applyFilter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs, boolean acceptObjects) {
    List<Object> result = new ArrayList<>();

    if (args.length == 0) {
      throw new InterpretException(getName() + " filter requires an attr to filter on", interpreter.getLineNumber());
    }

    if (!(args[0] instanceof String)) {
      throw new InterpretException(getName() + " filter requires the filter attr arg to be a string", interpreter.getLineNumber());
    }

    Object[] expArgs = new String[]{};

    String attr = (String) args[0];

    ExpTest expTest = interpreter.getContext().getExpTest("truthy");
    if (args.length > 1) {

      if (!(args[1] instanceof String)) {
        throw new InterpretException(getName() + " filter requires the expression test arg to be a string", interpreter.getLineNumber());
      }

      expTest = interpreter.getContext().getExpTest((String) args[1]);
      if (expTest == null) {
        throw new InterpretException("No expression test defined with name '" + args[1] + "'", interpreter.getLineNumber());
      }

      if (args.length > 2) {
        expArgs = Arrays.copyOfRange(args, 2, args.length);
      }
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();

      Object attrVal = new Variable(interpreter, String.format("%s.%s", "placeholder", attr)).resolve(val);
      if (acceptObjects == expTest.evaluate(attrVal, interpreter, expArgs)) {
        result.add(val);
      }
    }

    return result;
  }
}
