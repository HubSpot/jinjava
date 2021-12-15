package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
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
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, false)
      );
    }
  }

  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException e,
    boolean preserveIdentifier
  ) {
    return String.format(
      "%s.%s",
      EvalResultHolder.reconstructNode(bindings, context, base, e, preserveIdentifier),
      property
    );
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    base.clearEvalResult();
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
