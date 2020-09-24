package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "gt")
public class IsGreaterThanSymbolExpTest extends IsGtTest {

  @Override
  public String getName() {
    return ">";
  }
}
