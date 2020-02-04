package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

/**
 * Mark the value as safe which means that in an environment with automatic escaping enabled this variable will not be escaped.
 */
@JinjavaDoc(
    value = "Mark the value as safe, which means that in an environment with automatic escaping enabled this variable will not be escaped.",
    input = @JinjavaParam(value = "value", desc = "Value to mark as safe", required = true),
    snippets = {
        @JinjavaSnippet(code = "{{ content.post_list_content|safe }}")
    })
public class SafeFilter implements Filter {

  @Override
  public String getName() {
    return "safe";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return null;
    }

    if (!(var instanceof String)) {
      return var;
    }

    return new SafeString((String) var);
  }
}
