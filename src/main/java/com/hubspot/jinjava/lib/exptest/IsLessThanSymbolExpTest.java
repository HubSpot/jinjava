package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "lt")
public class IsLessThanSymbolExpTest extends IsLtTest {

  @Override
  public String getName() {
    return "<";
  }
}
