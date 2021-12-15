package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstList extends AstList implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstList(AstParameters elements) {
    super(elements);
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
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    for (int i = 0; i < elements.getCardinality(); i++) {
      ((EvalResultHolder) elements.getChild(i)).clearEvalResult();
    }
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    StringJoiner joiner = new StringJoiner(", ");
    for (int i = 0; i < elements.getCardinality(); i++) {
      joiner.add(
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) elements.getChild(i),
          deferredParsingException,
          preserveIdentifier
        )
      );
    }
    return "[" + joiner.toString() + "]";
  }
}
