package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;

@JinjavaDoc(value = "", aliasOf = "in")
public class IsWithinExpTest extends IsInExpTest {

  @Override
  public String getName() {
    return "within";
  }
}
