package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;

public class EagerExpressionResolverTestSomethingPyish implements PyishSerializable {

  private String name;

  public EagerExpressionResolverTestSomethingPyish(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
