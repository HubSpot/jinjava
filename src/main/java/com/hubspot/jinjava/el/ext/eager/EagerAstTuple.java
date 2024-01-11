package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstTuple extends AstTuple implements EvalResultHolder {

  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstTuple(AstParameters elements) {
    super(elements);
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
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    IdentifierPreservationStrategy identifierPreservationStrategy
  ) {
    StringJoiner joiner = new StringJoiner(", ");
    for (int i = 0; i < elements.getCardinality(); i++) {
      joiner.add(
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) elements.getChild(i),
          deferredParsingException,
          identifierPreservationStrategy
        )
      );
    }
    return '(' + joiner.toString() + ')';
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
}
