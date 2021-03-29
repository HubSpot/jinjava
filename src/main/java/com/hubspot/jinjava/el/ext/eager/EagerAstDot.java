package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstDot;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstDot extends AstDot implements EvalResultHolder {
  private Object evalResult;
  private final EvalResultHolder base;
  private final String property;

  public EagerAstDot(
    AstNode base,
    String property,
    boolean lvalue,
    boolean ignoreReturnType
  ) {
    this(EagerAstNode.getAsEvalResultHolder(base), property, lvalue, ignoreReturnType);
  }

  public EagerAstDot(
    EvalResultHolder base,
    String property,
    boolean lvalue,
    boolean ignoreReturnType
  ) {
    super((AstNode) base, property, lvalue, ignoreReturnType);
    this.base = base;
    this.property = property;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) throws ELException {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        String.format("%s.%s", e.getDeferredEvalResult(), this.property)
      );
    } finally {
      base.getAndClearEvalResult();
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

  public String getProperty() {
    return property;
  }
}
