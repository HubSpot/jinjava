package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Returns true if a list contains all values in a second list",
    input = @JinjavaParam(value = "list", type="list", required = true),
    params = @JinjavaParam(value = "list_two", type="list", desc = "The second list to check if every element is in the first list", required = true),
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3] is containingall [2, 3] }}")
    })
public class IsContainingAllExpTest extends CollectionExpTest {

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {

    if (null == var || args.length == 0 || args[0] == null) {
      return false;
    }

    ForLoop loop = ObjectIterator.getLoop(args[0]);
    while (loop.hasNext()) {
      Object matchValue = loop.next();
      if (!(Boolean) COLLECTION_MEMBERSHIP_OPERATOR.apply(TYPE_CONVERTER, matchValue, var)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String getName() {
    return "containingall";
  }
}
