package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstProperty;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstPropertyDecorator extends AstProperty implements EvalResultHolder {
  private Object evalResult;
  private AstProperty astProperty;
  protected final AstNode prefix;

  private EagerAstPropertyDecorator(AstProperty astProperty) {
    this(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(astProperty.getChild(0)),
      astProperty.isLeftValue(),
      false,
      false
    );
    this.astProperty = astProperty;
  }

  private EagerAstPropertyDecorator(
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
    return new EagerAstPropertyDecorator(astProperty);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    if (evalResult != null) {
      return evalResult;
    }
    evalResult = astProperty.eval(bindings, context);
    return evalResult;
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
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
