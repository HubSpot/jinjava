package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "le")
public class IsLessThanOrEqualToSymbolExpTest extends IsLtTest {

  @Override
  public String getName() {
    return "<=";
  }
}
