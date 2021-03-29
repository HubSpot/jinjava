package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstProperty;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstProperty extends AstProperty implements EvalResultHolder {
  private Object evalResult;
  private AstProperty astProperty;
  protected final AstNode prefix;

  private EagerAstProperty(AstProperty astProperty) {
    this(
      (AstNode) EagerAstNode.getAsEvalResultHolder(astProperty.getChild(0)),
      astProperty.isLeftValue(),
      false,
      false
    );
    this.astProperty = astProperty;
  }

  private EagerAstProperty(
    AstNode prefix,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(prefix, lvalue, strict, ignoreReturnType);
    this.prefix = prefix;
  }

  public static EvalResultHolder getAsEvalResultHolder(AstProperty astProperty) {
    if (astProperty instanceof EvalResultHolder) {
      return (EvalResultHolder) astProperty;
    }
    return new EagerAstProperty(astProperty);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = astProperty.eval(bindings, context);
      return evalResult;
    } finally {
      ((EvalResultHolder) prefix).getAndClearEvalResult();
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return evalResult != null;
  }

  @Override
  protected Object getProperty(Bindings bindings, ELContext context) throws ELException {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    astProperty.appendStructure(builder, bindings);
  }

  @Override
  public int getCardinality() {
    return astProperty.getCardinality();
  }
}
