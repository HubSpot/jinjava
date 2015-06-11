package com.hubspot.jinjava.el;

import com.hubspot.jinjava.el.ext.ExtendedParser;

import de.odysseus.el.tree.impl.Builder;
import de.odysseus.el.tree.impl.Parser;

/**
 * Syntax extensions for the expression language library
 *
 * - pipe '|' postfix unary operator for applying filters to expressions - positive '+' prefix unary operator for absolute value of numeric - 'is' postfix operator for creating a boolean expression with an expression test function - named
 * function args support (still requires precise order, but accepts syntax of fn(foo=bar, a=b)
 *
 */
public class ExtendedSyntaxBuilder extends Builder {
  private static final long serialVersionUID = 1L;

  public ExtendedSyntaxBuilder() {
    super();
  }

  public ExtendedSyntaxBuilder(Feature... features) {
    super(features);
  }

  @Override
  protected Parser createParser(String expression) {
    return new ExtendedParser(this, expression);
  }

}
