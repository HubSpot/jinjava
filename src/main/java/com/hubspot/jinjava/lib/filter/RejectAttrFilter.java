package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Filters a sequence of objects by applying a test to an attribute of an object or the attribute and "
        + "rejecting the ones with the test succeeding.",
    params = {
        @JinjavaParam(value = "seq", type = "sequence", desc = "Sequence to test"),
        @JinjavaParam(value = "attribute", desc = "Attribute to test for and reject items that contain it"),
        @JinjavaParam(value = "exp_test", type = "name of expression test", defaultValue = "truthy", desc = "Specify which expression test to run for making the rejection")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This loop would reject any post containing content.post_list_summary_featured_image",
            code = "{% for content in contents|rejectattr('post_list_summary_featured_image') %}\n" +
                "    <div class=\"post-item\">Post in listing markup</div>\n" +
                "{% endfor %}")
    })
public class RejectAttrFilter implements Filter {

  @Override
  public String getName() {
    return "rejectattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<Object> result = new ArrayList<>();

    if (args.length == 0) {
      throw new InterpretException(getName() + " filter requires an attr to filter on", interpreter.getLineNumber());
    }

    String[] expArgs = new String[]{};
    String attr = args[0];

    ExpTest expTest = interpreter.getContext().getExpTest("truthy");
    if (args.length > 1) {
      expTest = interpreter.getContext().getExpTest(args[1]);
      if (expTest == null) {
        throw new InterpretException("No expression test defined with name '" + args[1] + "'",
                                     interpreter.getLineNumber());
      }
    }

    if (args.length > 2) {
      expArgs = Arrays.copyOfRange(args, 2, args.length);
    }
    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();
      Object attrVal = interpreter.resolveProperty(val, attr);

      if (!expTest.evaluate(attrVal, interpreter, (Object[]) expArgs)) {
        result.add(val);
      }
    }

    return result;
  }

}
