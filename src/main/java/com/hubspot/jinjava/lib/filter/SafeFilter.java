package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

/**
 * Mark the value as safe which means that in an environment with automatic escaping enabled this variable will not be escaped.
 *
 * This is currently implemented as a pass-through for the given variable.
 *
 */
@JinjavaDoc(
    value = "Mark the value as safe, which means that in an environment with automatic escaping enabled this variable will not be escaped.",
    snippets = {
        @JinjavaSnippet(code = "{{ content.post_list_content|safe }}")
    })
public class SafeFilter implements Filter {

  @Override
  public String getName() {
    return "safe";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter,
      String... args) {
    return var;
  }

}
