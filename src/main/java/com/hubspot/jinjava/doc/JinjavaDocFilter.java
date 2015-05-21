package com.hubspot.jinjava.doc;

import java.util.Map;

public class JinjavaDocFilter extends JinjavaDocItem {

  public JinjavaDocFilter(String name, String desc, String aliasOf, boolean deprecated, JinjavaDocParam[] params, JinjavaDocSnippet[] snippets, Map<String, String> meta) {
    super(name, desc, aliasOf, deprecated, params, snippets, meta);
  }

}
