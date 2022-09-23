package com.hubspot.jinjava.el.ext;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBinary.Operator;
import de.odysseus.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;

public class OrOperator implements Operator {

  @Override
  public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
    Object leftResult = left.eval(bindings, context);
    if (bindings.convert(leftResult, Boolean.class)) {
      return leftResult;
    }

    return right.eval(bindings, context);
  }

  @Override
  public String toString() {
    return "||";
  }

  public static final OrOperator OP = new OrOperator();
}
