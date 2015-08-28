package com.hubspot.jinjava.lib.filter;

import java.util.LinkedHashMap;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Extract a unique set from a sequence of objects",
    params = {
        @JinjavaParam(value = "sequence", type = "sequence", desc = "Sequence to filter"),
        @JinjavaParam(value = "attr", type = "Optional attribute on object to use as unique identifier")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "Filter duplicated strings from a sequence of strings",
            code = "{{ ['foo', 'bar', 'foo', 'other'] | unique | join(', ') }}",
            output = "foo, bar, other"),
        @JinjavaSnippet(
            desc = "Filter out duplicate blog posts",
            code = "{% for content in contents|unique(attr='slug') %}\n"
                + "\n{% endfor %}")
    })
public class UniqueFilter implements Filter {

  @Override
  public String getName() {
    return "unique";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Map<Object, Object> result = new LinkedHashMap<>();
    String attr = null;

    if (args.length > 0) {
      attr = args[0];
    }

    ForLoop loop = ObjectIterator.getLoop(var);
    while (loop.hasNext()) {
      Object val = loop.next();
      Object key = val;

      if (attr != null) {
        key = interpreter.resolveProperty(val, attr);
      }

      if (!result.containsKey(key)) {
        result.put(key, val);
      }
    }

    return result.values();
  }

}
