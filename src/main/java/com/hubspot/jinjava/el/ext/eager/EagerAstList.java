package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstList extends AstList implements EvalResultHolder {
  private Object evalResult;

  public EagerAstList(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(", ");
      for (int i = 0; i < elements.getCardinality(); i++) {
        joiner.add(
          EvalResultHolder.reconstructNode(
            bindings,
            context,
            (EvalResultHolder) elements.getChild(i),
            e,
            false
          )
        );
      }
      throw new DeferredParsingException(this, "[" + joiner.toString() + "]");
    } finally {
      for (int i = 0; i < elements.getCardinality(); i++) {
        ((EvalResultHolder) elements.getChild(i)).getAndClearEvalResult();
      }
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
}
