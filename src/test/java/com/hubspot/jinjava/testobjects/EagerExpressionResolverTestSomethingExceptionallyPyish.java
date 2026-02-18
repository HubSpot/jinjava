package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;

public class EagerExpressionResolverTestSomethingExceptionallyPyish
  implements PyishSerializable {

  private String name;

  public EagerExpressionResolverTestSomethingExceptionallyPyish(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    throw new DeferredValueException("Can't serialize");
  }
}
