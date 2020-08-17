package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "ne")
public class IsNotEqualToSymbolExpTest extends IsNeExpTest {

  @Override
  public String getName() {
    return "!=";
  }
}
