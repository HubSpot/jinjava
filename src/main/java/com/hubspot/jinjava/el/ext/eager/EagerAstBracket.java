package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBracket;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstBracket extends AstBracket implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstBracket(
    AstNode base,
    AstNode property,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(base),
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(property),
      lvalue,
      strict,
      ignoreReturnType
    );
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    return EvalResultHolder.super.eval(
      () -> super.eval(bindings, context),
      bindings,
      context
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

  public AstNode getMethod() {
    return property;
  }

  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    IdentifierPreservationStrategy identifierPreservationStrategy
  ) {
    return String.format(
      "%s[%s]",
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        (EvalResultHolder) prefix,
        deferredParsingException,
        identifierPreservationStrategy
      ),
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        (EvalResultHolder) property,
        deferredParsingException,
        IdentifierPreservationStrategy.RESOLVING
      )
    );
  }
}
