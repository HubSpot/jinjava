package com.hubspot.jinjava.doc;

public class JinjavaDocTag extends JinjavaDocItem {

  private final boolean empty;
  
  public JinjavaDocTag(String name, boolean empty, String desc, String aliasOf, JinjavaDocParam[] params, JinjavaDocSnippet[] snippets) {
    super(name, desc, aliasOf, params, snippets);
    this.empty = empty;
  }

  public boolean isEmpty() {
    return empty;
  }

}
