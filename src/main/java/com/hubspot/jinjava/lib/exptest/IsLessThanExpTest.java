package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "lt")
public class IsLessThanExpTest extends IsLtTest {

  @Override
  public String getName() {
    return "lessthan";
  }
}
