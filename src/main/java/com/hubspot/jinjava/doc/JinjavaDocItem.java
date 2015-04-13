package com.hubspot.jinjava.doc;

public abstract class JinjavaDocItem {

  private final String name;
  private final String desc;
  private final String aliasOf;
  private final JinjavaDocParam[] params;
  private final JinjavaDocSnippet[] snippets;
  
  public JinjavaDocItem(String name, String desc, String aliasOf, JinjavaDocParam[] params, JinjavaDocSnippet[] snippets) {
    this.name = name;
    this.desc = desc;
    this.aliasOf = aliasOf;
    this.params = params;
    this.snippets = snippets;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }

  public String getAliasOf() {
    return aliasOf;
  }
  
  public JinjavaDocParam[] getParams() {
    return params;
  }
  
  public JinjavaDocSnippet[] getSnippets() {
    return snippets;
  }
  
}
