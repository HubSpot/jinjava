package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "gt")
public class IsGreaterThanExpTest extends IsGtTest {

  @Override
  public String getName() {
    return "greaterthan";
  }
}
