package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;

@JinjavaDoc(value="", aliasOf="length")
public class CountFilter extends LengthFilter {

  @Override
  public String getName() {
    return "count";
  }

}
