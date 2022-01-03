package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ExtendedSyntaxBuilder;
import com.hubspot.jinjava.el.tree.impl.Parser;

public class EagerExtendedSyntaxBuilder extends ExtendedSyntaxBuilder {

  public EagerExtendedSyntaxBuilder() {
    super();
  }

  public EagerExtendedSyntaxBuilder(Feature... features) {
    super(features);
  }

  @Override
  protected Parser createParser(String expression) {
    return new EagerExtendedParser(this, expression);
  }
}
