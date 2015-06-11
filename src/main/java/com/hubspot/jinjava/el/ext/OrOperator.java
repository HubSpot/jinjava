package com.hubspot.jinjava.el.ext;

import javax.el.ELContext;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBinary.Operator;
import de.odysseus.el.tree.impl.ast.AstNode;

public class OrOperator implements Operator {

  @Override
  public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
    Object leftResult = left.eval(bindings, context);
    if (bindings.convert(leftResult, Boolean.class)) {
      return leftResult;
    }

    return right.eval(bindings, context);
  }

  public static final OrOperator OP = new OrOperator();
}
