package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstNamedParameter;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstNamedParameter
  extends AstNamedParameter
  implements EvalResultHolder {
  protected boolean hasEvalResult;
  protected Object evalResult;
  protected final AstIdentifier name;
  protected final EvalResultHolder value;

  public EagerAstNamedParameter(AstIdentifier name, AstNode value) {
    this(name, EagerAstNodeDecorator.getAsEvalResultHolder(value));
  }

  private EagerAstNamedParameter(AstIdentifier name, EvalResultHolder value) {
    super(name, (AstNode) value);
    this.name = name;
    this.value = value;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, false)
      );
    }
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    return String.format(
      "%s=%s",
      name,
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        value,
        deferredParsingException,
        false
      )
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
    value.clearEvalResult();
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
