package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;

@JinjavaDoc(value="", aliasOf="default")
public class DAliasedDefaultFilter extends DefaultFilter {

  @Override
  public String getName() {
    return "d";
  }

}
