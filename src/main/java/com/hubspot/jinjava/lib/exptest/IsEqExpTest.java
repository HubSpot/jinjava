package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "equalto")
public class IsEqExpTest extends IsEqualToExpTest {

  @Override
  public String getName() {
    return "eq";
  }
}
