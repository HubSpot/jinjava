package com.hubspot.jinjava.doc;

import java.util.Map;

public class JinjavaDocTag extends JinjavaDocItem {

  private final boolean empty;

  public JinjavaDocTag(String name, boolean empty, String desc, String aliasOf, boolean deprecated, JinjavaDocParam[] params, JinjavaDocSnippet[] snippets, Map<String, String> meta) {
    super(name, desc, aliasOf, deprecated, params, snippets, meta);
    this.empty = empty;
  }

  public boolean isEmpty() {
    return empty;
  }

}
