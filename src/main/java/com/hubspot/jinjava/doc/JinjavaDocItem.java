package com.hubspot.jinjava.doc;

import java.util.Map;

public abstract class JinjavaDocItem {

  private final String name;
  private final String desc;
  private final String aliasOf;
  private final boolean deprecated;
  private final JinjavaDocParam[] params;
  private final JinjavaDocSnippet[] snippets;
  private final Map<String, String> meta;

  public JinjavaDocItem(String name, String desc, String aliasOf, boolean deprecated, JinjavaDocParam[] params, JinjavaDocSnippet[] snippets, Map<String, String> meta) {
    this.name = name;
    this.desc = desc;
    this.aliasOf = aliasOf;
    this.deprecated = deprecated;
    this.params = params.clone();
    this.snippets = snippets.clone();
    this.meta = meta;
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

  public boolean isDeprecated() {
    return deprecated;
  }

  public JinjavaDocParam[] getParams() {
    return params.clone();
  }

  public JinjavaDocSnippet[] getSnippets() {
    return snippets.clone();
  }

  public Map<String, String> getMeta() {
    return meta;
  }

}
