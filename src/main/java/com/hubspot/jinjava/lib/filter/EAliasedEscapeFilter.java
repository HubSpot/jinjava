package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;

@JinjavaDoc(value="", aliasOf="escape")
public class EAliasedEscapeFilter extends EscapeFilter {

  @Override
  public String getName() {
    return "e";
  }

}
