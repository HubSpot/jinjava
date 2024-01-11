package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstDot;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstDot extends AstDot implements EvalResultHolder {

  protected Object evalResult;
  protected boolean hasEvalResult;
  protected final EvalResultHolder base;
  protected final String property;

  public EagerAstDot(
    AstNode base,
    String property,
    boolean lvalue,
    boolean ignoreReturnType
  ) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(base),
      property,
      lvalue,
      ignoreReturnType
    );
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
    return EvalResultHolder.super.eval(
      () -> super.eval(bindings, context),
      bindings,
      context
    );
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException e,
    IdentifierPreservationStrategy identifierPreservationStrategy
  ) {
    return String.format(
      "%s.%s",
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        base,
        e,
        identifierPreservationStrategy
      ),
      property
    );
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void setEvalResult(Object evalResult) {
    this.evalResult = evalResult;
    hasEvalResult = true;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  public AstNode getPrefix() {
    return prefix;
  }

  public String getProperty() {
    return property;
  }
}
