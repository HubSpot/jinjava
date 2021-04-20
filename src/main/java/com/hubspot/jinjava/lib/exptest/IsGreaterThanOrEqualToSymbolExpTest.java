package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "ge")
public class IsGreaterThanOrEqualToSymbolExpTest extends IsGeTest {

  @Override
  public String getName() {
    return ">=";
  }
}
